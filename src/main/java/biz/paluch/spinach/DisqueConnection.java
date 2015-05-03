package biz.paluch.spinach;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.RedisServerConnection;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueConnection<K, V> extends RedisServerConnection<K, V>, Closeable {

    String addJob(K queue, V job, long duration, TimeUnit timeUnit);

    String addJob(K queue, V job, long duration, TimeUnit timeUnit, AddJobArgs addJobArgs);

    Job<K, V> getJob(K queue);

    Job<K, V> getJob(long duration, TimeUnit timeUnit, K queue);

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
