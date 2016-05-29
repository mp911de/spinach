package biz.paluch.spinach.cluster;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ClusterNodesParserTest {

    @Test
    public void testParse3Nodes() throws Exception {

        Collection<DisqueNode> result = ClusterNodesParser
                .parse("c37ab8396be428403d4e55c0d317348be27ed973 127.0.0.1:7381 noflags -111 1401258245007 connected \n"
                        + "3d005a179da7d8dc1adae6409d47b39c369e992b 127.0.0.1:7380 handshake 0 1401258245007 disconnected\n"
                        + "4213a8dabb94f92eb6a860f4d0729e6a25d43e0c 127.0.0.1:7379 myself 0 1 connected\n"
                        + "5f4a2236d00008fba7ac0dd24b95762b446767bd :0 noaddr 0 1 connected");

        assertThat(result).hasSize(4);

        Iterator<DisqueNode> iterator = result.iterator();

        DisqueNode p1 = iterator.next();

        assertThat(p1.getNodeId()).isEqualTo("c37ab8396be428403d4e55c0d317348be27ed973");
        assertThat(p1.getAddr()).isEqualTo("127.0.0.1");
        assertThat(p1.getPort()).isEqualTo(7381);
        assertThat(p1.getFlags()).isEqualTo(Collections.singleton(DisqueNode.NodeFlag.NOFLAGS));
        assertThat(p1.getPingSentTimestamp()).isEqualTo(-111);
        assertThat(p1.getPongReceivedTimestamp()).isEqualTo(1401258245007L);
        assertThat(p1.isConnected()).isTrue();

        // skip
        iterator.next();

        DisqueNode p3 = iterator.next();

        assertThat(p3.toString()).contains(DisqueNode.class.getSimpleName());

    }

    @Test
    public void testParse2Nodes() {

        Collection<DisqueNode> result = ClusterNodesParser
                .parse("f37e56400fdc4b097597b6998b273059ad6f3b47 127.0.0.1:7712 noflags 0 1441026672360 connected\n"
                        + "2febf1de8bffc1450642b4353e174884cd40b717 127.0.0.1:7711 myself 0 0 connected");

        assertThat(result).hasSize(2);

    }

    @Test
    public void testModel() throws Exception {
        DisqueNode node = new DisqueNode();
        node.setConnected(true);
        node.setFlags(new HashSet<>());
        node.setNodeId("abcd");
        node.setPingSentTimestamp(2);
        node.setPongReceivedTimestamp(3);
        node.setAddr("127.0.0.1");
        node.setPort(1);

        assertThat(node.toString()).contains(DisqueNode.class.getSimpleName());

        DisqueNode similarNode = new DisqueNode();
        similarNode.setNodeId("abcd");
        assertThat(node).isEqualTo(similarNode);
        assertThat(node.hashCode()).isEqualTo(similarNode.hashCode());

    }

    @Test
    public void testGetNodeIdPrefixFromJobId() throws Exception {
        String result = GetJobsAction.getNodeIdPrefix("D-ff010e8a-6FH7ewVysl5mZmXbW0/GRqvG-05a1A$");
        assertThat(result).isEqualTo("ff010e8a");
    }

}
