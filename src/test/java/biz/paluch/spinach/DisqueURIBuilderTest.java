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

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.lambdaworks.redis.ConnectionPoint;

public class DisqueURIBuilderTest {

    @Test
    public void newUri() throws Exception {
        DisqueURI result = new DisqueURI("a", 1, 2, TimeUnit.DAYS);
        assertThat(result.getConnectionPoints()).hasSize(1);

        assertThat(result.getUnit()).isEqualTo(TimeUnit.DAYS);
        assertThat(result.getTimeout()).isEqualTo(2);
        assertThat(result.isSsl()).isFalse();

        assertThat(result.isSsl()).isFalse();
        assertThat(result.isStartTls()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPort0() throws Exception {
        DisqueURI.create("host", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPort65536() throws Exception {
        DisqueURI.create("host", 65536);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyHost() throws Exception {
        DisqueURI.create("", 44);
    }

    @Test
    public void programmatic() throws Exception {
        DisqueURI uri = new DisqueURI();
        uri.setSsl(true);
        uri.setStartTls(true);

        DisqueURI.DisqueHostAndPort hap = new DisqueURI.DisqueHostAndPort();
        hap.setHost("host");
        hap.setPort(42);

        assertThat(hap.toString()).contains("host:42");

        DisqueURI.DisqueSocket socket = new DisqueURI.DisqueSocket();
        socket.setSocket("/path");

        assertThat(socket.toString()).contains("/path");

        uri.getConnectionPoints().add(hap);
        uri.getConnectionPoints().add(socket);

        assertThat(uri.getConnectionPoints()).hasSize(2);
    }

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
    public void disqueSslFromBuilder() throws Exception {
        DisqueURI result = DisqueURI.Builder.disque("host").withSsl(true).withStartTls(false).withVerifyPeer(true).build();

        assertThat(result.isSsl()).isTrue();
        assertThat(result.isStartTls()).isFalse();
        assertThat(result.isVerifyPeer()).isTrue();
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
