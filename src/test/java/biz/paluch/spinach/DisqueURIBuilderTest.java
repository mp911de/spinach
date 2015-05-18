package biz.paluch.spinach;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.ConnectionPoint;
import org.junit.Test;

public class DisqueURIBuilderTest {

    @Test
    public void disque() throws Exception {
        DisqueURI result = DisqueURI.Builder.disque("localhost").withTimeout(2, TimeUnit.HOURS).build();
        assertThat(result.getConnectionPoints()).hasSize(1);
        assertThat(result.getTimeout()).isEqualTo(2);
        assertThat(result.getUnit()).isEqualTo(TimeUnit.HOURS);
    }

    @Test
    public void disqueWithPort() throws Exception {
        DisqueURI result = DisqueURI.Builder.disque("localhost", 1).withTimeout(2, TimeUnit.HOURS).build();
        assertThat(result.getConnectionPoints()).hasSize(1);
        assertThat(result.getTimeout()).isEqualTo(2);
        assertThat(result.getUnit()).isEqualTo(TimeUnit.HOURS);
    }

    @Test
    public void disqueFromUrl() throws Exception {
        DisqueURI result = DisqueURI.create(DisqueURI.URI_SCHEME_DISQUE + "://password@localhost/1");

        assertThat(result.getConnectionPoints()).hasSize(1);
        ConnectionPoint cp = result.getConnectionPoints().get(0);

        assertThat(cp.getHost()).isEqualTo("localhost");
        assertThat(cp.getPort()).isEqualTo(DisqueURI.DEFAULT_DISQUE_PORT);
        assertThat(result.getPassword()).isEqualTo("password".toCharArray());
        assertThat(result.isSsl()).isFalse();
    }

    @Test
    public void disqueSocketFromUrl() throws Exception {
        DisqueURI result = DisqueURI.create(DisqueURI.URI_SCHEME_DISQUE_SOCKET + "://password@/var/tmp");

        assertThat(result.getConnectionPoints()).hasSize(1);
        ConnectionPoint cp = result.getConnectionPoints().get(0);

        assertThat(cp.getHost()).isNull();
        assertThat(cp.getPort()).isEqualTo(-1);
        assertThat(cp.getSocket()).isEqualTo("/var/tmp");
        assertThat(result.getPassword()).isEqualTo("password".toCharArray());
    }

    @Test
    public void disqueSslFromUrl() throws Exception {
        DisqueURI result = DisqueURI.create(DisqueURI.URI_SCHEME_DISQUE_SECURE + "://:password@localhost/1");

        assertThat(result.getConnectionPoints()).hasSize(1);
        ConnectionPoint cp = result.getConnectionPoints().get(0);

        assertThat(cp.getHost()).isEqualTo("localhost");
        assertThat(cp.getPort()).isEqualTo(DisqueURI.DEFAULT_DISQUE_PORT);
        assertThat(result.getPassword()).isEqualTo("password".toCharArray());
        assertThat(result.isSsl()).isTrue();
    }

    @Test
    public void multipleDisqueFromUrl() throws Exception {
        DisqueURI result = DisqueURI.create(DisqueURI.URI_SCHEME_DISQUE + "://password@localhost/1");

        assertThat(result.getConnectionPoints()).hasSize(1);
        ConnectionPoint hap = result.getConnectionPoints().get(0);

        assertThat(hap.getHost()).isEqualTo("localhost");
        assertThat(hap.getPort()).isEqualTo(DisqueURI.DEFAULT_DISQUE_PORT);
        assertThat(result.getPassword()).isEqualTo("password".toCharArray());

        result = DisqueURI.create(DisqueURI.URI_SCHEME_DISQUE + "://password@host1:1,host2:3423,host3/1");

        assertThat(result.getConnectionPoints()).hasSize(3);

        assertThat(result.getPassword()).isEqualTo("password".toCharArray());

        ConnectionPoint cp1 = result.getConnectionPoints().get(0);
        assertThat(cp1.getPort()).isEqualTo(1);
        assertThat(cp1.getHost()).isEqualTo("host1");

        ConnectionPoint cp2 = result.getConnectionPoints().get(1);
        assertThat(cp2.getPort()).isEqualTo(3423);
        assertThat(cp2.getHost()).isEqualTo("host2");

        ConnectionPoint cp3 = result.getConnectionPoints().get(2);
        assertThat(cp3.getPort()).isEqualTo(DisqueURI.DEFAULT_DISQUE_PORT);
        assertThat(cp3.getHost()).isEqualTo("host3");

    }

}