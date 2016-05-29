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
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
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
