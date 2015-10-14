package biz.paluch.spinach.commands;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;

import biz.paluch.spinach.TestSettings;
import biz.paluch.spinach.cluster.ClusterNodesParser;
import biz.paluch.spinach.cluster.DisqueNode;

public class ClusterCommandTest extends AbstractCommandTest {

    @Test
    public void clusterMeet() throws Exception {
        String result = disque.clusterMeet(TestSettings.hostAddr(), TestSettings.port(1));
        assertThat(result).isEqualTo("OK");
    }

    @Test
    @Ignore("Run me manually otherwise I will affect all the other tests")
    public void clusterReset() throws Exception {

        assertThat(disque.clusterReset(false)).isEqualTo("OK");
        disque.clusterMeet(TestSettings.hostAddr(), TestSettings.port(1));
    }

    @Test
    public void clusterSaveconfig() throws Exception {
        String result = disque.clusterSaveconfig();
        assertThat(result).isEqualTo("OK");
    }

    @Test
    @Ignore("Run me manually otherwise I will affect all the other tests")
    public void clusterForget() throws Exception {

        String output = disque.clusterNodes();
        Collection<DisqueNode> result = ClusterNodesParser.parse(output);

        DisqueNode otherNode = getOtherNode(result);

        assertThat(disque.clusterForget(otherNode.getNodeId())).isEqualTo("OK");
        disque.clusterMeet(otherNode.getAddr(), otherNode.getPort());
    }

    @Test
    public void clusterMyId() throws Exception {

        String output = disque.clusterNodes();
        Collection<DisqueNode> result = ClusterNodesParser.parse(output);

        DisqueNode ownNode = getOwnNode(result);

        assertThat(disque.clusterMyId()).isEqualTo(ownNode.getNodeId());
    }

    @Test
    public void clusterNodes() throws Exception {
        String output = disque.clusterNodes();

        Collection<DisqueNode> result = ClusterNodesParser.parse(output);
        assertThat(result.size()).isGreaterThan(1);
    }

    @Test
    public void clusterInfo() throws Exception {
        String output = disque.clusterInfo();

        assertThat(output).contains("cluster_state:").contains("cluster_stats_messages_sent");
    }

    private DisqueNode getOtherNode(Collection<DisqueNode> nodes) {
        for (DisqueNode node : nodes) {
            if (node.getFlags().contains(DisqueNode.NodeFlag.MYSELF)) {
                continue;
            }

            return node;
        }

        return null;
    }

    private DisqueNode getOwnNode(Collection<DisqueNode> nodes) {
        for (DisqueNode node : nodes) {
            if (node.getFlags().contains(DisqueNode.NodeFlag.MYSELF)) {
                return node;
            }
        }
        return null;
    }

}
