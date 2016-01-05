package biz.paluch.spinach.api;

import biz.paluch.spinach.api.async.DisqueAsyncCommands;
import biz.paluch.spinach.api.rx.DisqueReactiveCommands;
import biz.paluch.spinach.api.sync.DisqueCommands;

import com.lambdaworks.redis.api.StatefulConnection;

/**
 * A thread-safe connection to a redis server. Multiple threads may share one {@link DisqueConnection}.
 * 
 * A {@link com.lambdaworks.redis.protocol.ConnectionWatchdog} monitors each connection and reconnects automatically until
 * {@link #close} is called. All pending commands will be (re)sent after successful reconnection.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueConnection<K, V> extends StatefulConnection<K, V> {

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

}
