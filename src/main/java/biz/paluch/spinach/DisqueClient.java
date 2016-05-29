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

import com.google.common.base.Supplier;
import com.lambdaworks.redis.*;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.codec.Utf8StringCodec;
import com.lambdaworks.redis.protocol.CommandHandler;
import com.lambdaworks.redis.protocol.RedisCommand;
import com.lambdaworks.redis.resource.ClientResources;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.impl.*;

/**
 * A scalable thread-safe Disque client. Multiple threads may share one connection if they avoid blocking operations.
 * {@link DisqueClient} is an expensive resource. It holds a set of netty's {@link io.netty.channel.EventLoopGroup}'s that
 * consist of up to {@code Number of CPU's * 4} threads. Reuse this instance as much as possible.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class DisqueClient extends AbstractRedisClient {

    private final static DisqueURI EMPTY_DISQUE_URI = new DisqueURI();
    private final DisqueURI disqueURI;

    protected DisqueClient(ClientResources clientResources, DisqueURI disqueURI) {

        super(clientResources);

        assertNotNull(disqueURI);

        this.disqueURI = disqueURI;

        setOptions(new ClientOptions.Builder().build());
        setDefaultTimeout(60, TimeUnit.MINUTES);
        setDefaultTimeout(disqueURI.getTimeout(), disqueURI.getUnit());
    }

    /**
     * Creates a uri-less {@link DisqueClient}. You can connect to different Disque servers but you must supply a
     * {@link DisqueURI} on connecting. Methods without having a {@link DisqueURI} will fail with a
     * {@link java.lang.IllegalStateException}.
     * 
     * @deprecated Use the factory method {@link #create()}
     */
    @Deprecated
    public DisqueClient() {
        this(null, EMPTY_DISQUE_URI);
    }

    /**
     * Create a new client that connects to the supplied host on the default port.
     *
     * @param uri a Disque URI.
     * @deprecated Use the factory method {@link #create(String)}
     */
    @Deprecated
    public DisqueClient(String uri) {
        this(uri != null && uri.startsWith("disque") ? DisqueURI.create(uri) : new DisqueURI.Builder().disque(uri).build());
    }

    /**
     * Create a new client that connects to the supplied host and port. Connection attempts and non-blocking commands will
     * {@link #setDefaultTimeout timeout} after 60 seconds.
     *
     * @param host Server hostname.
     * @param port Server port.
     * @deprecated Use the factory method {@link #create(DisqueURI)}
     */
    @Deprecated
    public DisqueClient(String host, int port) {
        this(DisqueURI.Builder.disque(host, port).build());
    }

    /**
     * Create a new client that connects to the supplied host and port. Connection attempts and non-blocking commands will
     * {@link #setDefaultTimeout timeout} after 60 seconds.
     *
     * @param disqueURI Disque URI.
     * @deprecated Use the factory method {@link #create(DisqueURI)}
     */
    @Deprecated
    public DisqueClient(DisqueURI disqueURI) {
        this(null, disqueURI);
    }

    /**
     * Creates a uri-less DisqueClient with default {@link ClientResources}. You can connect to different Redis servers but you
     * must supply a {@link RedisURI} on connecting. Methods without having a {@link RedisURI} will fail with a
     * {@link java.lang.IllegalStateException}.
     * 
     * @return a new instance of {@link DisqueClient}
     */
    public static DisqueClient create() {
        return new DisqueClient(null, EMPTY_DISQUE_URI);
    }

    /**
     * Create a new client that connects to the supplied {@link RedisURI uri} with default {@link ClientResources}. You can
     * connect to different Redis servers but you must supply a {@link RedisURI} on connecting.
     * 
     * @param disqueURI the Redis URI, must not be {@literal null}
     * @return a new instance of {@link DisqueClient}
     */
    public static DisqueClient create(DisqueURI disqueURI) {
        assertNotNull(disqueURI);
        return new DisqueClient(null, disqueURI);
    }

    /**
     * Create a new client that connects to the supplied uri with default {@link ClientResources}. You can connect to different
     * Redis servers but you must supply a {@link RedisURI} on connecting.
     *
     * @param uri the Redis URI, must not be {@literal null}
     * @return a new instance of {@link DisqueClient}
     */
    public static DisqueClient create(String uri) {
        checkArgument(uri != null, "uri must not be null");
        return new DisqueClient(null, DisqueURI.create(uri));
    }

    /**
     * Creates a uri-less DisqueClient with shared {@link ClientResources}. You need to shut down the {@link ClientResources}
     * upon shutting down your application. You can connect to different Redis servers but you must supply a {@link RedisURI} on
     * connecting. Methods without having a {@link RedisURI} will fail with a {@link java.lang.IllegalStateException}.
     *
     * @param clientResources the client resources, must not be {@literal null}
     * @return a new instance of {@link DisqueClient}
     */
    public static DisqueClient create(ClientResources clientResources) {
        assertNotNull(clientResources);
        return new DisqueClient(clientResources, EMPTY_DISQUE_URI);
    }

    /**
     * Create a new client that connects to the supplied uri with shared {@link ClientResources}.You need to shut down the
     * {@link ClientResources} upon shutting down your application. You can connect to different Redis servers but you must
     * supply a {@link RedisURI} on connecting.
     *
     * @param clientResources the client resources, must not be {@literal null}
     * @param uri the Redis URI, must not be {@literal null}
     *
     * @return a new instance of {@link DisqueClient}
     */
    public static DisqueClient create(ClientResources clientResources, String uri) {
        assertNotNull(clientResources);
        checkArgument(uri != null, "uri must not be null");
        return create(clientResources, DisqueURI.create(uri));
    }

    /**
     * Create a new client that connects to the supplied {@link RedisURI uri} with shared {@link ClientResources}. You need to
     * shut down the {@link ClientResources} upon shutting down your application.You can connect to different Redis servers but
     * you must supply a {@link RedisURI} on connecting.
     * 
     * @param clientResources the client resources, must not be {@literal null}
     * @param disqueURI the Redis URI, must not be {@literal null}
     * @return a new instance of {@link DisqueClient}
     */
    public static DisqueClient create(ClientResources clientResources, DisqueURI disqueURI) {
        assertNotNull(clientResources);
        assertNotNull(disqueURI);
        return new DisqueClient(clientResources, disqueURI);
    }

    /**
     * Open a new connection to a Disque server that treats keys and values as UTF-8 strings. This method requires to have the
     * {@link DisqueURI} specified when constructing the client. Command timeouts are applied from the default
     * {@link #setDefaultTimeout(long, TimeUnit)} settings.
     *
     * @return A new connection.
     */
    public DisqueConnection<String, String> connect() {
        return connect(new Utf8StringCodec());
    }

    /**
     * Open a new connection to a Disque server. Use the supplied {@link RedisCodec codec} to encode/decode keys and values.
     * Command timeouts are applied from the default {@link #setDefaultTimeout(long, TimeUnit)} settings.
     *
     * @param codec use this codec to encode/decode keys and values, must note be {@literal null}
     * @param <K> Key type.
     * @param <V> Value type.
     * @return A new connection.
     */
    public <K, V> DisqueConnection<K, V> connect(RedisCodec<K, V> codec) {
        checkForDisqueURI();
        return connect0(codec, this.disqueURI, SocketAddressSupplierFactory.Factories.ROUND_ROBIN, timeout, unit);
    }

    /**
     * Open a new connection to a Disque server with the supplied {@link DisqueURI} that treats keys and values as UTF-8
     * strings. Command timeouts are applied from the given {@link DisqueURI#getTimeout()} settings.
     *
     * @param disqueURI the disque server to connect to, must not be {@literal null}
     * @return A new connection.
     */
    public DisqueConnection<String, String> connect(DisqueURI disqueURI) {
        return connect(new Utf8StringCodec(), disqueURI, SocketAddressSupplierFactory.Factories.ROUND_ROBIN);
    }

    /**
     * Open a new connection to a Disque server using the supplied {@link DisqueURI} and the supplied {@link RedisCodec codec}
     * to encode/decode keys. Command timeouts are applied from the given {@link DisqueURI#getTimeout()} settings.
     *
     * @param codec use this codec to encode/decode keys and values, must not be {@literal null}
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

        assertNotNull(disqueURI);
        checkValidDisqueURI(disqueURI);
        return connect0(codec, disqueURI, socketAddressSupplierFactory, disqueURI.getTimeout(), disqueURI.getUnit());
    }

    private <K, V> DisqueConnectionImpl<K, V> connect0(RedisCodec<K, V> codec, final DisqueURI disqueURI,
            SocketAddressSupplierFactory socketAddressSupplierFactory, long timeout, TimeUnit unit) {

        checkArgument(codec != null, "RedisCodec must not be null");
        checkValidDisqueURI(disqueURI);
        checkArgument(socketAddressSupplierFactory != null, "SocketAddressSupplierFactory must not be null");

        BlockingQueue<RedisCommand<K, V, ?>> queue = new LinkedBlockingQueue<RedisCommand<K, V, ?>>();

        checkArgument(!disqueURI.getConnectionPoints().isEmpty(), "No connection points specified");

        ClientOptions options = getOptions();
        final CommandHandler<K, V> commandHandler = new CommandHandler<K, V>(options, clientResources, queue);
        final DisqueConnectionImpl<K, V> connection = newDisquelAsyncConnectionImpl(commandHandler, codec, timeout, unit);

        logger.debug("Trying to get a Disque connection for one of: " + disqueURI.getConnectionPoints());

        final RedisURI redisURI = new RedisURI();
        toRedisURI(disqueURI, null, redisURI);
        ConnectionBuilder connectionBuilder = connectionBuilder(disqueURI, options, commandHandler, connection, redisURI);

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

        if (socketAddressSupplier instanceof ConnectionAware) {
            ((ConnectionAware) socketAddressSupplier).setConnection(connection);
        }

        if (socketAddressSupplier instanceof EventExecutorAware) {
            ((EventExecutorAware) socketAddressSupplier).setEventExecutor(genericWorkerPool);
        }

        return connection;
    }

    private <K, V> ConnectionBuilder connectionBuilder(DisqueURI disqueURI, ClientOptions options,
            CommandHandler<K, V> commandHandler, DisqueConnectionImpl<K, V> connection, RedisURI redisURI) {
        ConnectionBuilder connectionBuilder;
        if (disqueURI.isSsl()) {
            connectionBuilder = SslConnectionBuilder.sslConnectionBuilder().ssl(redisURI);
        } else {
            connectionBuilder = ConnectionBuilder.connectionBuilder();
        }

        connectionBuilder.clientOptions(options);
        connectionBuilder.clientResources(clientResources);
        connectionBuilder(commandHandler, connection, null, connectionBuilder, redisURI);
        return connectionBuilder;
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

    /**
     * Returns the {@link ClientResources} which are used with that client.
     *
     * @return the {@link ClientResources} for this client
     */
    public ClientResources getResources() {
        return clientResources;
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
        checkArgument(disqueURI != EMPTY_DISQUE_URI && !disqueURI.getConnectionPoints().isEmpty(),
                "A valid DisqueURI with a host is needed");
    }

    private void checkForDisqueURI() {
        checkState(this.disqueURI != EMPTY_DISQUE_URI,
                "DisqueURI is not available. Use DisqueClient(Host), DisqueClient(Host, Port) or DisqueClient(DisqueURI) to construct your client.");
    }

    private static <K, V> void assertNotNull(RedisCodec<K, V> codec) {
        checkArgument(codec != null, "RedisCodec must not be null");
    }

    private static void assertNotNull(DisqueURI disqueURI) {
        checkArgument(disqueURI != null, "DisqueURI must not be null");
    }

    private static void assertNotNull(ClientResources clientResources) {
        checkArgument(clientResources != null, "ClientResources must not be null");
    }

}
