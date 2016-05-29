package biz.paluch.spinach;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.WaitFor;
import com.lambdaworks.redis.event.DefaultEventPublisherOptions;
import com.lambdaworks.redis.event.Event;
import com.lambdaworks.redis.event.EventBus;
import com.lambdaworks.redis.event.metrics.CommandLatencyEvent;
import com.lambdaworks.redis.event.metrics.MetricEventPublisher;
import com.lambdaworks.redis.resource.ClientResources;
import com.lambdaworks.redis.resource.DefaultClientResources;

import biz.paluch.spinach.api.sync.DisqueCommands;
import biz.paluch.spinach.support.FastShutdown;
import rx.Subscription;
import rx.functions.Func1;
import rx.observers.TestSubscriber;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ClientMetricsTest {

    private ClientResources clientResources;
    private DisqueClient disqueClient;
    private DisqueCommands<String, String> disque;

    @Before
    public void before() throws Exception {

        clientResources = new DefaultClientResources.Builder()
                .commandLatencyPublisherOptions(DefaultEventPublisherOptions.create()).build();
        disqueClient = DisqueClient.create(clientResources, DisqueURI.create(TestSettings.host(), TestSettings.port()));
        disque = disqueClient.connect().sync();
    }

    @After
    public void after() throws Exception {
        disque.close();

        FastShutdown.shutdown(disqueClient);
        FastShutdown.shutdown(clientResources);
    }

    @Test
    public void testMetricsEvent() throws Exception {

        EventBus eventBus = clientResources.eventBus();
        MetricEventPublisher publisher = (MetricEventPublisher) ReflectionTestUtils.getField(clientResources,
                "metricEventPublisher");
        publisher.emitMetricsEvent();

        final TestSubscriber<CommandLatencyEvent> subscriber = new TestSubscriber<CommandLatencyEvent>();
        Subscription subscription = eventBus.get().filter(new Func1<Event, Boolean>() {
            @Override
            public Boolean call(Event redisEvent) {

                return redisEvent instanceof CommandLatencyEvent;
            }
        }).cast(CommandLatencyEvent.class).subscribe(subscriber);

        generateTestData();

        publisher.emitMetricsEvent();

        WaitFor.waitOrTimeout(new Condition() {
            @Override
            public boolean isSatisfied() {
                return !subscriber.getOnNextEvents().isEmpty();
            }
        }, timeout(seconds(5)));

        subscription.unsubscribe();

        subscriber.assertValueCount(1);

        CommandLatencyEvent event = subscriber.getOnNextEvents().get(0);

        assertThat(event.getLatencies()).hasSize(2);
        assertThat(event.toString()).contains("local:any ->");
        assertThat(event.toString()).contains("commandType=PING");
    }

    private void generateTestData() {

        for (int i = 0; i < 10; i++) {
            disque.info();
        }

        for (int i = 0; i < 10; i++) {
            disque.ping();
        }
    }
}
