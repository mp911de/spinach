package biz.paluch.spinach.support;

import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.resource.ClientResources;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class FastShutdown {

    /**
     * Shut down a {@link AbstractRedisClient} with a timeout of 10ms.
     * 
     * @param redisClient
     */
    public static void shutdown(AbstractRedisClient redisClient) {
        redisClient.shutdown(10, 10, TimeUnit.MILLISECONDS);
    }

    /**
     * Shut down a {@link ClientResources} client with a timeout of 10ms.
     *
     * @param clientResources
     */
    public static void shutdown(ClientResources clientResources) {
        clientResources.shutdown(10, 10, TimeUnit.MILLISECONDS);
    }
}
