package biz.paluch.spinach.commands.rx;

import biz.paluch.spinach.commands.QueueCommandTest;
import org.junit.Before;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 14.07.15 08:28
 */
public class RxQueueCommandTest extends QueueCommandTest {

    @Before
    public void openConnection() throws Exception {
        disque = RxSyncInvocationHandler.sync(client.connect());
        disque.debugFlushall();
    }
}
