package biz.paluch.spinach;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.net.ConnectException;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.impl.DisqueConnectionImpl;

import com.google.common.base.Supplier;
import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.ConnectionBuilder;
import com.lambdaworks.redis.ConnectionPoint;
import com.lambdaworks.redis.RedisConnectionException;
import com.lambdaworks.redis.RedisException;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.SslConnectionBuilder;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.codec.Utf8StringCodec;
import com.lambdaworks.redis.protocol.CommandHandler;
import com.lambdaworks.redis.protocol.RedisCommand;

/**
 * A scalable thread-safe Disquelient. Multiple threads may share one connection if they avoid blocking operations.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class DisqueClient extends AbstractRedisClient {
    private final DisqueURI disqueURI;

    /**
     * Creates a uri-less DisqueClient. You can connect to different disque servers but you must supply a {@link DisqueURI} on
     * connecting. Methods without having a {@link DisqueURI} will fail with a {@link java.lang.IllegalStateException}.
     */
    public DisqueClient() {
        disqueURI = null;
        setOptions(new ClientOptions.Builder().build());
        setDefaultTimeout(60, TimeUnit.MINUTES);
    }

    /**
     * Create a new client that connects to the supplied host on the default port.
     * 
     * @param uri a Disque URI.
     *
     */
    public DisqueClient(String uri) {
        this(uri != null && uri.startsWith("disque") ? DisqueURI.create(uri) : new DisqueURI.Builder().disque(uri).build());
    }

    /**
     * Create a new client that connects to the supplied host and port. Connection attempts and non-blocking commands will
     * {@link #setDefaultTimeout timeout} after 60 seconds.
     * 
     * @param host Server hostname.
     * @param port Server port.
     */
    public DisqueClient(String host, int port) {
        this(DisqueURI.Builder.disque(host, port).build());
    }

    /**
     * Create a new client that connects to the supplied host and port. Connection attempts and non-blocking commands will
     * {@link #setDefaultTimeout timeout} after 60 seconds.
     * 
     * @param disqueURI Disque URI.
     */
    public DisqueClient(DisqueURI disqueURI) {
        super();
        this.disqueURI = disqueURI;
        setDefaultTimeout(disqueURI.getTimeout(), disqueURI.getUnit());
        setOptions(new ClientOptions.Builder().build());
    }

    /**
     * Connect to a Disque server that treats keys and values as UTF-8 strings.
     *
     * @return A new connection.
     */
    public DisqueConnection<String, String> connect() {
        return connect(new Utf8StringCodec());
    }

    /**
     * Open a new synchronous connection to the disque server. Use the supplied {@link RedisCodec codec} to encode/decode keys
     * and values.
     *
     * @param codec Use this codec to encode/decode keys and values, must note be {@literal null}
     * @param <K> Key type.
     * @param <V> Value type.
     * @return A new connection.
     */
    public <K, V> DisqueConnection<K, V> connect(RedisCodec<K, V> codec) {
        checkForDisqueURI();
        checkArgument(codec != null, "RedisCodec must not be null");
        return connect(codec, this.disqueURI);
    }

    /**
     * Connect to a Disque server with the supplied {@link DisqueURI} that treats keys and values as UTF-8 strings.
     *
     * @param disqueURI the disque server to connect to, must not be {@literal null}
     * @return A new connection.
     */
    public DisqueConnection<String, String> connect(DisqueURI disqueURI) {
        checkValidDisqueURI(disqueURI);
        return connect(new Utf8StringCodec(), disqueURI);
    }

    private <K, V> DisqueConnectionImpl<K, V> connect(RedisCodec<K, V> codec, DisqueURI disqueURI) {
        BlockingQueue<RedisCommand<K, V, ?>> queue = new LinkedBlockingQueue<RedisCommand<K, V, ?>>();

        ClientOptions options = getOptions();
        final CommandHandler<K, V> commandHandler = new CommandHandler<K, V>(options, queue);
        final DisqueConnectionImpl<K, V> connection = newDisquelAsyncConnectionImpl(commandHandler, codec, timeout, unit);

        logger.debug("Trying to get a disque connection for one of: " + disqueURI.getConnectionPoints());

        ConnectionBuilder connectionBuilder;
        RedisURI redisURI = new RedisURI();
        toRedisURI(disqueURI, null, redisURI);
        if (disqueURI.isSsl()) {
            connectionBuilder = SslConnectionBuilder.sslConnectionBuilder().ssl(redisURI);
        } else {
            connectionBuilder = ConnectionBuilder.connectionBuilder();
        }

        connectionBuilder.clientOptions(options);
        connectionBuilder(commandHandler, connection, null, connectionBuilder, redisURI);

        boolean connected = false;
        Exception causingException = null;
        boolean first = true;

        validateUrisAreOfSameConnectionType(disqueURI.getConnectionPoints());
        for (ConnectionPoint connectionPoint : disqueURI.getConnectionPoints()) {
            toRedisURI(disqueURI, connectionPoint, redisURI);
            if (first) {
                channelType(connectionBuilder, connectionPoint);
                first = false;
            }
            connectionBuilder.socketAddressSupplier(getSocketAddressSupplier(connectionPoint));
            logger.debug("Connecting to disque, address: " + getSocketAddress(connectionPoint));
            try {
                initializeChannel(connectionBuilder);
                connected = true;
                break;
            } catch (Exception e) {
                logger.warn("Cannot connect disque " + connectionPoint + ": " + e.toString());
                causingException = e;
                if (e instanceof ConnectException) {
                    continue;
                }
            }
        }

        try {
            if (disqueURI.getPassword() != null) {
                connection.sync().auth(new String(disqueURI.getPassword()));
            }
        } catch (RedisException e) {
            connection.close();
            throw e;
        }

        if (!connected) {
            throw new RedisConnectionException("Cannot connect to disque: " + disqueURI, causingException);
        }

        return connection;
    }

    private void validateUrisAreOfSameConnectionType(List<? extends ConnectionPoint> connectionPoints) {
        boolean unixDomainSocket = false;
        boolean inetSocket = false;
        for (ConnectionPoint connectionPoint : connectionPoints) {
            if (connectionPoint.getSocket() != null) {
                unixDomainSocket = true;
            }
            if (connectionPoint.getHost() != null) {
                inetSocket = true;
            }
        }

        if (unixDomainSocket && inetSocket) {
            throw new RedisConnectionException("You cannot mix unix domain socket and IP socket URI's");
        }

    }

    private void toRedisURI(DisqueURI disqueURI, ConnectionPoint connectionPoint, RedisURI redisURI) {

        redisURI.setSsl(disqueURI.isSsl());
        redisURI.setStartTls(disqueURI.isStartTls());
        redisURI.setVerifyPeer(disqueURI.isVerifyPeer());
        redisURI.setTimeout(disqueURI.getTimeout());
        redisURI.setUnit(disqueURI.getUnit());

        if (connectionPoint != null) {
            redisURI.setPort(connectionPoint.getPort());
            redisURI.setHost(connectionPoint.getHost());
        }

    }

    protected <K, V> DisqueConnectionImpl<K, V> newDisquelAsyncConnectionImpl(CommandHandler<K, V> commandHandler,
            RedisCodec<K, V> codec, long timeout, TimeUnit unit) {
        return new DisqueConnectionImpl<K, V>(commandHandler, codec, timeout, unit);
    }

    private Supplier<SocketAddress> getSocketAddressSupplier(final ConnectionPoint connectionPoint) {
        return new Supplier<SocketAddress>() {
            @Override
            public SocketAddress get() {

                return getSocketAddress(connectionPoint);
            }

        };
    }

    private SocketAddress getSocketAddress(ConnectionPoint connectionPoint) {
        if (connectionPoint instanceof DisqueURI.DisqueSocket) {
            return ((DisqueURI.DisqueSocket) connectionPoint).getResolvedAddress();
        }
        return ((DisqueURI.DisqueHostAndPort) connectionPoint).getResolvedAddress();
    }

    private void checkValidDisqueURI(DisqueURI disqueURI) {
        checkArgument(disqueURI != null && !disqueURI.getConnectionPoints().isEmpty(),
                "A valid DisqueURI with a host is needed");
    }

    private void checkForDisqueURI() {
        checkState(
                this.disqueURI != null,
                "DisqueURI is not available. Use DisqueClient(Host), DisqueClient(Host, Port) or DisqueClient(DisqueURI) to construct your client.");
    }

}
