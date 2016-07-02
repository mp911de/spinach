/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * @author Mark Paluch
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
