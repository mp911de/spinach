package biz.paluch.spinach;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.RedisServerConnection;
import com.lambdaworks.redis.ScanCursor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueConnection<K, V> extends RedisServerConnection<K, V>, Closeable {

    String addjob(K queue, V job, long timeout, TimeUnit timeUnit);

    String addjob(K queue, V job, long timeout, TimeUnit timeUnit, AddJobArgs addJobArgs);

    Job<K, V> getjob(K queue);

    Job<K, V> getjob(long timeout, TimeUnit timeUnit, K queue);

    List<Job<K, V>> getjob(K... queues);

    List<Job<K, V>> getjob(long timeout, TimeUnit timeUnit, long count, K... queues);

    long enqueue(String... jobIds);

    long dequeue(String... jobIds);

    long deljob(String... jobIds);

    long ackjob(String... jobIds);

    long fastack(String... jobIds);

    List<Object> show(String jobId);

    long working(String jobId);

    long qlen(K queue);

    List<Job<K, V>> qpeek(K queue, long count);

    String debugFlushall();

    List<Object> hello();

    /**
     * Ping the server.
     * 
     * @return simple-string-reply
     */
    String ping();

    /**
     * Incrementally iterate the keys space.
     *
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    KeyScanCursor<K> qscan();

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    KeyScanCursor<K> qscan(ScanArgs scanArgs);

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    KeyScanCursor<K> qscan(ScanCursor scanCursor, ScanArgs scanArgs);

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    KeyScanCursor<K> qscan(ScanCursor scanCursor);

    /**
     * Set the default timeout for operations.
     *
     * @param timeout the timeout value
     * @param unit the unit of the timeout value
     */
    void setTimeout(long timeout, TimeUnit unit);

    /**
     * Authenticate to the server.
     *
     * @param password the password
     * @return String simple-string-reply
     */
    String auth(String password);

    /**
     * Close the connection.
     * 
     * @return String simple-string-reply always OK.
     */
    String quit();

    /**
     * Close the connection. The connection will become not usable anymore as soon as this method was called.
     */
    @Override
    void close();

}
