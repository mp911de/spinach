package biz.paluch.spinach.api.rx;

import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.api.QScanArgs;
import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.ScanCursor;
import rx.Observable;

/**
 * Reactive commands related with Disque Queues.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueQueueReactiveCommands<K, V> {

    /**
     *
     * @param jobIds
     * @return Return the number of jobs actually move from active to queued state
     */
    Observable<Long> enqueue(String... jobIds);

    /**
     *
     * @param jobIds the job Id's
     * @return Return the number of jobs actually moved from queue to active state
     */
    Observable<Long> dequeue(String... jobIds);

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

    /**
     * Return the number of jobs queued.
     * 
     * @param queue the queue
     * @return the number of jobs queued
     */
    Observable<Long> qlen(K queue);

    /**
     * Return an array of at most "count" jobs available inside the queue "queue" without removing the jobs from the queue. This
     * is basically an introspection and debugging command.
     * 
     * @param queue the queue
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
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<K>> qscan(ScanCursor scanCursor, QScanArgs scanArgs);

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    Observable<KeyScanCursor<K>> qscan(ScanCursor scanCursor);

}
