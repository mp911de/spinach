package biz.paluch.spinach.api.async;

import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.AddJobArgs;
import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.api.ScanArgs;

import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.ScanCursor;

/**
 * Asynchronous executed commands for Disque. This API is thread-safe.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueAsyncCommands<K, V> extends DisqueServerAsyncCommands<K, V> {

    /**
     * Add job tot the {@code queue} with the body of {@code job}
     * 
     * @param queue the target queue
     * @param job job body
     * @param timeout TTL timeout
     * @param timeUnit TTL timeout time unit
     * @return the job id
     */
    RedisFuture<String> addjob(K queue, V job, long timeout, TimeUnit timeUnit);

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
    RedisFuture<String> addjob(K queue, V job, long timeout, TimeUnit timeUnit, AddJobArgs addJobArgs);

    /**
     * Get jobs from the specified queue. By default COUNT is 1, so just one job will be returned. If there are no jobs in any
     * of the specified queues the command will block.
     *
     * @param queue the queue
     * @return the job
     */
    RedisFuture<Job<K, V>> getjob(K queue);

    /**
     * Get jobs from the specified queue. By default COUNT is 1, so just one job will be returned. If there are no jobs in any
     * of the specified queues the command will block.
     *
     * @param timeout timeout to wait
     * @param timeUnit timeout unit
     * @param queue the queue
     * @return the job.
     */
    RedisFuture<Job<K, V>> getjob(long timeout, TimeUnit timeUnit, K queue);

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
    RedisFuture<List<Job<K, V>>> getjobs(K... queues);

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
    RedisFuture<List<Job<K, V>>> getjobs(long timeout, TimeUnit timeUnit, long count, K... queues);

    /**
     *
     * @param jobIds
     * @return Return the number of jobs actually move from active to queued state
     */
    RedisFuture<Long> enqueue(String... jobIds);

    /**
     *
     * @param jobIds
     * @return Return the number of jobs actually moved from queue to active state
     */
    RedisFuture<Long> dequeue(String... jobIds);

    /**
     * Evict (and possibly remove from queue) all the jobs in memeory matching the specified job IDs. Jobs are evicted whatever
     * their state is, since this command is mostly used inside the AOF or for debugging purposes.
     * 
     * @param jobIds
     * @return The return value is the number of jobs evicted
     */
    RedisFuture<Long> deljob(String... jobIds);

    /**
     * Set job state as acknowledged, if the job does not exist creates a fake job just to hold the acknowledge.
     * 
     * @param jobIds
     * @return The command returns the number of jobs already known and that were already not in the ACKED state.
     */
    RedisFuture<Long> ackjob(String... jobIds);

    /**
     * Performs a fast acknowledge of the specified jobs.
     * 
     * @param jobIds
     * @return The command returns the number of jobs that are deleted from the local node as a result of receiving the command
     */
    RedisFuture<Long> fastack(String... jobIds);

    /**
     * Describes a job without changing its state.
     * 
     * @param jobId
     * @return bulk-reply
     */
    RedisFuture<List<Object>> show(String jobId);

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
     * @param jobId
     * @return retry count.
     */
    RedisFuture<Long> working(String jobId);

    /**
     * Return the number of jobs queued.
     * 
     * @param queue
     * @return the number of jobs queued
     */
    RedisFuture<Long> qlen(K queue);

    /**
     * Return an array of at most "count" jobs available inside the queue "queue" without removing the jobs from the queue. This
     * is basically an introspection and debugging command.
     * 
     * @param queue the queue
     * @param count number of jobs to return
     * @return List of jobs.
     */
    RedisFuture<List<Job<K, V>>> qpeek(K queue, long count);

    /**
     * Incrementally iterate the keys space.
     *
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    RedisFuture<KeyScanCursor<K>> qscan();

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    RedisFuture<KeyScanCursor<K>> qscan(ScanArgs scanArgs);

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    RedisFuture<KeyScanCursor<K>> qscan(ScanCursor scanCursor, ScanArgs scanArgs);

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    RedisFuture<KeyScanCursor<K>> qscan(ScanCursor scanCursor);

    /**
     * Authenticate to the server.
     *
     * @param password the password
     * @return String simple-string-reply
     */
    RedisFuture<String> auth(String password);

    /**
     * Ping the server.
     *
     * @return simple-string-reply
     */
    RedisFuture<String> ping();

    /**
     * Close the connection.
     * 
     * @return String simple-string-reply always OK.
     */
    RedisFuture<String> quit();

    /**
     * Close the connection. The connection will become not usable anymore as soon as this method was called.
     */
    void close();

    /**
     * 
     * @return true if the connection is open (connected and not closed).
     */
    boolean isOpen();

    /**
     *
     * @return the underlying connection.
     */
    DisqueConnection<K, V> getConnection();

}
