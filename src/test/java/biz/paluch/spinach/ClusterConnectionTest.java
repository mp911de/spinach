package biz.paluch.spinach;

import static biz.paluch.spinach.TestSettings.host;
import static biz.paluch.spinach.TestSettings.port;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.support.DefaultDisqueClient;
import biz.paluch.spinach.support.FastShutdown;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ClusterConnectionTest {

    private static DisqueClient disqueClient;

    @BeforeClass
    public static void beforeClass() {

        DisqueURI disqueURI = new DisqueURI.Builder().withDisque(host(), port()).withDisque(host(), port(1)).build();
        disqueClient = DisqueClient.create(DefaultDisqueClient.getClientResources(), disqueURI);
    }

    @AfterClass
    public static void afterClass() {
        FastShutdown.shutdown(disqueClient);
    }

    @Test
    public void connect() throws Exception {
        DisqueConnection<String, String> connection = disqueClient.connect();

        assertThat(connection.sync().info()).contains("tcp_port:" + port());
        connection.sync().quit();
        assertThat(connection.sync().info()).contains("tcp_port:" + port(1));

        assertThat(connection.isOpen()).isTrue();

        connection.close();
    }
}
