package biz.paluch.spinach;

import static biz.paluch.spinach.TestSettings.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.DisqueConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ClusterConnectionTest {

    private static DisqueClient disqueClient;

    @BeforeClass
    public static void beforeClass() {

        DisqueURI disqueURI = new DisqueURI.Builder().withDisque(host(), port()).withDisque(host(), port(1)).build();
        disqueClient = new DisqueClient(disqueURI);
    }

    @AfterClass
    public static void afterClass() {
        disqueClient.shutdown(5, 5, TimeUnit.MILLISECONDS);
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
