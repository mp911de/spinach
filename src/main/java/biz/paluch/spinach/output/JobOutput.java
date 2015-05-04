package biz.paluch.spinach.output;

import java.nio.ByteBuffer;

import biz.paluch.spinach.Job;

import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.protocol.CommandOutput;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class JobOutput<K, V> extends CommandOutput<K, V, Job<K, V>> {

    private K queue;
    private String id;
    private V body;

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
            body = codec.decodeValue(bytes);
            return;
        }
    }

    @Override
    public void complete(int depth) {
        if (queue != null && id != null && body != null) {
            output = new Job<K, V>(queue, id, body);
        }
    }
}
