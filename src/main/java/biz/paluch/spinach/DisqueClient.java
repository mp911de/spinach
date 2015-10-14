package biz.paluch.spinach;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.impl.DisqueConnectionImpl;
import biz.paluch.spinach.impl.SocketAddressSupplier;
import biz.paluch.spinach.impl.SocketAddressSupplierFactory;

import com.google.common.base.Supplier;
import com.lambdaworks.redis.*;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.codec.Utf8StringCodec;
import com.lambdaworks.redis.protocol.CommandHandler;
import com.lambdaworks.redis.protocol.RedisCommand;

/**
 * A scalable thread-safe Disque client. Multiple threads may share one connection if they avoid blocking operations.
 * {@link DisqueClient} is an expensive resource. It holds a set of netty's {@link io.netty.channel.EventLoopGroup}'s that
 * consist of up to {@code Number of CPU's * 4} threads. Reuse this instance as much as possible.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class DisqueClient extends AbstractRedisClient {
    private final DisqueURI disqueURI;

    /**
     * Creates a uri-less {@link DisqueClient}. You can connect to different Disque servers but you must supply a
     * {@link DisqueURI} on connecting. Methods without having a {@link DisqueURI} will fail with a
     * {@link java.lang.IllegalStateException}.
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
     * Open a new connection to a Disque server that treats keys and values as UTF-8 strings. This method requires to have the
     * {@link DisqueURI} specified when constructing the client.
     *
     * @return A new connection.
     */
    public DisqueConnection<String, String> connect() {
        return connect(new Utf8StringCodec());
    }

    /**
     * Open a new connection to a Disque server. Use the supplied {@link RedisCodec codec} to encode/decode keys and values.
     *
     * @param codec Use this codec to encode/decode keys and values, must note be {@literal null}
     * @param <K> Key type.
     * @param <V> Value type.
     * @return A new connection.
     */
    public <K, V> DisqueConnection<K, V> connect(RedisCodec<K, V> codec) {
        checkForDisqueURI();
        return connect(codec, this.disqueURI, SocketAddressSupplierFactory.Factories.ROUND_ROBIN);
    }

    /**
     * Open a new connection to a Disque server with the supplied {@link DisqueURI} that treats keys and values as UTF-8
     * strings.
     *
     * @param disqueURI the disque server to connect to, must not be {@literal null}
     * @return A new connection.
     */
    public DisqueConnection<String, String> connect(DisqueURI disqueURI) {
        return connect(new Utf8StringCodec(), disqueURI, SocketAddressSupplierFactory.Factories.ROUND_ROBIN);
    }

    /**
     * Open a new connection to a Disque server using the supplied {@link DisqueURI} and the supplied {@link RedisCodec codec}
     * to encode/decode keys.
     *
     * @param codec Use this codec to encode/decode keys and values, must not be {@literal null}
     * @param disqueURI the Disque server to connect to, must not be {@literal null}
     * @param socketAddressSupplierFactory factory for {@link SocketAddress} for connecting to Disque based on multiple
     *        connection points.
     * @param <K> Key type.
     * @param <V> Value type.
     * 
     * @return A new connection.
     */
    public <K, V> DisqueConnection<K, V> connect(RedisCodec<K, V> codec, DisqueURI disqueURI,
            SocketAddressSupplierFactory socketAddressSupplierFactory) {
        return connect0(codec, disqueURI, socketAddressSupplierFactory);
    }

    private <K, V> DisqueConnectionImpl<K, V> connect0(RedisCodec<K, V> codec, final DisqueURI disqueURI,
            SocketAddressSupplierFactory socketAddressSupplierFactory) {

        checkArgument(codec != null, "RedisCodec must not be null");
        checkValidDisqueURI(disqueURI);
        checkArgument(socketAddressSupplierFactory != null, "SocketAddressSupplierFactory must not be null");

        BlockingQueue<RedisCommand<K, V, ?>> queue = new LinkedBlockingQueue<RedisCommand<K, V, ?>>();

        checkArgument(!disqueURI.getConnectionPoints().isEmpty(), "No connection points specified");

        ClientOptions options = getOptions();
        final CommandHandler<K, V> commandHandler = new CommandHandler<K, V>(options, queue);
        final DisqueConnectionImpl<K, V> connection = newDisquelAsyncConnectionImpl(commandHandler, codec, timeout, unit);

        logger.debug("Trying to get a Disque connection for one of: " + disqueURI.getConnectionPoints());

        ConnectionBuilder connectionBuilder;
        final RedisURI redisURI = new RedisURI();
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

        validateUrisAreOfSameConnectionType(disqueURI.getConnectionPoints());

        int connectionAttempts = disqueURI.getConnectionPoints().size();
        final SocketAddressSupplier socketAddressSupplier = socketAddressSupplierFactory.newSupplier(disqueURI);

        channelType(connectionBuilder, disqueURI.getConnectionPoints().get(0));
        connectionBuilder.socketAddressSupplier(new Supplier<SocketAddress>() {
            @Override
            public SocketAddress get() {

                SocketAddress socketAddress = socketAddressSupplier.get();
                if (socketAddress instanceof InetSocketAddress) {
                    InetSocketAddress isa = (InetSocketAddress) socketAddress;
                    redisURI.setPort(isa.getPort());
                    redisURI.setHost(isa.getHostName());
                }
                return socketAddress;
            }
        });

        for (int i = 0; i < connectionAttempts; i++) {
            try {
                initializeChannel(connectionBuilder);
                connected = true;
                break;
            } catch (Exception e) {
                logger.warn(e.getMessage());
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
            throw new RedisConnectionException("Cannot connect to Disque: " + disqueURI, causingException);
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
