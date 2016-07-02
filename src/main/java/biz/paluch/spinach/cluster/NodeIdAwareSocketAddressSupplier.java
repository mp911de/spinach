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
package biz.paluch.spinach.cluster;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.lambdaworks.redis.internal.LettuceAssert;

import biz.paluch.spinach.impl.HelloClusterSocketAddressSupplier;
import biz.paluch.spinach.impl.RoundRobin;
import biz.paluch.spinach.impl.SocketAddressSupplier;

/**
 * This mechanism allows to set a preferred node Id for next {@code HELLO} handshake. If the
 * {@link #setPreferredNodeIdPrefix(String)} is set, the selection mechanism tries to provide a {@link SocketAddress} from the
 * preferred node.
 *
 * @see biz.paluch.spinach.impl.HelloClusterSocketAddressSupplier
 */
public class NodeIdAwareSocketAddressSupplier extends HelloClusterSocketAddressSupplier {
    private transient String currentNodeId;
    private transient InetSocketAddress currentSocketAddress;
    private String preferredNodeIdPrefix;

    /**
     *
     * @param bootstrap bootstrap/fallback {@link SocketAddressSupplier} for bootstrapping before any communication is done.
     */
    public NodeIdAwareSocketAddressSupplier(SocketAddressSupplier bootstrap) {
        super(bootstrap);
    }

    @Override
    public SocketAddress get() {
        currentSocketAddress = (InetSocketAddress) super.get();
        resolveCurrentNodeId();
        return currentSocketAddress;
    }

    @Override
    public void reloadNodes() {
        super.reloadNodes();
        resolveCurrentNodeId();
    }

    private void resolveCurrentNodeId() {
        if (currentSocketAddress == null) {
            return;
        }

        for (DisqueNode disqueNode : getNodes()) {

            if (currentSocketAddress.isUnresolved()) {
                if (currentSocketAddress.getHostString().equals(disqueNode.getAddr())
                        && disqueNode.getPort() == currentSocketAddress.getPort()) {
                    currentNodeId = disqueNode.getNodeId();
                    break;
                }
            } else {
                if (currentSocketAddress.getAddress().getHostAddress().equals(disqueNode.getAddr())
                        && disqueNode.getPort() == currentSocketAddress.getPort()) {
                    currentNodeId = disqueNode.getNodeId();
                    break;
                }
            }
        }
    }

    /**
     *
     * @return the current connected nodeId, may be {@literal null} if not resolvable
     */
    public String getCurrentNodeId() {
        return currentNodeId;
    }

    /**
     * Set the id prefix of the preferred node.
     *
     * @param preferredNodeIdPrefix the id prefix of the preferred node
     */
    public void setPreferredNodeIdPrefix(String preferredNodeIdPrefix) {
        LettuceAssert.notNull(preferredNodeIdPrefix, "preferredNodeIdPrefix must not be null");
        boolean resetRoundRobin = false;

        if (this.preferredNodeIdPrefix == null || !preferredNodeIdPrefix.equals(this.preferredNodeIdPrefix)) {
            resetRoundRobin = true;
        }

        this.preferredNodeIdPrefix = preferredNodeIdPrefix;

        if (resetRoundRobin) {
            resetRoundRobin(preferredNodeIdPrefix);
        }
    }

    /**
     * Reset the {@link RoundRobin} to start with the node matching the {@code preferredNodeIdPrefix}.
     *
     * @param preferredNodeIdPrefix the id prefix of the preferred node
     */
    private void resetRoundRobin(String preferredNodeIdPrefix) {
        DisqueNode previous = null; // remember the previous node because the offset is a marker to start with the next
        // element
        for (DisqueNode disqueNode : getNodes()) {
            if (disqueNode.getNodeId().startsWith(preferredNodeIdPrefix)) {
                roundRobin = new RoundRobin<DisqueNode>(getNodes(), previous);
                return;
            }
            previous = disqueNode;
        }
    }

    public String getPreferredNodeIdPrefix() {
        return preferredNodeIdPrefix;
    }
}
