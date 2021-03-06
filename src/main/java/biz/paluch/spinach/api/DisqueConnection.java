/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * @author Mark Paluch
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
