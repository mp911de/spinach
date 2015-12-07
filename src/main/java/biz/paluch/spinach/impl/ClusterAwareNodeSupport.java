package biz.paluch.spinach.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.io.Serializable;
import java.util.*;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.cluster.DisqueNode;

import com.google.common.collect.Lists;

/**
 * Convenient base class for classes that rely on the cluster topology of Disque. Typically subclassed by
 * {@link SocketAddressSupplier SocketAddressSuppliers}.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public abstract class ClusterAwareNodeSupport {

    public final static int MAX_ALLOWED_PRIORITY = 99;

    private DisqueConnection<Object, Object> disqueConnection;
    private final List<DisqueNode> nodes = Lists.newArrayList();

    /**
     * Load/reload cluster nodes and order the nodes by its priority.
     */
    protected void reloadNodes() {

        Hello hello = HelloParser.parse(disqueConnection.sync().hello());
        Collections.sort(hello.nodes, new Comparator<PrioritizedDisqueNode>() {
            @Override
            public int compare(PrioritizedDisqueNode o1, PrioritizedDisqueNode o2) {

                if (o1.priority == o2.priority) {
                    return o1.disqueNode.getPort() - o2.disqueNode.getPort();
                }
                return o1.priority - o2.priority;
            }
        });
        this.nodes.clear();

        for (PrioritizedDisqueNode node : hello.nodes) {
            if (isFiltered(node)) {
                continue;
            }

            this.nodes.add(node.disqueNode);
        }
    }

    /**
     * @param node the cluster node
     * @return {@literal true} if the {@code node} is filtered
     */
    protected boolean isFiltered(PrioritizedDisqueNode node) {
        if (node.priority > MAX_ALLOWED_PRIORITY) {
            return true;
        }
        return false;
    }

    public <K, V> void setConnection(DisqueConnection<K, V> disqueConnection) {
        this.disqueConnection = (DisqueConnection<Object, Object>) disqueConnection;
    }

    /**
     *
     * @return the list of {@link DisqueNode nodes}
     */
    public List<DisqueNode> getNodes() {
        return nodes;
    }

    /**
     * Disque node with priority.
     */
    static class PrioritizedDisqueNode implements Serializable {

        DisqueNode disqueNode;
        int priority;

    }

    static class Hello {
        long version;
        String nodeId;
        List<PrioritizedDisqueNode> nodes = Lists.newArrayList();

    }

    static class HelloParser implements Serializable {

        public static Hello parse(List<Object> hello) {

            checkArgument(hello.size() > 2, "HELLO output must contain more than two elements");
            checkArgument(Long.valueOf(1).equals(hello.get(0)), "Only HELLO version 1 supported. Received HELLO version is "
                    + hello.get(0));

            Hello result = new Hello();
            result.version = (Long) hello.get(0);
            result.nodeId = (String) hello.get(1);

            for (int i = 2; i < hello.size(); i++) {
                checkState(hello.get(i) instanceof Collection, "HELLO output at index " + i + " is not a collection");
                Collection<Object> nodeDetails = (Collection<Object>) hello.get(i);
                checkState(nodeDetails.size() > 3, "HELLO output at index " + i + " has less than 4 elements");

                Iterator<Object> iterator = nodeDetails.iterator();

                DisqueNode disqueNode = new DisqueNode();
                disqueNode.setNodeId((String) iterator.next());
                disqueNode.setAddr((String) iterator.next());
                disqueNode.setPort(Integer.parseInt((String) iterator.next()));

                PrioritizedDisqueNode prioritizedDisqueNode = new PrioritizedDisqueNode();
                prioritizedDisqueNode.disqueNode = disqueNode;
                prioritizedDisqueNode.priority = Integer.parseInt((String) iterator.next());

                result.nodes.add(prioritizedDisqueNode);
            }

            return result;
        }
    }

}
