package biz.paluch.spinach;

import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.codec.Utf8StringCodec;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class DisqueClient extends AbstractRedisClient {
    private final RedisCodec<String, String> codec = new Utf8StringCodec();
    private final DisqueURI disqueURI;

    /**
     * Creates a uri-less DisqueClient. You can connect to different disque servers but you must supply a {@link DisqueURI} on
     * connecting. Methods without having a {@link DisqueURI} will fail with a {@link java.lang.IllegalStateException}.
     */
    public DisqueClient() {
        disqueURI = null;
        setDefaultTimeout(60, TimeUnit.MINUTES);
    }

    /**
     * Create a new client that connects to the supplied host on the default port.
     * 
     * @param host Server hostname.
     */
    public DisqueClient(String host) {
        this(host, DisqueURI.DEFAULT_DISQUE_PORT);
    }

    /**
     * Create a new client that connects to the supplied host and port. Connection attempts and non-blocking commands will
     * {@link #setDefaultTimeout timeout} after 60 seconds.
     * 
     * @param host Server hostname.
     * @param port Server port.
     */
    public DisqueClient(String host, int port) {
        this(DisqueURI.Builder.disque(host, port).build());
    }

    /**
     * Create a new client that connects to the supplied host and port. Connection attempts and non-blocking commands will
     * {@link #setDefaultTimeout timeout} after 60 seconds.
     * 
     * @param disqueURI Disque URI.
     */
    public DisqueClient(DisqueURI disqueURI) {
        super();
        this.disqueURI = disqueURI;
        setDefaultTimeout(disqueURI.getTimeout(), disqueURI.getUnit());
    }

}
