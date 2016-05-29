package biz.paluch.spinach;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.ConnectException;
import java.net.SocketAddress;
import java.nio.channels.UnresolvedAddressException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.support.DefaultDisqueClient;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.commands.AbstractCommandTest;
import biz.paluch.spinach.impl.RoundRobinSocketAddressSupplier;
import biz.paluch.spinach.impl.SocketAddressSupplier;
import biz.paluch.spinach.impl.SocketAddressSupplierFactory;
import biz.paluch.spinach.support.FastShutdown;

import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.Timeout;
import com.lambdaworks.redis.*;
import com.lambdaworks.redis.codec.Utf8StringCodec;
import com.lambdaworks.redis.protocol.CommandHandler;
import com.lambdaworks.redis.resource.ClientResources;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ClientTest extends AbstractCommandTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static ClientResources clientResources =  DefaultDisqueClient.getClientResources();

    @Override
    public void openConnection() throws Exception {
        Logger logger = LogManager.getLogger(CommandHandler.class);
        logger.setLevel(Level.ALL);
        super.openConnection();
    }

    @Override
    public void closeConnection() throws Exception {
        super.closeConnection();
        Logger logger = LogManager.getLogger(CommandHandler.class);
        logger.setLevel(Level.INFO);
    }

    @Test(expected = RedisException.class)
    public void close() throws Exception {
        disque.close();
        disque.auth("");
    }

    @Test
    public void isOpen() throws Exception {

        assertThat(disque.isOpen()).isTrue();
        assertThat(disque.getConnection().isOpen()).isTrue();
        disque.close();
        assertThat(disque.isOpen()).isFalse();
        assertThat(disque.getConnection().isOpen()).isFalse();
    }

    @Test
    public void commandTimeoutIsSetOnConnection() throws Exception {

        DisqueURI disqueURI = DisqueURI.Builder.disque(host, port).withTimeout(10, TimeUnit.MINUTES).build();

        DisqueConnection<String, String> connection = getDisqueClient().connect(disqueURI);
        connection.close();

        assertThat(connection.getTimeout()).isEqualTo(10);
        assertThat(connection.getTimeoutUnit()).isEqualTo(TimeUnit.MINUTES);
    }

    @Test
    public void listenerTest() throws Exception {

        final TestConnectionListener listener = new TestConnectionListener();

        DisqueClient client = DisqueClient.create(clientResources, DisqueURI.create(host, port));
        client.addListener(listener);

        assertThat(listener.onConnected).isNull();
        assertThat(listener.onDisconnected).isNull();
        assertThat(listener.onException).isNull();

        DisqueConnection<String, String> connection = client.connect();
        waitOrTimeout(new Condition() {

            @Override
            public boolean isSatisfied() {
                return listener.onConnected != null;
            }
        }, Timeout.timeout(seconds(2)));

        assertThat(listener.onConnected).isEqualTo(connection);
        assertThat(listener.onDisconnected).isNull();

        connection.close();

        waitOrTimeout(new Condition() {

            @Override
            public boolean isSatisfied() {
                return listener.onDisconnected != null;
            }
        }, Timeout.timeout(seconds(2)));

        assertThat(listener.onConnected).isEqualTo(connection);
        assertThat(listener.onDisconnected).isEqualTo(connection);

        FastShutdown.shutdown(client);

    }

    @Test
    public void listenerTestWithRemoval() throws Exception {

        final TestConnectionListener removedListener = new TestConnectionListener();
        final TestConnectionListener retainedListener = new TestConnectionListener();

        DisqueClient client = DisqueClient.create(clientResources, DisqueURI.create(host, port));
        client.addListener(removedListener);
        client.addListener(retainedListener);
        client.removeListener(removedListener);

        DisqueConnection<String, String> connection = client.connect();
        waitOrTimeout(new Condition() {

            @Override
            public boolean isSatisfied() {
                return retainedListener.onConnected != null;
            }
        }, Timeout.timeout(seconds(2)));

        assertThat(retainedListener.onConnected).isNotNull();

        assertThat(removedListener.onConnected).isNull();
        assertThat(removedListener.onDisconnected).isNull();
        assertThat(removedListener.onException).isNull();
        FastShutdown.shutdown(client);
    }

    @Test
    public void reconnect() throws Exception {

        disque.ping();
        disque.quit();
        Thread.sleep(100);
        assertThat(disque.ping()).isEqualTo("PONG");
        disque.quit();
        Thread.sleep(100);
        assertThat(disque.ping()).isEqualTo("PONG");
        disque.quit();
        Thread.sleep(100);
        assertThat(disque.ping()).isEqualTo("PONG");
    }

    @Test(expected = RedisCommandInterruptedException.class, timeout = 10)
    public void interrupt() throws Exception {
        Thread.currentThread().interrupt();
        disque.ping();
    }

    @Test
    public void connectFailure() throws Exception {
        DisqueClient client = DisqueClient.create(clientResources, "disque://invalid");
        try {
            client.connect();
        } catch (Exception e) {
            assertThat(e).hasRootCauseExactlyInstanceOf(UnresolvedAddressException.class);
        }

        client.shutdown(0, 0, TimeUnit.MILLISECONDS);
    }

    @Test
    public void connectConnectionRefused() throws Exception {
        DisqueClient client =  DisqueClient.create(clientResources, DisqueURI.create(TestSettings.host(), TestSettings.port(999)));
        try {
            client.connect();
        } catch (Exception e) {
            assertThat(e).hasRootCauseExactlyInstanceOf(ConnectException.class);
            assertThat(e.getCause().getCause()).hasMessageContaining(TestSettings.host());
        }
        client.shutdown(0, 0, TimeUnit.MILLISECONDS);
    }

    private class TestConnectionListener implements RedisConnectionStateListener {

        public RedisChannelHandler<?, ?> onConnected;
        public RedisChannelHandler<?, ?> onDisconnected;
        public RedisChannelHandler<?, ?> onException;

        @Override
        public void onRedisConnected(RedisChannelHandler<?, ?> connection) {
            onConnected = connection;
        }

        @Override
        public void onRedisDisconnected(RedisChannelHandler<?, ?> connection) {
            onDisconnected = connection;
        }

        @Override
        public void onRedisExceptionCaught(RedisChannelHandler<?, ?> connection, Throwable cause) {
            onException = connection;

        }
    }

    @Test
    public void emptyClient() throws Exception {

        DisqueClient client = DisqueClient.create(clientResources);
        try {
            client.connect();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("DisqueURI");
        }

        try {
            client.connect((DisqueURI) null);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageContaining("DisqueURI");
        }

        client.shutdown(0, 0, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testExceptionWithCause() throws Exception {
        RedisException e = new RedisException(new RuntimeException());
        assertThat(e).hasCauseExactlyInstanceOf(RuntimeException.class);
    }

    @Test
    public void connectWithCustomStrategyConnectLast() throws Exception {

        int port0 = TestSettings.port(0);
        int port1 = TestSettings.port(1);
        DisqueURI disqueURI = DisqueURI.Builder.disque(host, port0).withDisque(host, port1).build();
        final List<ConnectionPoint> connectionPoints = disqueURI.getConnectionPoints();

        DisqueConnection<String, String> connect = client.connect(new Utf8StringCodec(), disqueURI,
                new SocketAddressSupplierFactory() {
                    @Override
                    public SocketAddressSupplier newSupplier(final DisqueURI disqueURI) {
                        return new RoundRobinSocketAddressSupplier(connectionPoints) {
                            @Override
                            public SocketAddress get() {
                                return getSocketAddress(connectionPoints.get(connectionPoints.size() - 1));
                            }
                        };
                    }
                });

        String info = connect.sync().info("server");
        connect.close();
        assertThat(info).contains("tcp_port:" + port1);
    }

    @Test
    public void connectWithHelloClusterConnectionStrategy() throws Exception {

        DisqueURI disqueURI = DisqueURI.Builder.disque(host, port).build();

        DisqueConnection<String, String> connect = client.connect(new Utf8StringCodec(), disqueURI,
                SocketAddressSupplierFactory.Factories.HELLO_CLUSTER);

        // initial address
        assertThat(connect.sync().info("server")).contains("tcp_port:" + port);
        connect.sync().quit();

        // obtained from cluster, may be the same
        assertThat(connect.sync().info("server")).contains("tcp_port:" + port);
        connect.sync().quit();

        // obtained from cluster, second cluster node
        assertThat(connect.sync().info("server")).contains("tcp_port:" + TestSettings.port(1));

        connect.close();
    }

    @Test
    public void clusterConnectionTest() throws Exception {

        int port0 = TestSettings.port(0);
        int port1 = TestSettings.port(1);
        DisqueURI disqueURI = DisqueURI.Builder.disque(host, port0).withDisque(host, port1).build();

        DisqueConnection<String, String> connect = client.connect(disqueURI);
        assertThat(connect.sync().info("server")).contains("tcp_port:" + port0);
        connect.sync().quit();
        assertThat(connect.sync().info("server")).contains("tcp_port:" + port1);
        connect.close();
    }
}
