package biz.paluch.spinach.output;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import biz.paluch.spinach.api.Job;

import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.protocol.CommandOutput;

/**
 * Output handler for commands returning {@link Job} data structres.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class JobOutput<K, V> extends CommandOutput<K, V, Job<K, V>> {

    private K queue;
    private String id;
    private V body;
    private Map<String, Long> counters;
    private String lastKey;

    public JobOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(ByteBuffer bytes) {

        if (queue == null) {
            queue = codec.decodeKey(bytes);
            return;
        }

        if (id == null) {
            id = decodeAscii(bytes);
            return;
        }

        if (body == null) {
            counters = new HashMap<String, Long>();
            body = codec.decodeValue(bytes);
            return;
        }

        lastKey = decodeAscii(bytes);
    }

    @Override
    public void set(long integer) {
        counters.put(lastKey, integer);
    }

    @Override
    public void complete(int depth) {
        if (queue != null && id != null && body != null && counters != null) {
            output = new Job<K, V>(queue, id, body, counters);
        }
    }
}
