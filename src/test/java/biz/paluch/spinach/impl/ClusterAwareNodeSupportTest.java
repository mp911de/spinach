package biz.paluch.spinach.impl;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.cluster.DisqueNode;
import biz.paluch.spinach.commands.AbstractCommandTest;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ClusterAwareNodeSupportTest extends AbstractCommandTest {

    private TestClusterAwareNodeSupport sut = new TestClusterAwareNodeSupport();

    @Test
    public void testClusterView() throws Exception {

        sut.setConnection(disque.getConnection());
        sut.reloadNodes();

        List<DisqueNode> nodes = sut.getNodes();
        assertThat(nodes).hasSize(2);
        assertThat(nodes.get(0).getPort()).isEqualTo(port);
        assertThat(nodes.get(0).getAddr()).isNotNull();
        assertThat(nodes.get(0).getNodeId()).isNotNull();
    }
}