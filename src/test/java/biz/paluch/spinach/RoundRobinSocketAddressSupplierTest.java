package biz.paluch.spinach;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import biz.paluch.spinach.impl.RoundRobinSocketAddressSupplier;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RoundRobinSocketAddressSupplierTest {

    private static DisqueURI.DisqueHostAndPort hap1 = new DisqueURI.DisqueHostAndPort("127.0.0.1", 1);
    private static DisqueURI.DisqueHostAndPort hap2 = new DisqueURI.DisqueHostAndPort("127.0.0.1", 2);
    private static DisqueURI.DisqueHostAndPort hap3 = new DisqueURI.DisqueHostAndPort("127.0.0.1", 3);

    private Collection<DisqueURI.DisqueHostAndPort> points = Arrays.asList(hap1, hap2, hap3);

    @Test
    public void noOffset() throws Exception {

        RoundRobinSocketAddressSupplier sut = new RoundRobinSocketAddressSupplier(points, null);

        assertThat(sut.get()).isEqualTo(getSocketAddress(hap1));
        assertThat(sut.get()).isEqualTo(getSocketAddress(hap2));
        assertThat(sut.get()).isEqualTo(getSocketAddress(hap3));
        assertThat(sut.get()).isEqualTo(getSocketAddress(hap1));
    }

    @Test
    public void withOffset() throws Exception {

        RoundRobinSocketAddressSupplier sut = new RoundRobinSocketAddressSupplier(points, hap2);

        assertThat(sut.get()).isEqualTo(getSocketAddress(hap3));
        assertThat(sut.get()).isEqualTo(getSocketAddress(hap1));
        assertThat(sut.get()).isEqualTo(getSocketAddress(hap2));
        assertThat(sut.get()).isEqualTo(getSocketAddress(hap3));
    }

    private InetSocketAddress getSocketAddress(DisqueURI.DisqueHostAndPort hostAndPort) {
        return InetSocketAddress.createUnresolved(hostAndPort.getHost(), hostAndPort
                .getPort());
    }
}