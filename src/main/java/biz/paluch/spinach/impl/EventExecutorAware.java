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
package biz.paluch.spinach.impl;

import io.netty.util.concurrent.EventExecutor;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Interface to be implemented by {@link SocketAddressSupplier} that want to be aware of the {@link ScheduledExecutorService}.
 * 
 * @author Mark Paluch
 */
public interface EventExecutorAware {

    /**
     * Set the {@link ScheduledExecutorService event executor}. Invoked after activating and authenticating the connection.
     * 
     * @param eventExecutor the eventExecutor
     */
    void setEventExecutor(ScheduledExecutorService eventExecutor);
}
