package biz.paluch.spinach.commands;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import biz.paluch.spinach.DisqueClient;
import biz.paluch.spinach.DisqueURI;
import biz.paluch.spinach.TestSettings;
import biz.paluch.spinach.api.sync.DisqueCommands;

import com.lambdaworks.redis.RedisException;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class BasicCommandTest extends AbstractCommandTest {

    @Test
    public void hello() throws Exception {

        List<Object> result = disque.hello();
        // 1, nodeId, two nested nodes
        assertThat(result).hasSize(4);
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
                DisqueClient disqueClient = new DisqueClient(disqueURI);
                DisqueCommands<String, String> authConnection = disqueClient.connect().sync();
                authConnection.ping();
                authConnection.close();
                disqueClient.shutdown(100, 100, TimeUnit.MILLISECONDS);
            }

        };
    }
}
