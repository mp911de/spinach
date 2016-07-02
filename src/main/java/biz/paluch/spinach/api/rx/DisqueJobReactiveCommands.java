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

import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.GetJobArgs;
import rx.Observable;
import biz.paluch.spinach.api.AddJobArgs;
import biz.paluch.spinach.api.JScanArgs;
import biz.paluch.spinach.api.Job;

import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.ScanCursor;

/**
 * Reactive commands related with Disque Jobs.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 * @author Mark Paluch
 */
public interface DisqueJobReactiveCommands<K, V> {

    /**
     * Set job state as acknowledged, if the job does not exist creates a fake job just to hold the acknowledge.
     *
     * @param jobIds the job Id's
     * @return the number of jobs already known and that were already not in the ACKED state.
     */
    Observable<Long> ackjob(String... jobIds);

    /**
     * Add a job to the {@code queue} with the body {@code job}.
     *
     * @param queue the queue name
     * @param job job body
     * @param commandTimeout command timeout to reach replication level
     * @param timeUnit command timeout unit
     * @return the job id
     */
    Observable<String> addjob(K queue, V job, long commandTimeout, TimeUnit timeUnit);

    /**
     *
     * Add a job to the {@code queue} with the body {@code job}.
     *
     * @param queue the queue name
     * @param job job body
     * @param commandTimeout command timeout to reach replication level
     * @param timeUnit command timeout unit
     * @param addJobArgs additional job arguments
     * @return the job id
     */
    Observable<String> addjob(K queue, V job, long commandTimeout, TimeUnit timeUnit, AddJobArgs addJobArgs);

    /**
     * Evict (and possibly remove from queue) all the jobs in memeory matching the specified job IDs. Jobs are evicted whatever
     * their state is, since this command is mostly used inside the AOF or for debugging purposes.
     *
     * @param jobIds the job Id's
     * @return the number of jobs evicted
     */
    Observable<Long> deljob(String... jobIds);

    /**
     * Performs a fast acknowledge of the specified jobs.
     *
     * @param jobIds the job Id's
     * @return the number of jobs that are deleted from the local node as a result of receiving the command
     */
    Observable<Long> fastack(String... jobIds);

    /**
     * Get jobs from the specified queue. If there are no jobs in any of the specified queues the command will block the
     * connection. If timeout is given, the command will block until given timeout and return null.
     *
     * @param getJobArgs job arguments
     * @param queues the queue name
     * @return the job or null
     */
    Observable<Job<K, V>> getjob(GetJobArgs getJobArgs, K... queues);

    /**
     * Get jobs from the specified queue. If there are no jobs in any of the specified queues the command will block the
     * connection.
     *
     * @param queues the queue names
     * @return the job or null
     */
    Observable<Job<K, V>> getjob(K... queues);

    /**
     * Get jobs from the specified queue. If there are no jobs in any of the specified queues the command will block the
     * connection. If timeout is given, the command will block until given timeout and return null.
     *
     * @param timeout max timeout to wait
     * @param timeUnit timeout unit
     * @param queues the queue names
     * @return the job or null
     */
    Observable<Job<K, V>> getjob(long timeout, TimeUnit timeUnit, K... queues);

    /**
     * Get jobs from the specified queue. If there are no jobs in any of the specified queues the command will block until
     * timeout unless {@link GetJobArgs#getNoHang()} option is passed.
     * <p>
     * When there are jobs in more than one of the queues, the command guarantees to return jobs in the order the queues are
     * specified. If {@code count} allows more jobs to be returned, queues are scanned again and again in the same order popping
     * more elements.
     * </p>
     * <p>
     * If there are not enough jobs in any of the specified queues the command will return less than {@code count} jobs after
     * the timeout.
     * </p>
     *
     * @param getJobArgs job arguments
     * @param count count of jobs to return
     * @param queues the queue names
     * @return the jobs
     */
    Observable<Job<K, V>> getjobs(GetJobArgs getJobArgs, long count, K... queues);

    /**
     * Get jobs from the specified queues. If there are no jobs in any of the specified queues the command will block the
     * connection.
     *
     * @param queues the queue names
     * @return the jobs
     */
    Observable<Job<K, V>> getjobs(K... queues);

    /**
     * Get jobs from the specified queues. If there are no jobs in any of the specified queues the command will block the
     * connection.
     * <p>
     * When there are jobs in more than one of the queues, the command guarantees to return jobs in the order the queues are
     * specified. If {@code count} allows more jobs to be returned, queues are scanned again and again in the same order popping
     * more elements.
     * </p>
     * <p>
     * If there are not enough jobs in any of the specified queues the command will return less than {@code count} jobs after
     * the timeout.
     * </p>
     * 
     * @param timeout timeout to wait
     * @param timeUnit timeout unit
     * @param count count of jobs to return
     * @param queues the queue names
     * @return the jobs
     */
    Observable<Job<K, V>> getjobs(long timeout, TimeUnit timeUnit, long count, K... queues);

    /**
     * Incrementally iterate all the existing queues in the local node returning the job id's.
     *
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<String>> jscan();

    /**
     * Incrementally iterate all the existing queues in the local node returning the job id's.
     *
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<String>> jscan(JScanArgs<K> scanArgs);

    /**
     * Incrementally iterate all the existing queues in the local node returning the job id's.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<String>> jscan(ScanCursor scanCursor);

    /**
     * Incrementally iterate all the existing queues in the local node returning the job id's.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<String>> jscan(ScanCursor scanCursor, JScanArgs<K> scanArgs);

    /**
     * Describes a job without changing its state.
     *
     * @param jobId the job Id's
     * @return bulk-reply
     */
    Observable<List<Object>> show(String jobId);

}
