package biz.paluch.spinach.impl;

import io.netty.util.concurrent.EventExecutor;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Interface to be implemented by {@link SocketAddressSupplier} that want to be aware of the {@link ScheduledExecutorService}.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface EventExecutorAware {

    /**
     * Set the {@link ScheduledExecutorService event executor}. Invoked after activating and authenticating the connection.
     * 
     * @param eventExecutor the eventExecutor
     */
    void setEventExecutor(ScheduledExecutorService eventExecutor);
}
