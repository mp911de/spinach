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

import static biz.paluch.spinach.TestSettings.host;
import static biz.paluch.spinach.TestSettings.sslPort;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import biz.paluch.spinach.support.DefaultDisqueClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import biz.paluch.spinach.api.sync.DisqueCommands;
import biz.paluch.spinach.support.FastShutdown;

import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.JavaRuntime;
import com.lambdaworks.redis.RedisConnectionException;
import com.lambdaworks.redis.resource.ClientResources;

/**
 * @author Mark Paluch
 */
public class SslTest {
    public static final String KEYSTORE = "work/keystore.jks";
    public static ClientResources clientResources =  DefaultDisqueClient.getClientResources();
    public static DisqueClient disqueClient = DisqueClient.create(clientResources);

    @Before
    public void before() throws Exception {
        assumeTrue("Assume that stunnel runs on port 7443", Sockets.isOpen(host(), sslPort()));
        assertThat(new File(KEYSTORE)).exists();
        System.setProperty("javax.net.ssl.trustStore", KEYSTORE);
    }

    @AfterClass
    public static void afterClass() {
        FastShutdown.shutdown(disqueClient);
    }

    @Test
    public void regularSsl() throws Exception {
        DisqueURI disqueUri = DisqueURI.Builder.disque(host(), sslPort()).withSsl(true).withVerifyPeer(false).build();

        DisqueCommands<String, String> connection = disqueClient.connect(disqueUri).sync();

        assertThat(connection.ping()).isEqualTo("PONG");

        connection.close();
    }

    @Test
    public void pingBeforeActivate() throws Exception {
        DisqueURI disqueUri = DisqueURI.Builder.disque(host(), sslPort()).withSsl(true).withVerifyPeer(false).build();
        disqueClient.setOptions(new ClientOptions.Builder().pingBeforeActivateConnection(true).build());

        DisqueCommands<String, String> connection = disqueClient.connect(disqueUri).sync();

        assertThat(connection.ping()).isEqualTo("PONG");

        connection.close();
    }

    @Test
    public void regularSslWithReconnect() throws Exception {
        DisqueURI disqueUri = DisqueURI.Builder.disque(host(), sslPort()).withSsl(true).withVerifyPeer(false).build();

        DisqueCommands<String, String> connection = disqueClient.connect(disqueUri).sync();
        assertThat(connection.ping()).isEqualTo("PONG");
        connection.quit();
        assertThat(connection.ping()).isEqualTo("PONG");
        connection.close();
    }

    @Test(expected = RedisConnectionException.class)
    public void sslWithVerificationWillFail() throws Exception {

        assumeTrue(JavaRuntime.AT_LEAST_JDK_8);
        DisqueURI disqueUri = DisqueURI.create("disques://" + host() + ":" + sslPort());

        disqueClient.connect(disqueUri);

    }

}