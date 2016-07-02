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

import java.io.Serializable;
import java.util.*;

import com.lambdaworks.redis.internal.LettuceAssert;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.cluster.DisqueNode;

/**
 * Convenient base class for classes that rely on the cluster topology of Disque. Typically subclassed by
 * {@link SocketAddressSupplier SocketAddressSuppliers}.
 * 
 * @author Mark Paluch
 */
public abstract class ClusterAwareNodeSupport {

    public final static int MAX_ALLOWED_PRIORITY = 99;

    private DisqueConnection<Object, Object> disqueConnection;
    private final List<DisqueNode> nodes = new ArrayList<>();

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
        List<PrioritizedDisqueNode> nodes = new ArrayList<>();

    }

    static class HelloParser {

        public static Hello parse(List<Object> hello) {

            LettuceAssert.isTrue(hello.size() > 2, "HELLO output must contain more than two elements");
            LettuceAssert.isTrue(Long.valueOf(1).equals(hello.get(0)),
                    "Only HELLO version 1 supported. Received HELLO version is " + hello.get(0));

            Hello result = new Hello();
            result.version = (Long) hello.get(0);
            result.nodeId = (String) hello.get(1);

            for (int i = 2; i < hello.size(); i++) {
                LettuceAssert.assertState(hello.get(i) instanceof Collection,
                        "HELLO output at index " + i + " is not a collection");
                Collection<Object> nodeDetails = (Collection<Object>) hello.get(i);
                LettuceAssert.assertState(nodeDetails.size() > 3, "HELLO output at index " + i + " has less than 4 elements");

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
