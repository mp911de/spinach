package biz.paluch.spinach.output;

import java.nio.ByteBuffer;
import java.util.*;

import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.output.CommandOutput;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
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
