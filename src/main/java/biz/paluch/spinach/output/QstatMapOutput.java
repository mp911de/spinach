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
import java.util.*;

import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.output.CommandOutput;

/**
 * @author Mark Paluch
 */
public class QstatMapOutput<K, V> extends CommandOutput<K, V, Map<String, Object>> {
    private final Deque<List<Object>> stack;
    private int depth;
    private String key;

    @SuppressWarnings("rawtypes")
    public QstatMapOutput(RedisCodec<K, V> codec) {
        super(codec, new HashMap<String, Object>());
        stack = new LinkedList<List<Object>>();
        depth = 0;
    }

    @Override
    public void set(ByteBuffer bytes) {
        if (stack.isEmpty()) {
            if (key == null) {
                key = decodeAscii(bytes);
                return;
            }

            Object value = (bytes == null) ? null : key.equals("queue") ? codec.decodeKey(bytes) : decodeAscii(bytes);
            output.put(key, value);
            key = null;
        } else {
            stack.peek().add(bytes == null ? null : decodeAscii(bytes));
        }
    }

    @Override
    public void set(long integer) {
        if (stack.isEmpty()) {
            if (key == null) {
                key = "";
                return;
            }

            output.put(key, Long.valueOf(integer));
            key = null;
        } else {
            stack.peek().add(integer);
        }
    }

    @Override
    public void complete(int depth) {
        if (depth < this.depth) {
            if (!stack.isEmpty()) {
                output.put(key, stack.pop());
                key = null;
            }
            this.depth--;
        }
    }

    @Override
    public void multi(int count) {
        this.depth++;
        if (depth > 1) {
            List<Object> a = new ArrayList<Object>(count);
            stack.push(a);
        }
    }
}
