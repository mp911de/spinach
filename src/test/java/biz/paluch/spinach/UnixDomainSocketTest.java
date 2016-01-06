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
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
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
