package biz.paluch.spinach.commands.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.functions.Func1;
import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.api.rx.DisqueReactiveCommands;
import biz.paluch.spinach.commands.JobCommandTest;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RxJobCommandTest extends JobCommandTest {

    protected DisqueReactiveCommands<String, String> rx;

    @Before
    public void openConnection() throws Exception {
        disque = RxSyncInvocationHandler.sync(client.connect());
        disque.debugFlushall();
        rx = disque.getConnection().reactive();
    }

    @Test
    public void addJob() throws Exception {

        String result = rx.addjob(queue, value, 5, TimeUnit.SECONDS).toBlocking().first();
        assertThat(result).startsWith("D-");
    }

    @Test
    public void rxChaining() throws Exception {

        addJob();
        long qlen = rx.qlen(queue).toBlocking().first();

        assertThat(qlen).isEqualTo(1);

        final DisqueReactiveCommands<String, String> rx = client.connect().reactive();
        rx.getjob(queue).flatMap(new Func1<Job<String, String>, Observable<Long>>() {
            @Override
            public Observable<Long> call(Job<String, String> job) {
                return rx.ackjob(job.getId());
            }
        }).subscribe();

        assertThat(rx.qlen(queue).toBlocking().first()).isEqualTo(0);

    }

}
