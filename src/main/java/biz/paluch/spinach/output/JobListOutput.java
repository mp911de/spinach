package biz.paluch.spinach.output;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lambdaworks.redis.output.CommandOutput;
import rx.Subscriber;
import biz.paluch.spinach.api.Job;

import com.lambdaworks.redis.codec.RedisCodec;

/**
 * Output handler for commands returning a {@link List} of {@link Job Jobs} data structres.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class JobListOutput<K, V> extends CommandOutput<K, V, List<Job<K, V>>> implements SupportsObservables {

    private K defaultQueue;
    private K queue;
    private String id;
    private V body;
    private Map<String, Long> counters = new HashMap<String, Long>();
    private String lastKey;
    private Subscriber<Object> subscriber;

    public JobListOutput(RedisCodec<K, V> codec) {
        super(codec, new ArrayList<Job<K, V>>());
    }

    public JobListOutput(RedisCodec<K, V> codec, K defaultQueue) {
        super(codec, new ArrayList<Job<K, V>>());
        this.defaultQueue = defaultQueue;
    }

    @Override
    public void set(ByteBuffer bytes) {

        if (queue == null) {
            if (defaultQueue != null) {
                queue = defaultQueue;
            } else {
                queue = codec.decodeKey(bytes);
                return;
            }
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
        if (lastKey != null) {
            counters.put(lastKey, integer);
            lastKey = null;
        }
    }

    @Override
    public void complete(int depth) {

        if (id != null && body != null && depth == 1) {
            Job<K, V> job = new Job<K, V>(queue, id, body, counters);
            if (subscriber != null && !subscriber.isUnsubscribed()) {
                subscriber.onNext(job);
            }

            output.add(job);

            queue = null;
            id = null;
            body = null;
        }

    }

    @Override
    public <T> void setSubscriber(Subscriber<T> subscriber) {
        this.subscriber = (Subscriber<Object>) subscriber;
    }
}
