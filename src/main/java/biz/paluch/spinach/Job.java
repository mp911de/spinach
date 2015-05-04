package biz.paluch.spinach;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @param <K> Queue-Id Type.
 * @param <V> Body-Id Type.
 */
public class Job<K, V> {
    private K queue;
    private String id;
    private V body;

    protected Job() {
    }

    public Job(K queue, String id, V body) {
        this.queue = queue;
        this.id = id;
        this.body = body;
    }

    public K getQueue() {
        return queue;
    }

    public String getId() {
        return id;
    }

    public V getBody() {
        return body;
    }
}
