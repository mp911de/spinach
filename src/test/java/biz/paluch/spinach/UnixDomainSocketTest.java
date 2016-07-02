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
import static org.junit.Assume.assumeTrue;

import java.util.Locale;

import biz.paluch.spinach.support.DefaultDisqueClient;
import org.junit.Test;

import biz.paluch.spinach.api.sync.DisqueCommands;
import biz.paluch.spinach.commands.AbstractCommandTest;
import biz.paluch.spinach.support.FastShutdown;

import com.lambdaworks.redis.resource.ClientResources;
import io.netty.util.internal.SystemPropertyUtil;

/**
 * @author Mark Paluch
 */
public class UnixDomainSocketTest extends AbstractCommandTest {

    private static ClientResources clientResources = DefaultDisqueClient.getClientResources();

    @Test
    public void linux_x86_64_socket() throws Exception {

        linuxOnly();

        DisqueClient disqueClient = DisqueClient.create(clientResources, DisqueURI.Builder.disqueSocket(TestSettings.socket())
                .build());

        DisqueCommands<String, String> connection = disqueClient.connect().sync();

        connection.debugFlushall();
        connection.ping();

        FastShutdown.shutdown(disqueClient);
    }

    @Test
    public void differentSocketTypes() throws Exception {

        DisqueClient disqueClient = DisqueClient.create(clientResources, DisqueURI.Builder.disqueSocket(TestSettings.socket())
                .withDisque(TestSettings.host()).build());

        try {
            disqueClient.connect();
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("You cannot mix unix");
        }

        FastShutdown.shutdown(disqueClient);
    }

    private void linuxOnly() {
        String osName = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
        assumeTrue("Only supported on Linux, your os is " + osName, osName.startsWith("linux"));
    }

}
