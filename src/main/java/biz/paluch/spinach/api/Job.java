package biz.paluch.spinach.api;

import java.util.Map;

/**
 * Disque Job data structure.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @param <K> Queue-Id Type.
 * @param <V> Body-Id Type.
 */
public class Job<K, V> {
    private K queue;
    private String id;
    private V body;
    private Map<String, Long> counters;

    protected Job() {
    }

    public Job(K queue, String id, V body, Map<String, Long> counters) {
        this.queue = queue;
        this.id = id;
        this.body = body;
        this.counters = counters;
    }

    /**
     *
     * @return the queue
     */
    public K getQueue() {
        return queue;
    }

    /**
     *
     * @return the JobId
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return the Job body
     */
    public V getBody() {
        return body;
    }

    /**
     * If requested with a WITHCOUNTERS flag, getjob also populates a counters field.
     *
     * @return map of counters
     */
    public Map<String, Long> getCounters() { return counters; }
}
