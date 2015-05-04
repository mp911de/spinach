package biz.paluch.spinach;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.RedisServerAsyncConnection;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueAsyncConnection<K, V> extends RedisServerAsyncConnection<K, V>, Closeable {

    RedisFuture<String> addjob(K queue, V job, long timeout, TimeUnit timeUnit);

    RedisFuture<String> addjob(K queue, V job, long timeout, TimeUnit timeUnit, AddJobArgs addJobArgs);

    RedisFuture<Job<K, V>> getjob(K queue);

    RedisFuture<Job<K, V>> getjob(long timeout, TimeUnit timeUnit, K queue);

    RedisFuture<List<Job<K, V>>> getjob(K... queues);

    RedisFuture<List<Job<K, V>>> getjob(long timeout, TimeUnit timeUnit, long count, K... queues);

    RedisFuture<Long> enqueue(String... jobIds);

    RedisFuture<Long> dequeue(String... jobIds);

    RedisFuture<Long> deljob(String... jobIds);

    RedisFuture<Long> ackjob(String... jobIds);

    RedisFuture<Long> fastack(String... jobIds);

    RedisFuture<List<Object>> show(String jobId);

    RedisFuture<Long> qlen(K queue);

    RedisFuture<List<Job<K, V>>> qpeek(K queue, long count);

    RedisFuture<String> debugFlushall();

    RedisFuture<List<Object>> hello();

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
     * Ping the server.
     * 
     * @return RedisFuture&lt;String&gt; simple-string-reply
     */
    RedisFuture<String> ping();

    /**
     * Close the connection.
     * 
     * @return RedisFuture&lt;String&gt; simple-string-reply always OK.
     */
    RedisFuture<String> quit();

    /**
     * Close the connection. The connection will become not usable anymore as soon as this method was called.
     */
    @Override
    void close();

    /**
     * 
     * @return true if the connection is open (connected and not closed).
     */
    boolean isOpen();

}
