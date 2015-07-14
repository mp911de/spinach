package biz.paluch.spinach.commands.rx;

import biz.paluch.spinach.commands.ServerCommandTest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 14.07.15 08:28
 */
public class RxServerCommandTest extends ServerCommandTest {

    @Before
    public void openConnection() throws Exception {
        disque = RxSyncInvocationHandler.sync(client.connect());
        disque.debugFlushall();
    }

    // does not harm, because it's only executed when subscribing.
    @Test
    public void shutdown() throws Exception {
        disque.getConnection().reactive().shutdown(true);
    }
}
