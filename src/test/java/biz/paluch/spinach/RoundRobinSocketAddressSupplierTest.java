/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.paluch.spinach;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import biz.paluch.spinach.impl.RoundRobinSocketAddressSupplier;

/**
 * @author Mark Paluch
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