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

    RedisFuture<String> addjob(K queue, V job, long timeout, TimeUnit timeUnit);

    RedisFuture<String> addjob(K queue, V job, long timeout, TimeUnit timeUnit, AddJobArgs addJobArgs);

    RedisFuture<Job<K, V>> getjob(K queue);

    RedisFuture<Job<K, V>> getjob(long timeout, TimeUnit timeUnit, K queue);

    RedisFuture<List<Job<K, V>>> getjobs(K... queues);

    RedisFuture<List<Job<K, V>>> getjobs(long timeout, TimeUnit timeUnit, long count, K... queues);

    RedisFuture<Long> enqueue(String... jobIds);

    RedisFuture<Long> dequeue(String... jobIds);

    RedisFuture<Long> deljob(String... jobIds);

    RedisFuture<Long> ackjob(String... jobIds);

    RedisFuture<Long> fastack(String... jobIds);

    RedisFuture<List<Object>> show(String jobId);

    RedisFuture<Long> working(String jobId);

    RedisFuture<Long> qlen(K queue);

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
     * @return RedisFuture&lt;String&gt; simple-string-reply
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

    DisqueConnection<K, V> getConnection();

}
