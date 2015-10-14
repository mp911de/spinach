package biz.paluch.spinach.impl;

import biz.paluch.spinach.api.DisqueConnection;

/**
 * Interface to be implemented by {@link SocketAddressSupplier} that want to be aware of their connection.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface ConnectionAware {

    /**
     * Set the {@link DisqueConnection connection}.
     * <p>
     * Invoked after activating and authenticating the connection.
     * 
     * @param disqueConnection the connection
     * @param <K> Key type
     * @param <V> Value type
     */
    <K, V> void setConnection(DisqueConnection<K, V> disqueConnection);
}
