package biz.paluch.spinach.api.async;

import biz.paluch.spinach.api.DisqueConnection;
import com.lambdaworks.redis.RedisFuture;

/**
 * Asynchronous executed commands for Disque. This API is thread-safe.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueAsyncCommands<K, V> extends DisqueJobAsyncCommands<K, V>, DisqueQueueAsyncCommands<K, V>,
        DisqueServerAsyncCommands<K, V>, DisqueClusterAsyncCommands<K, V> {

    /**
     * Authenticate to the server.
     *
     * @param password the password
     * @return String simple-string-reply
     */
    RedisFuture<String> auth(String password);

    /**
     * Close the connection. The connection will become not usable anymore as soon as this method was called.
     */
    void close();

    /**
     *
     * @return the underlying connection.
     */
    DisqueConnection<K, V> getConnection();

    /**
     * 
     * @return true if the connection is open (connected and not closed).
     */
    boolean isOpen();

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

}
