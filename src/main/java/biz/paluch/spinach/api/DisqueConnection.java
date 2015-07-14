package biz.paluch.spinach.api;

import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.async.DisqueAsyncCommands;
import biz.paluch.spinach.api.rx.DisqueReactiveCommands;
import biz.paluch.spinach.api.sync.DisqueCommands;

import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.protocol.RedisCommand;

/**
 * A thread-safe connection to a redis server. Multiple threads may share one {@link DisqueConnection}.
 * 
 * A {@link com.lambdaworks.redis.protocol.ConnectionWatchdog} monitors each connection and reconnects automatically until
 * {@link #close} is called. All pending commands will be (re)sent after successful reconnection.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 24.06.15 07:29
 */
public interface DisqueConnection<K, V> {

    /**
     * Returns the {@link DisqueCommands} API for the current connection. Does not create a new connection.
     * 
     * @return the synchronous API for the underlying connection.
     */
    DisqueCommands<K, V> sync();

    /**
     * Returns the {@link DisqueAsyncCommands} API for the current connection. Does not create a new connection.
     * 
     * @return the asynchronous API for the underlying connection.
     */
    DisqueAsyncCommands<K, V> async();

    /**
     * Returns the {@link DisqueReactiveCommands} API for the current connection. Does not create a new connection.
     *
     * @return the reactive API for the underlying connection.
     */
    DisqueReactiveCommands<K, V> reactive();

    /**
     * Set the default command timeout for this connection.
     *
     * @param timeout Command timeout.
     * @param unit Unit of time for the timeout.
     */
    void setTimeout(long timeout, TimeUnit unit);

    /**
     * @return the timeout unit.
     */
    TimeUnit getTimeoutUnit();

    /**
     * @return the timeout.
     */
    long getTimeout();

    /**
     * Dispatch a command. Write a command on the channel. The command may be changed/wrapped during write and the written
     * instance is returned after the call.
     *
     * @param command the redis command
     * @param <T> result type
     * @return the written redis command
     */
    <T> RedisCommand<K, V, T> dispatch(RedisCommand<K, V, T> command);

    /**
     * Close the connection. The connection will become not usable anymore as soon as this method was called.
     */
    void close();

    /**
     * @return true if the connection is open (connected and not closed).
     */
    boolean isOpen();

    /**
     *
     * @return the client options valid for this connection.
     */
    ClientOptions getOptions();

    /**
     * Reset the command state. Queued commands will be canceled and the internal state will be reset. This is useful when the
     * internal state machine gets out of sync with the connection.
     */
    void reset();
}
