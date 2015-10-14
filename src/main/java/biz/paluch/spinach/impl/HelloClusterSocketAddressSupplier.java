package biz.paluch.spinach.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.cluster.DisqueNode;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class HelloClusterSocketAddressSupplier extends ClusterAwareNodeSupport implements SocketAddressSupplier,
        ConnectionAware {

    private final SocketAddressSupplier bootstrap;

    private RoundRobin<DisqueNode> roundRobin;
    private String preferredNodeIdPrefix;

    public HelloClusterSocketAddressSupplier(SocketAddressSupplier bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public SocketAddress get() {

        if (getNodes().isEmpty()) {
            return bootstrap.get();
        }

        DisqueNode disqueNode = roundRobin.next();
        return new InetSocketAddress(disqueNode.getAddr(), disqueNode.getPort());
    }

    @Override
    public <K, V> void setConnection(DisqueConnection<K, V> disqueConnection) {
        super.setConnection(disqueConnection);
        reloadNodes();
    }

    @Override
    protected void reloadNodes() {
        super.reloadNodes();

        if (preferredNodeIdPrefix != null) {
            for (DisqueNode disqueNode : getNodes()) {
                if (disqueNode.getNodeId().startsWith(preferredNodeIdPrefix)) {
                    roundRobin = new RoundRobin<DisqueNode>(getNodes(), disqueNode);
                    return;
                }
            }
        }

        roundRobin = new RoundRobin<DisqueNode>(getNodes());
    }

    public void setPreferredNodeIdPrefix(String preferredNodeIdPrefix) {
        this.preferredNodeIdPrefix = preferredNodeIdPrefix;
    }
}
