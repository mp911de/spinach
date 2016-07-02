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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;

import com.lambdaworks.redis.ConnectionPoint;
import com.lambdaworks.redis.internal.LettuceAssert;

import biz.paluch.spinach.DisqueURI;

/**
 * Round-Robin socket address supplier. Connection points are iterated circular/infinitely.
 * 
 * @author Mark Paluch
 */
public class RoundRobinSocketAddressSupplier implements SocketAddressSupplier {

    protected final Collection<? extends ConnectionPoint> connectionPoint;
    protected RoundRobin<? extends ConnectionPoint> roundRobin;

    /**
     * 
     * @param connectionPoints the collection of {@link ConnectionPoint connection points}, must not be {@literal null}.
     */
    public RoundRobinSocketAddressSupplier(Collection<? extends ConnectionPoint> connectionPoints) {
        this(connectionPoints, null);
    }

    /**
     * 
     * @param connectionPoints the collection of {@link ConnectionPoint connection points}, must not be {@literal null}.
     * @param offset {@link ConnectionPoint connection point} offset for starting the round robin cycle at that point, can be
     *        {@literal null}.
     */
    public RoundRobinSocketAddressSupplier(Collection<? extends ConnectionPoint> connectionPoints, ConnectionPoint offset) {
        LettuceAssert.notNull(connectionPoints, "ConnectionPoints must not be null");
        this.connectionPoint = connectionPoints;
        this.roundRobin = new RoundRobin<ConnectionPoint>(connectionPoints, offset);
    }

    @Override
    public SocketAddress get() {
        ConnectionPoint connectionPoint = roundRobin.next();
        return getSocketAddress(connectionPoint);
    }

    protected static SocketAddress getSocketAddress(ConnectionPoint connectionPoint) {

        if (connectionPoint instanceof DisqueURI.DisqueSocket) {
            return ((DisqueURI.DisqueSocket) connectionPoint).getSocketAddress();
        }
        return InetSocketAddress.createUnresolved(connectionPoint.getHost(), connectionPoint.getPort());
    }
}
