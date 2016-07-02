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
import static biz.paluch.spinach.TestSettings.port;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.support.DefaultDisqueClient;
import biz.paluch.spinach.support.FastShutdown;

/**
 * @author Mark Paluch
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
