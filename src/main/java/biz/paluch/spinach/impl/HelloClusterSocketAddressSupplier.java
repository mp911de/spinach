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

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.cluster.DisqueNode;

/**
 * Supplier for {@link SocketAddress adresses} that is aware of the cluster nodes.
 * <p>
 * This class performs a {@code HELLO} command handshake upon connection and retrieves the nodes from the command result. The
 * node set is not refreshed once it is retrieved. The nodes are used in the order of their priority in a round-robin fashion.
 * Until the handshake is completed a fallback {@link SocketAddressSupplier} is used.
 * </p>
 *
 * @author Mark Paluch
 */
public class HelloClusterSocketAddressSupplier extends ClusterAwareNodeSupport implements SocketAddressSupplier,
        ConnectionAware {

    protected final SocketAddressSupplier bootstrap;
    protected RoundRobin<DisqueNode> roundRobin;

    /**
     * 
     * @param bootstrap bootstrap/fallback {@link SocketAddressSupplier} for bootstrapping before any communication is done.
     */
    public HelloClusterSocketAddressSupplier(SocketAddressSupplier bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public SocketAddress get() {

        if (getNodes().isEmpty()) {
            return bootstrap.get();
        }

        DisqueNode disqueNode = roundRobin.next();
        return InetSocketAddress.createUnresolved(disqueNode.getAddr(), disqueNode.getPort());
    }

    @Override
    public <K, V> void setConnection(DisqueConnection<K, V> disqueConnection) {
        super.setConnection(disqueConnection);
        reloadNodes();
    }

    @Override
    public void reloadNodes() {
        super.reloadNodes();
        roundRobin = new RoundRobin<DisqueNode>(getNodes());
    }

}
