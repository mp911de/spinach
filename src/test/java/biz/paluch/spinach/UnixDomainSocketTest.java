package biz.paluch.spinach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.*;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.sync.DisqueCommands;
import org.apache.log4j.Logger;
import org.junit.Test;

import io.netty.util.internal.SystemPropertyUtil;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class UnixDomainSocketTest {

    protected Logger log = Logger.getLogger(getClass());

    private DisqueCommands<String, String> connection;

    @Test
    public void linux_x86_64_socket() throws Exception {

        linuxOnly();

        DisqueClient disqueClient = new DisqueClient(DisqueURI.Builder.disqueSocket(TestSettings.socket()).build());

        DisqueCommands<String, String> connection = disqueClient.connect().sync();

        connection.debugFlushall();
        connection.ping();

        disqueClient.shutdown(0, 0, TimeUnit.SECONDS);
    }

    @Test
    public void differentSocketTypes() throws Exception {

        DisqueClient disqueClient = new DisqueClient(DisqueURI.Builder.disqueSocket(TestSettings.socket())
                .withDisque(TestSettings.host()).build());

        try {
            disqueClient.connect();
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("You cannot mix unix");
        }

        disqueClient.shutdown(0, 0, TimeUnit.SECONDS);
    }

    private void linuxOnly() {
        String osName = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
        assumeTrue("Only supported on Linux, your os is " + osName, osName.startsWith("linux"));
    }

}
