package biz.paluch.spinach;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.TimeUnit;

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
    public void redisFromUrl() throws Exception {
        DisqueURI result = DisqueURI.create(DisqueURI.URI_SCHEME_DISQUE + "://password@localhost/1");

        assertThat(result.getConnectionPoints()).hasSize(1);
        DisqueURI.DisqueHostAndPort hap = result.getConnectionPoints().get(0);

        assertThat(hap.getHost()).isEqualTo("localhost");
        assertThat(hap.getPort()).isEqualTo(DisqueURI.DEFAULT_DISQUE_PORT);
        assertThat(result.getPassword()).isEqualTo("password".toCharArray());
        assertThat(result.isSsl()).isFalse();
    }

    @Test
    public void redisSslFromUrl() throws Exception {
        DisqueURI result = DisqueURI.create(DisqueURI.URI_SCHEME_DISQUE_SECURE + "://:password@localhost/1");

        assertThat(result.getConnectionPoints()).hasSize(1);
        DisqueURI.DisqueHostAndPort hap = result.getConnectionPoints().get(0);

        assertThat(hap.getHost()).isEqualTo("localhost");
        assertThat(hap.getPort()).isEqualTo(DisqueURI.DEFAULT_DISQUE_PORT);
        assertThat(result.getPassword()).isEqualTo("password".toCharArray());
        assertThat(result.isSsl()).isTrue();
    }

    @Test
    public void disqueFromUrl() throws Exception {
        DisqueURI result = DisqueURI.create(DisqueURI.URI_SCHEME_DISQUE + "://password@localhost/1#master");

        assertThat(result.getConnectionPoints()).hasSize(1);
        DisqueURI.DisqueHostAndPort hap = result.getConnectionPoints().get(0);

        assertThat(hap.getHost()).isEqualTo("localhost");
        assertThat(hap.getPort()).isEqualTo(DisqueURI.DEFAULT_DISQUE_PORT);
        assertThat(result.getPassword()).isEqualTo("password".toCharArray());

        result = DisqueURI.create(DisqueURI.URI_SCHEME_DISQUE + "://password@host1:1,host2:3423,host3/1#master");

        assertThat(result.getConnectionPoints()).hasSize(3);

        assertThat(result.getPassword()).isEqualTo("password".toCharArray());

        DisqueURI.DisqueHostAndPort sentinel1 = result.getConnectionPoints().get(0);
        assertThat(sentinel1.getPort()).isEqualTo(1);
        assertThat(sentinel1.getHost()).isEqualTo("host1");

        DisqueURI.DisqueHostAndPort sentinel2 = result.getConnectionPoints().get(1);
        assertThat(sentinel2.getPort()).isEqualTo(3423);
        assertThat(sentinel2.getHost()).isEqualTo("host2");

        DisqueURI.DisqueHostAndPort sentinel3 = result.getConnectionPoints().get(2);
        assertThat(sentinel3.getPort()).isEqualTo(DisqueURI.DEFAULT_DISQUE_PORT);
        assertThat(sentinel3.getHost()).isEqualTo("host3");

    }

}