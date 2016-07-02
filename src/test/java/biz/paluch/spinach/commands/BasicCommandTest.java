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
package biz.paluch.spinach.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import biz.paluch.spinach.DisqueClient;
import biz.paluch.spinach.DisqueURI;
import biz.paluch.spinach.TestSettings;
import biz.paluch.spinach.api.sync.DisqueCommands;

import com.lambdaworks.redis.RedisException;

/**
 * @author Mark Paluch
 */
public class BasicCommandTest extends AbstractCommandTest {

    @Test
    public void hello() throws Exception {

        List<Object> result = disque.hello();
        // 1, nodeId, two nested nodes
        assertThat(result.size()).isGreaterThan(3);
    }

    @Test
    public void info() throws Exception {

        String result = disque.ping();
        assertThat(result).isNotEmpty();
    }

    @Test
    public void auth() throws Exception {
        new WithPasswordRequired() {
            @Override
            protected void run(DisqueClient client) throws Exception {
                DisqueCommands<String, String> connection = client.connect().sync();
                try {
                    connection.ping();
                    fail("Server doesn't require authentication");
                } catch (RedisException e) {
                    assertThat(e.getMessage()).isEqualTo("NOAUTH Authentication required.");
                    assertThat(connection.auth(passwd)).isEqualTo("OK");
                    assertThat(connection.ping()).isEqualTo("PONG");
                }

                DisqueURI disqueURI = DisqueURI.create("disque://" + passwd + "@" + TestSettings.host() + ":"
                        + TestSettings.port());
                DisqueClient disqueClient = DisqueClient.create(disqueURI);
                DisqueCommands<String, String> authConnection = disqueClient.connect().sync();
                authConnection.ping();
                authConnection.close();
                disqueClient.shutdown(100, 100, TimeUnit.MILLISECONDS);
            }

        };
    }
}
