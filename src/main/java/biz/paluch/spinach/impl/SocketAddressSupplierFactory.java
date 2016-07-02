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

import com.lambdaworks.redis.internal.LettuceAssert;
import com.lambdaworks.redis.internal.LettuceLists;

import biz.paluch.spinach.DisqueURI;

/**
 * Factory for a {@link SocketAddressSupplier}. This factory creates new instances of {@link SocketAddressSupplier} based on a
 * {@link DisqueURI} for every logical connection.
 * 
 * @author Mark Paluch
 */
public interface SocketAddressSupplierFactory {

    /**
     * Creates a new {@link SocketAddressSupplier} from a {@link SocketAddressSupplier}.
     * 
     * @param disqueURI the connection URI object, must not be {@literal null}
     * @return an new instance of {@link SocketAddressSupplier}
     */
    SocketAddressSupplier newSupplier(DisqueURI disqueURI);

    enum Factories implements SocketAddressSupplierFactory {
        /**
         * Round-Robin address supplier that runs circularly over the provided connection points within the {@link DisqueURI}.
         */
        ROUND_ROBIN {
            @Override
            public SocketAddressSupplier newSupplier(DisqueURI disqueURI) {
                LettuceAssert.notNull(disqueURI, "DisqueURI must not be null");
                return new RoundRobinSocketAddressSupplier(LettuceLists.unmodifiableList(disqueURI.getConnectionPoints()));
            }
        },

        /**
         * Cluster-topology-aware address supplier that obtains its initial connection point from the {@link DisqueURI} and then
         * looks up the cluster topology using the {@code HELLO} command. Connections are established using the cluster node IP
         * addresses.
         */
        HELLO_CLUSTER {
            @Override
            public SocketAddressSupplier newSupplier(DisqueURI disqueURI) {
                LettuceAssert.notNull(disqueURI, "DisqueURI must not be null");
                return new HelloClusterSocketAddressSupplier(ROUND_ROBIN.newSupplier(disqueURI));
            }
        };

        @Override
        public SocketAddressSupplier newSupplier(DisqueURI disqueURI) {
            return null;
        }
    }
}
