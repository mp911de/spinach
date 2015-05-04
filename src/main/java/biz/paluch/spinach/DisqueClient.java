package biz.paluch.spinach;

import static com.google.common.base.Preconditions.*;

import java.net.ConnectException;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.impl.DisqueAsyncConnectionImpl;
import com.google.common.base.Supplier;
import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.ConnectionBuilder;
import com.lambdaworks.redis.ConnectionPoint;
import com.lambdaworks.redis.RedisConnectionException;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.codec.Utf8StringCodec;
import com.lambdaworks.redis.protocol.CommandHandler;
import com.lambdaworks.redis.protocol.RedisCommand;
import io.netty.channel.ChannelOption;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class DisqueClient extends AbstractRedisClient {
    private final RedisCodec<String, String> codec = new Utf8StringCodec();
    private final DisqueURI disqueURI;

    /**
     * Creates a uri-less DisqueClient. You can connect to different disque servers but you must supply a {@link DisqueURI} on
     * connecting. Methods without having a {@link DisqueURI} will fail with a {@link java.lang.IllegalStateException}.
     */
    public DisqueClient() {
        disqueURI = null;
        setDefaultTimeout(60, TimeUnit.MINUTES);
    }

    /**
     * Create a new client that connects to the supplied host on the default port.
     * 
     * @param host Server hostname.
     */
    public DisqueClient(String host) {
        this(host, DisqueURI.DEFAULT_DISQUE_PORT);
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
    }

    /**
     * Open a new synchronous connection to the disque server that treats keys and values as UTF-8 strings.
     *
     * @return A new connection.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DisqueConnection<String, String> connect() {
        return (DisqueConnection<String, String>) connect((RedisCodec) codec);
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
    @SuppressWarnings("unchecked")
    public <K, V> DisqueConnection<K, V> connect(RedisCodec<K, V> codec) {
        checkForDisqueURI();
        checkArgument(codec != null, "RedisCodec must not be null");
        return connect(codec, this.disqueURI);
    }

    /**
     * Open a new synchronous connection to the supplied {@link DisqueURI} that treats keys and values as UTF-8 strings.
     *
     * @param disqueURI the disque server to connect to, must not be {@literal null}
     * @return A new connection.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DisqueConnection<String, String> connect(DisqueURI disqueURI) {
        checkValidDisqueURI(disqueURI);
        return (DisqueConnection<String, String>) connect((RedisCodec) codec, disqueURI);
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

    @SuppressWarnings({ "rawtypes" })
    private <K, V> DisqueConnection connect(RedisCodec<K, V> codec, DisqueURI disqueURI) {
        return (DisqueConnection) syncHandler(connectAsyncImpl(codec, disqueURI), DisqueConnection.class);
    }

    /**
     * Open a new asynchronous connection to the disque server that treats keys and values as UTF-8 strings.
     *
     * @return A new connection.
     */
    public DisqueAsyncConnection<String, String> connectAsync() {
        return connectAsync(codec);
    }

    /**
     * Open a new asynchronous connection to the disque server. Use the supplied {@link RedisCodec codec} to encode/decode keys
     * and values.
     *
     * @param codec Use this codec to encode/decode keys and values, must not be {@literal null}
     * @param <K> Key type.
     * @param <V> Value type.
     * @return A new connection.
     */
    public <K, V> DisqueAsyncConnection<K, V> connectAsync(RedisCodec<K, V> codec) {
        checkForDisqueURI();
        checkArgument(codec != null, "RedisCodec must not be null");
        return connectAsyncImpl(codec, disqueURI);
    }

    /**
     * Open a new asynchronous connection to the supplied {@link DisqueURI} that treats keys and values as UTF-8 strings.
     *
     * @param disqueURI the disque server to connect to, must not be {@literal null}
     * @return A new connection.
     */
    public DisqueAsyncConnection<String, String> connectAsync(DisqueURI disqueURI) {
        checkValidDisqueURI(disqueURI);
        return connectAsyncImpl(codec, disqueURI);
    }

    private <K, V> DisqueAsyncConnectionImpl<K, V> connectAsyncImpl(RedisCodec<K, V> codec, DisqueURI disqueURI) {
        BlockingQueue<RedisCommand<K, V, ?>> queue = new LinkedBlockingQueue<RedisCommand<K, V, ?>>();

        final CommandHandler<K, V> commandHandler = new CommandHandler<K, V>(queue);
        final DisqueAsyncConnectionImpl<K, V> connection = newDisquelAsyncConnectionImpl(commandHandler, codec, timeout, unit);

        logger.debug("Trying to get a disque connection for one of: " + disqueURI.getConnectionPoints());

        ConnectionBuilder connectionBuilder = ConnectionBuilder.connectionBuilder();

        connectionBuilder(commandHandler, connection, null, true, connectionBuilder, null);
        connectionBuilder.bootstrap().option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                (int) disqueURI.getUnit().toMillis(disqueURI.getTimeout()));

        boolean connected = false;
        Exception causingException = null;
        boolean first = true;
        for (DisqueURI.DisqueHostAndPort uri : disqueURI.getConnectionPoints()) {

            if (first) {
                channelType(connectionBuilder, uri);
                first = false;
            }
            connectionBuilder.socketAddressSupplier(getSocketAddressSupplier(uri));
            logger.debug("Connecting to disque, address: " + uri.getResolvedAddress());
            try {
                initializeChannel(connectionBuilder);
                connected = true;
                break;
            } catch (Exception e) {
                logger.warn("Cannot connect disque at " + uri.getHost() + ":" + uri.getPort() + ": " + e.toString());
                causingException = e;
                if (e instanceof ConnectException) {
                    continue;
                }
            }
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

    protected <K, V> DisqueAsyncConnectionImpl<K, V> newDisquelAsyncConnectionImpl(CommandHandler<K, V> commandHandler,
            RedisCodec<K, V> codec, long timeout, TimeUnit unit) {
        return new DisqueAsyncConnectionImpl<K, V>(commandHandler, codec, timeout, unit);
    }

    private Supplier<SocketAddress> getSocketAddressSupplier(final DisqueURI.DisqueHostAndPort hostAndPort) {
        return new Supplier<SocketAddress>() {
            @Override
            public SocketAddress get() {
                return hostAndPort.getResolvedAddress();
            }

        };
    }
}
