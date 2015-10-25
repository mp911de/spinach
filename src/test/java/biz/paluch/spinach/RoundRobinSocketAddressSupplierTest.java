package biz.paluch.spinach;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import biz.paluch.spinach.impl.RoundRobinSocketAddressSupplier;

import com.google.common.collect.ImmutableList;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RoundRobinSocketAddressSupplierTest {

    private static DisqueURI.DisqueHostAndPort hap1 = new DisqueURI.DisqueHostAndPort("127.0.0.1", 1);
    private static DisqueURI.DisqueHostAndPort hap2 = new DisqueURI.DisqueHostAndPort("127.0.0.1", 2);
    private static DisqueURI.DisqueHostAndPort hap3 = new DisqueURI.DisqueHostAndPort("127.0.0.1", 3);

    private Collection<DisqueURI.DisqueHostAndPort> points = ImmutableList.of(hap1, hap2, hap3);

    @BeforeClass
    public static void beforeClass() throws Exception {
        hap1.getResolvedAddress();
        hap2.getResolvedAddress();
        hap3.getResolvedAddress();
    }

    @Test
    public void noOffset() throws Exception {

        RoundRobinSocketAddressSupplier sut = new RoundRobinSocketAddressSupplier(points, null);

        assertThat(sut.get()).isEqualTo(hap1.getResolvedAddress());
        assertThat(sut.get()).isEqualTo(hap2.getResolvedAddress());
        assertThat(sut.get()).isEqualTo(hap3.getResolvedAddress());
        assertThat(sut.get()).isEqualTo(hap1.getResolvedAddress());
    }

    @Test
    public void withOffset() throws Exception {

        RoundRobinSocketAddressSupplier sut = new RoundRobinSocketAddressSupplier(points, hap2);

        assertThat(sut.get()).isEqualTo(hap3.getResolvedAddress());
        assertThat(sut.get()).isEqualTo(hap1.getResolvedAddress());
        assertThat(sut.get()).isEqualTo(hap2.getResolvedAddress());
        assertThat(sut.get()).isEqualTo(hap3.getResolvedAddress());
    }
}