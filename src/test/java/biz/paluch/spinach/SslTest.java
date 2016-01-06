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
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
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

        assumeTrue(JavaRuntime.AT_LEAST_JDK_7);
        DisqueURI disqueUri = DisqueURI.create("disques://" + host() + ":" + sslPort());

        disqueClient.connect(disqueUri);

    }

}