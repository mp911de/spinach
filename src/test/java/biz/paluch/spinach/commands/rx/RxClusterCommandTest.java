package biz.paluch.spinach.commands.rx;

import biz.paluch.spinach.commands.ClusterCommandTest;
import org.junit.Before;

import biz.paluch.spinach.commands.QueueCommandTest;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 14.07.15 08:28
 */
public class RxClusterCommandTest extends ClusterCommandTest {

    @Before
    public void openConnection() throws Exception {
        disque = RxSyncInvocationHandler.sync(client.connect());
        disque.debugFlushall();
    }
}
