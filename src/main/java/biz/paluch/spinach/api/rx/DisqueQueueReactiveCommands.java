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
package biz.paluch.spinach.api.rx;

import java.util.Map;

import rx.Observable;
import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.api.PauseArgs;
import biz.paluch.spinach.api.QScanArgs;

import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.ScanCursor;

/**
 * Reactive commands related with Disque Queues.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author Mark Paluch
 */
public interface DisqueQueueReactiveCommands<K, V> {

    /**
     * Remove the job from the queue.
     * 
     * @param jobIds the job Id's
     * @return the number of jobs actually moved from queue to active state
     */
    Observable<Long> dequeue(String... jobIds);

    /**
     * Queue jobs if not already queued.
     * 
     * @param jobIds the job Id's
     * @return the number of jobs actually move from active to queued state
     */
    Observable<Long> enqueue(String... jobIds);

    /**
     * Queue jobs if not already queued and increment the nack counter.
     *
     * @param jobIds the job Id's
     * @return the number of jobs actually move from active to queued state
     */
    Observable<Long> nack(String... jobIds);

    /**
     * Change the {@literal PAUSE} pause state to:
     * <ul>
     * <li>Pause a queue</li>
     * <li>Clear the pause state for a queue</li>
     * <li>Query the pause state</li>
     * <li>Broadcast the pause state</li>
     * </ul>
     * 
     * @param queue the queue name
     * @param pauseArgs the pause args
     * @return pause state of the queue.
     */
    Observable<String> pause(K queue, PauseArgs pauseArgs);

    /**
     * Return the number of jobs queued.
     * 
     * @param queue the queue name
     * @return the number of jobs queued
     */
    Observable<Long> qlen(K queue);

    /**
     * Return an array of at most "count" jobs available inside the queue "queue" without removing the jobs from the queue. This
     * is basically an introspection and debugging command.
     * 
     * @param queue the queue name
     * @param count number of jobs to return
     * @return List of jobs.
     */
    Observable<Job<K, V>> qpeek(K queue, long count);

    /**
     * Incrementally iterate the keys space.
     *
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<K>> qscan();

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<K>> qscan(QScanArgs scanArgs);

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<K>> qscan(ScanCursor scanCursor);

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<K>> qscan(ScanCursor scanCursor, QScanArgs scanArgs);

    /**
     * Retrieve information about a queue as key value pairs.
     * 
     * @param queue the queue name
     * @return map containing the statistics (key value pairs)
     */
    Observable<Map<String, Object>> qstat(K queue);

    /**
     * If the job is queued, remove it from queue and change state to active. Postpone the job requeue time in the future so
     * that we'll wait the retry time before enqueueing again.
     * 
     * * Return how much time the worker likely have before the next requeue event or an error:
     * <ul>
     * <li>-ACKED: The job is already acknowledged, so was processed already.</li>
     * <li>-NOJOB We don't know about this job. The job was either already acknowledged and purged, or this node never received
     * a copy.</li>
     * <li>-TOOLATE 50% of the job TTL already elapsed, is no longer possible to delay it.</li>
     * </ul>
     *
     * @param jobId the job Id
     * @return retry count.
     */
    Observable<Long> working(String jobId);

}
