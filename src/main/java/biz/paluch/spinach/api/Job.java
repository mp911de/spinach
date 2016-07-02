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
package biz.paluch.spinach.api;

import java.util.Map;

/**
 * Disque Job data structure.
 *
 * @author Mark Paluch
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
