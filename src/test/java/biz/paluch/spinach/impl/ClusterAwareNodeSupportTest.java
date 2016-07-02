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

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.cluster.DisqueNode;
import biz.paluch.spinach.commands.AbstractCommandTest;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mark Paluch
 */
public class ClusterAwareNodeSupportTest extends AbstractCommandTest {

    private TestClusterAwareNodeSupport sut = new TestClusterAwareNodeSupport();

    @Test
    public void testClusterView() throws Exception {

        sut.setConnection(disque.getConnection());
        sut.reloadNodes();

        List<DisqueNode> nodes = sut.getNodes();
        assertThat(nodes.size()).isGreaterThan(1);
        assertThat(nodes.get(0).getPort()).isEqualTo(port);
        assertThat(nodes.get(0).getAddr()).isNotNull();
        assertThat(nodes.get(0).getNodeId()).isNotNull();
    }
}