package biz.paluch.spinach.cluster;

import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import rx.Observable;
import rx.Subscription;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import biz.paluch.spinach.DisqueURI;
import biz.paluch.spinach.TestSettings;
import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.commands.AbstractCommandTest;

import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.Duration;
import com.google.code.tempusfugit.temporal.WaitFor;
import com.lambdaworks.redis.codec.Utf8StringCodec;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class QueueListenerFactoryTest extends AbstractCommandTest {

    private static DisqueURI disqueURI0 = DisqueURI.create(TestSettings.host(), TestSettings.port(0));
    private static DisqueURI disqueURI1 = DisqueURI.create(TestSettings.host(), TestSettings.port(1));

    private TestSubscriber<Job<String, String>> subscriber = new TestSubscriber<Job<String, String>>();

    private QueueListenerFactory<String, String> queueListenerFactory = QueueListenerFactory.create(disqueURI0,
            new Utf8StringCodec(), queue);

    private DisqueConnection<String, String> connection0 = client.connect(disqueURI0);
    private DisqueConnection<String, String> connection1 = client.connect(disqueURI1);

    @Before
    public void before() throws Exception {
        connection0.sync().clusterLeaving(false);
        connection1.sync().clusterLeaving(false);
    }

    @After
    public void after() throws Exception {
        queueListenerFactory.shutdown(0, 200, TimeUnit.MILLISECONDS);
        connection0.close();
        connection1.close();
    }

    @Test
    public void simpleQueueListener() throws Exception {
        queueListenerFactory.getjobs().subscribe(subscriber);
        subscriber.assertNoErrors();

        createJobs(connection0);
        waitForSomeReceivedJobs();
    }

    @Test
    public void simpleQueueListenerWithArgs() throws Exception {
        queueListenerFactory.getjobs(10, TimeUnit.MILLISECONDS, 1).subscribe(subscriber);

        createJobs(connection0);
        waitForSomeReceivedJobs();
    }

    @Test
    public void queueListenerOnSameNode() throws Exception {
        queueListenerFactory.withLocalityTracking().getjobs().subscribe(subscriber);

        createJobs(connection0);
        waitForSomeReceivedJobs();

        assertThat(connection0.sync().clientList()).contains("QueueListener-");
        assertThat(connection1.sync().clientList()).doesNotContain("QueueListener-");
    }

    @Test
    public void queueListenerOnDifferentNode() throws Exception {
        queueListenerFactory.withLocalityTracking().getjobs().subscribe(subscriber);

        createJobs(connection1);
        waitForSomeReceivedJobs();

        assertThat(connection0.sync().clientList()).contains("QueueListener-");
        assertThat(connection1.sync().clientList()).doesNotContain("QueueListener-");
    }

    @Test
    public void unsubscribe() throws Exception {

        Subscription subscription = queueListenerFactory.withLocalityTracking().getjobs().subscribe(subscriber);

        createJobs(connection1);
        waitForSomeReceivedJobs();

        assertThat(subscriber.getOnCompletedEvents()).isEmpty();
        subscription.unsubscribe();
        Thread.sleep(500);

        subscriber.assertNoTerminalEvent();

        assertThat(connection0.sync().clientList()).doesNotContain("QueueListener-");
        assertThat(connection1.sync().clientList()).doesNotContain("QueueListener-");
    }

    @Test
    public void subscribeTwoTimesAtOneTime() throws Exception {

        Observable<Job<String, String>> getjobs = queueListenerFactory.withLocalityTracking().getjobs();
        final TestSubscriber<Job<String, String>> localSubscriber = new TestSubscriber<Job<String, String>>();
        Subscription subscription1 = getjobs.subscribe(subscriber);
        Subscription subscription2 = getjobs.subscribe(localSubscriber);

        createJobs(connection1);

        WaitFor.waitOrTimeout(new Condition() {
            @Override
            public boolean isSatisfied() {
                return subscriber.getOnNextEvents().size() + localSubscriber.getOnNextEvents().size() >= 5;
            }
        }, timeout(Duration.seconds(5)));

        subscription1.unsubscribe();

        createJobs(connection1);
        waitForReceivedJobs(localSubscriber, localSubscriber.getOnNextEvents().size() + 5);
        subscription2.unsubscribe();
        Thread.sleep(500);

        assertThat(subscriber.getOnNextEvents().size()).isGreaterThan(0);
        assertThat(localSubscriber.getOnNextEvents().size()).isGreaterThan(10);

        assertThat(connection0.sync().clientList()).doesNotContain("QueueListener-");
        assertThat(connection1.sync().clientList()).doesNotContain("QueueListener-");
    }

    @Test
    public void twoConnectionsSwitchNode() throws Exception {

        Observable<Job<String, String>> getjobs = queueListenerFactory.withLocalityTracking().getjobs();
        final TestSubscriber<Job<String, String>> localSubscriber = new TestSubscriber<Job<String, String>>();
        Subscription subscription1 = getjobs.subscribe(subscriber);
        Subscription subscription2 = getjobs.subscribe(localSubscriber);

        createJobs(connection1);

        WaitFor.waitOrTimeout(new Condition() {
            @Override
            public boolean isSatisfied() {
                createJobs(connection1);
                return subscriber.getOnNextEvents().size() > 5 && localSubscriber.getOnNextEvents().size() > 5;
            }
        }, timeout(Duration.seconds(5)));

        queueListenerFactory.switchNodes();
        TimeUnit.SECONDS.sleep(1);

        assertThat(connection0.sync().clientList()).doesNotContain("QueueListener-");
        assertThat(connection1.sync().clientList()).contains("QueueListener-");

        subscription1.unsubscribe();
        subscription2.unsubscribe();
    }

    @Test
    public void subscribeTwoTimesOneAfterOne() throws Exception {

        Observable<Job<String, String>> getjobs = queueListenerFactory.withLocalityTracking().getjobs();

        Subscription subscription1 = getjobs.subscribe(subscriber);

        createJobs(connection1);
        waitForSomeReceivedJobs();
        subscription1.unsubscribe();
        createJobs(connection1);

        TestSubscriber<Job<String, String>> localSubscriber = new TestSubscriber<Job<String, String>>();
        Subscription subscription2 = getjobs.subscribe(localSubscriber);
        waitForReceivedJobs(localSubscriber, 5);
        subscription2.unsubscribe();
        Thread.sleep(500);

        assertThat(connection0.sync().clientList()).doesNotContain("QueueListener-");
        assertThat(connection1.sync().clientList()).doesNotContain("QueueListener-");
    }

    @Test
    public void switchToNodeBackground() throws Exception {
        queueListenerFactory.withLocalityTracking().withNodeSwitching(50, TimeUnit.MILLISECONDS)
                .getjobs(100, TimeUnit.MILLISECONDS, 1).subscribe(subscriber);

        createJobs(connection1);
        waitForSomeReceivedJobs();
        TimeUnit.MILLISECONDS.sleep(100);
        createJobs(connection1);
        TimeUnit.SECONDS.sleep(1);

        assertThat(connection0.sync().clientList()).doesNotContain("QueueListener-");
        assertThat(connection1.sync().clientList()).contains("QueueListener-");
    }

    @Test
    public void switchToNodeSetTrigger() throws Exception {
        queueListenerFactory.withLocalityTracking().getjobs(50, TimeUnit.MILLISECONDS, 1).subscribe(subscriber);

        createJobs(connection1);
        waitForSomeReceivedJobs();
        queueListenerFactory.switchNodes();
        TimeUnit.MILLISECONDS.sleep(500);
        createJobs(connection1);
        TimeUnit.SECONDS.sleep(1);

        assertThat(connection0.sync().clientList()).doesNotContain("QueueListener-");
        assertThat(connection1.sync().clientList()).contains("QueueListener-");
    }

    @Test
    public void gracefulClusterLeave() throws Exception {
        connection0.sync().clusterLeaving(false);
        queueListenerFactory.withLocalityTracking().getjobs(50, TimeUnit.MILLISECONDS, 1).subscribe(subscriber);

        createJobs(connection0);
        waitForSomeReceivedJobs();
        connection0.sync().clusterLeaving(true);

        createJobs(connection0);
        waitForSomeReceivedJobs();
        TimeUnit.SECONDS.sleep(1);

        assertThat(connection0.sync().clientList()).doesNotContain("QueueListener-");
        assertThat(connection1.sync().clientList()).contains("QueueListener-");
        connection0.sync().clusterLeaving(false);
    }

    @Test
    public void sharedRedisClient() throws Exception {

        QueueListenerFactory sharedClientListener = QueueListenerFactory.create(client, Schedulers.io(), disqueURI0,
                new Utf8StringCodec(), queue);
        sharedClientListener.getjobs(10, TimeUnit.MILLISECONDS, 1).subscribe(subscriber);

        createJobs(connection0);
        waitForSomeReceivedJobs();

        sharedClientListener.shutdown();
    }

    private void waitForSomeReceivedJobs() throws InterruptedException, TimeoutException {
        waitForReceivedJobs(subscriber, 5);
    }

    private void waitForReceivedJobs(final TestSubscriber<Job<String, String>> subscriber, final int number)
            throws InterruptedException, TimeoutException {

        WaitFor.waitOrTimeout(new Condition() {
            @Override
            public boolean isSatisfied() {
                return subscriber.getOnNextEvents().size() >= number;
            }
        }, timeout(Duration.seconds(5)));

    }

    private void createJobs(DisqueConnection<String, String> connection) {
        for (int i = 0; i < 10; i++) {
            connection.async().addjob(queue, "job-" + i, 1, TimeUnit.MINUTES);
        }
    }
}