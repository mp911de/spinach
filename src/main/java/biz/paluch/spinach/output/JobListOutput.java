package biz.paluch.spinach.output;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import biz.paluch.spinach.api.Job;

import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.protocol.CommandOutput;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class JobListOutput<K, V> extends CommandOutput<K, V, List<Job<K, V>>> implements SupportsObservables {

    private K defaultQueue;
    private K queue;
    private String id;
    private V body;
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

        body = codec.decodeValue(bytes);
    }

    @Override
    public void complete(int depth) {

        if (id != null && body != null) {
            Job<K, V> job = new Job<K, V>(queue, id, body);
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
