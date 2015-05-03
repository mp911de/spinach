package biz.paluch.spinach;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.RedisServerAsyncConnection;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueAsyncConnection<K, V> extends RedisServerAsyncConnection<K, V>, Closeable {

    RedisFuture<String> addJob(K queue, V job, long duration, TimeUnit timeUnit);

    RedisFuture<String> addJob(K queue, V job, long duration, TimeUnit timeUnit, AddJobArgs addJobArgs);

    RedisFuture<Job<K, V>> getJob(K queue);

    RedisFuture<Job<K, V>> getJob(long duration, TimeUnit timeUnit, K queue);

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
