package biz.paluch.spinach.api.rx;

import rx.Observable;
import biz.paluch.spinach.api.DisqueConnection;

/**
 * Reactive commands for Disque. This API is thread-safe.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueReactiveCommands<K, V> extends DisqueJobReactiveCommands<K, V>, DisqueQueueReactiveCommands<K, V>,
        DisqueServerReactiveCommands<K, V>, DisqueClusterReactiveCommands<K, V> {

    /**
     * Authenticate to the server.
     *
     * @param password the password
     * @return String simple-string-reply
     */
    Observable<String> auth(String password);

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
    Observable<String> ping();

    /**
     * Close the connection.
     * 
     * @return String simple-string-reply always OK.
     */
    Observable<String> quit();

}
