package biz.paluch.spinach.api.sync;

import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.DisqueConnection;

/**
 *
 * Synchronous executed commands for Disque. This API is thread-safe.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueCommands<K, V> extends DisqueJobCommands<K, V>, DisqueQueueCommands<K, V>, DisqueServerCommands<K, V> {

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
     * @return simple-string-reply
     */
    String ping();

    /**
     * Close the connection.
     * 
     * @return String simple-string-reply always OK.
     */
    String quit();

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

    /**
     * Set the default timeout for operations.
     *
     * @param timeout the timeout value
     * @param unit the unit of the timeout value
     */
    void setTimeout(long timeout, TimeUnit unit);
}
