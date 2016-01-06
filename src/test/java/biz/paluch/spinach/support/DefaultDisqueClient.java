package biz.paluch.spinach.support;

import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.DisqueClient;
import biz.paluch.spinach.DisqueURI;
import biz.paluch.spinach.TestSettings;

import com.lambdaworks.redis.resource.ClientResources;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class DefaultDisqueClient {

    public final static DefaultDisqueClient instance = new DefaultDisqueClient();

    private DisqueClient disqueClient;
    private ClientResources clientResources;

    public DefaultDisqueClient() {
        clientResources = TestClientResources.create();
        disqueClient = DisqueClient.create(clientResources, DisqueURI.Builder.disque(TestSettings.host(), TestSettings.port())
                .build());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                FastShutdown.shutdown(disqueClient);
            }
        });
    }

    /**
     * Do not close the client.
     * 
     * @return the default disque client for the tests.
     */
    public static DisqueClient get() {
        instance.disqueClient.setDefaultTimeout(60, TimeUnit.SECONDS);
        return instance.disqueClient;
    }

    /**
     * Do not close the client resources.
     * @return the default client resources for the tests.
     */
    public static ClientResources getClientResources() {
        return instance.clientResources;
    }
}
