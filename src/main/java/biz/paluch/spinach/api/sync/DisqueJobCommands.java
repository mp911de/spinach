package biz.paluch.spinach.api.sync;

import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.AddJobArgs;
import biz.paluch.spinach.api.GetJobArgs;
import biz.paluch.spinach.api.JScanArgs;
import biz.paluch.spinach.api.Job;

import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.ScanCursor;

/**
 *
 * Synchronous executed commands related with Disque Jobs.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueJobCommands<K, V> {

    /**
     * Add job tot the {@code queue} with the body of {@code job}
     *
     * @param queue the target queue
     * @param job job body
     * @param timeout TTL timeout
     * @param timeUnit TTL timeout time unit
     * @return the job id
     */
    String addjob(K queue, V job, long timeout, TimeUnit timeUnit);

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
    String addjob(K queue, V job, long timeout, TimeUnit timeUnit, AddJobArgs addJobArgs);

    /**
     * Get jobs from the specified queue. By default COUNT is 1, so just one job will be returned. If there are no jobs in any
     * of the specified queues the command will block.
     *
     * @param queues queues
     * @return the job
     */
    Job<K, V> getjob(K... queues);

    /**
     * Get jobs from the specified queue. By default COUNT is 1, so just one job will be returned. If there are no jobs in any
     * of the specified queues the command will block.
     *
     * @param timeout timeout to wait
     * @param timeUnit timeout unit
     * @param queues queues
     * @return the job.
     */
    Job<K, V> getjob(long timeout, TimeUnit timeUnit, K... queues);

    /**
     * Get jobs from the specified queue. If there are no jobs in the specified queue the command will block.
     * Given a timeout or if noHang option is passed, the command returns null if no job can be found.
     *
     * @param getJobArgs job arguments
     * @param queues the queue
     * @return the job or null
     */
    Job<K, V> getjob(GetJobArgs getJobArgs, K... queues);

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
    List<Job<K, V>> getjobs(K... queues);

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
    List<Job<K, V>> getjobs(long timeout, TimeUnit timeUnit, long count, K... queues);

    /**
     * Get jobs from the specified queue. If there are no jobs in any of the specified queues the command will block
     * until timeout unless noHang option is passed.
     *
     * When there are jobs in more than one of the queues, the command guarantees to return jobs in the order the queues are
     * specified. If COUNT allows more jobs to be returned, queues are scanned again and again in the same order popping more
     * elements.
     *
     * If there are not enough jobs in any of the specified queues the command will return less than COUNT jobs after timeout.
     *
     * @param getJobArgs job arguments
     * @param queues queue names
     * @return the jobs
     */
    List<Job<K, V>> getjobs(GetJobArgs getJobArgs, K... queues);

    /**
     * Evict (and possibly remove from queue) all the jobs in memeory matching the specified job IDs. Jobs are evicted whatever
     * their state is, since this command is mostly used inside the AOF or for debugging purposes.
     *
     * @param jobIds the job Id's
     * @return the number of jobs evicted
     */
    long deljob(String... jobIds);

    /**
     * Set job state as acknowledged, if the job does not exist creates a fake job just to hold the acknowledge.
     *
     * @param jobIds the job Id's
     * @return the number of jobs already known and that were already not in the ACKED state.
     */
    long ackjob(String... jobIds);

    /**
     * Performs a fast acknowledge of the specified jobs.
     *
     * @param jobIds the job Id's
     * @return the number of jobs that are deleted from the local node as a result of receiving the command
     */
    long fastack(String... jobIds);

    /**
     * Describes a job without changing its state.
     *
     * @param jobId the job Id's
     * @return bulk-reply
     */
    List<Object> show(String jobId);

    /**
     * Incrementally iterate all the existing queues in the local node returning the job id's.
     *
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    KeyScanCursor<String> jscan();

    /**
     * Incrementally iterate all the existing queues in the local node returning the job id's.
     *
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    KeyScanCursor<String> jscan(JScanArgs<K> scanArgs);

    /**
     * Incrementally iterate all the existing queues in the local node returning the job id's.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    KeyScanCursor<String> jscan(ScanCursor scanCursor, JScanArgs<K> scanArgs);

    /**
     * Incrementally iterate all the existing queues in the local node returning the job id's.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    KeyScanCursor<String> jscan(ScanCursor scanCursor);

}
