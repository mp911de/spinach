package biz.paluch.spinach.api.rx;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueJobReactiveCommands<K, V> {

    /**
     * Add job tot the {@code queue} with the body of {@code job}
     * 
     * @param queue the target queue
     * @param job job body
     * @param timeout TTL timeout
     * @param timeUnit TTL timeout time unit
     * @return the job id
     */
    Observable<String> addjob(K queue, V job, long timeout, TimeUnit timeUnit);

    /**
     *
     * Add job tot the {@code queue} with the body of {@code job}
     * 
     * @param queue the target queue
     * @param job job body
     * @param timeout TTL timeout
     * @param timeUnit TTL timeout time unit
     * @param addJobArgs additional job arguments
     * @return the job id
     */
    Observable<String> addjob(K queue, V job, long timeout, TimeUnit timeUnit, AddJobArgs addJobArgs);

    /**
     * Get jobs from the specified queue. By default COUNT is 1, so just one job will be returned. If there are no jobs in any
     * of the specified queues the command will block.
     *
     * @param queue the queue
     * @return the job
     */
    Observable<Job<K, V>> getjob(K queue);

    /**
     * Get jobs from the specified queue. By default COUNT is 1, so just one job will be returned. If there are no jobs in any
     * of the specified queues the command will block.
     *
     * @param timeout timeout to wait
     * @param timeUnit timeout unit
     * @param queue the queue
     * @return the job.
     */
    Observable<Job<K, V>> getjob(long timeout, TimeUnit timeUnit, K queue);

    /**
     * Get jobs from the specified queues. By default COUNT is 1, so just one job will be returned. If there are no jobs in any
     * of the specified queues the command will block.
     *
     * When there are jobs in more than one of the queues, the command guarantees to return jobs in the order the queues are
     * specified. If COUNT allows more jobs to be returned, queues are scanned again and again in the same order popping more
     * elements.
     * 
     * @param queues queue names
     * @return the jobs
     */
    Observable<Job<K, V>> getjobs(K... queues);

    /**
     * Get jobs from the specified queues. If there are no jobs in any of the specified queues the command will block.
     *
     * When there are jobs in more than one of the queues, the command guarantees to return jobs in the order the queues are
     * specified. If COUNT allows more jobs to be returned, queues are scanned again and again in the same order popping more
     * elements.
     * 
     * @param timeout timeout to wait
     * @param timeUnit timeout unit
     * @param count count of jobs to return
     * @param queues queue names
     * @return the jobs
     */
    Observable<Job<K, V>> getjobs(long timeout, TimeUnit timeUnit, long count, K... queues);

    /**
     * Evict (and possibly remove from queue) all the jobs in memeory matching the specified job IDs. Jobs are evicted whatever
     * their state is, since this command is mostly used inside the AOF or for debugging purposes.
     * 
     * @param jobIds
     * @return The return value is the number of jobs evicted
     */
    Observable<Long> deljob(String... jobIds);

    /**
     * Set job state as acknowledged, if the job does not exist creates a fake job just to hold the acknowledge.
     * 
     * @param jobIds
     * @return The command returns the number of jobs already known and that were already not in the ACKED state.
     */
    Observable<Long> ackjob(String... jobIds);

    /**
     * Performs a fast acknowledge of the specified jobs.
     * 
     * @param jobIds
     * @return The command returns the number of jobs that are deleted from the local node as a result of receiving the command
     */
    Observable<Long> fastack(String... jobIds);

    /**
     * Describes a job without changing its state.
     * 
     * @param jobId
     * @return bulk-reply
     */
    Observable<List<Object>> show(String jobId);

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
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<String>> jscan(ScanCursor scanCursor, JScanArgs<K> scanArgs);

    /**
     * Incrementally iterate all the existing queues in the local node returning the job id's.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<String>> jscan(ScanCursor scanCursor);

}
