package biz.paluch.spinach.cluster;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.api.Job;
import io.netty.util.internal.ConcurrentSet;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;

/**
 * Queue listener create an {@link Observable}. This class creates a connection upon subscription and receives jobs from Disque.
 * Jobs are passed to the {@link Subscriber}. The connection and the used resources are freed upon unsubscription.
 * <p>
 * A {@link QueueListener} can track locality and check periodically whether the connected node is the one which produces the
 * most jobs. If the majority of received jobs originate from a different node, the listener is able to switch nodes.
 * </p>
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class QueueListener<K, V> implements Observable.OnSubscribe<Job<K, V>> {

    public static final int MIN_DISQUE_JOB_ID_LENGTH = 12;
    public static final String JOB_ID_PREFIX = "D-";

    private static final InternalLogger log = InternalLoggerFactory.getInstance(QueueListener.class);
    private static final AtomicInteger queueListenerIds = new AtomicInteger();

    private final AtomicInteger subscriberIds = new AtomicInteger();
    private final int id = queueListenerIds.incrementAndGet();

    private final Scheduler scheduler;
    private final Supplier<LocalityAwareConnection<K, V>> disqueConnectionSupplier;
    private final Set<GetJobsAction<K, V>> actions = new ConcurrentSet<>();
    private final GetJobsArgs<K> getJobsArgs;

    private long improveLocalityInterval = 0;
    private TimeUnit improveLocalityTimeUnit = null;
    private boolean jobLocalityTracking;

    private volatile Subscription reconnectTrigger;

    QueueListener(Scheduler scheduler, Supplier<LocalityAwareConnection<K, V>> disqueConnectionSupplier,
            GetJobsArgs<K> getJobsArgs) {

        this.scheduler = scheduler;
        this.disqueConnectionSupplier = disqueConnectionSupplier;
        this.getJobsArgs = getJobsArgs;
    }

    /**
     * Setup subscriptions when the Observable subscription is set up.
     * 
     * @param subscriber the subscriber
     */
    @Override
    public void call(Subscriber<? super Job<K, V>> subscriber) {

        log.debug("onSubscribe()");
        if (subscriber.isUnsubscribed()) {
            return;
        }

        String subscriberId = getClass().getSimpleName() + "-" + id + "-" + subscriberIds.incrementAndGet();
        subscriber.onStart();

        try {
            Scheduler.Worker worker = scheduler.createWorker();

            GetJobsAction<K, V> getJobsAction = new GetJobsAction<K, V>(disqueConnectionSupplier, subscriberId, subscriber,
                    jobLocalityTracking, getJobsArgs);

            actions.add(getJobsAction);
            Subscription subscription = worker.schedulePeriodically(getJobsAction, 0, 10, TimeUnit.MILLISECONDS);
            getJobsAction.setSelfSubscription(subscription);

            if (improveLocalityTimeUnit != null && improveLocalityInterval > 0 && reconnectTrigger == null) {
                reconnectTrigger = worker.schedulePeriodically(new Action0() {
                    @Override
                    public void call() {
                        switchNodes();
                    }
                }, improveLocalityInterval, improveLocalityInterval, improveLocalityTimeUnit);
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("QueueListener.call caught an exception: {}", e.getMessage(), e);
            }
            subscriber.onError(e);
        }
    }

    /**
     * Disable the queue listeners.
     */
    public void disable() {
        for (GetJobsAction<K, V> getJobsAction : actions) {
            getJobsAction.disable();
        }
    }

    /**
     * Unsubscribe and close the resources.
     * 
     * @param timeout
     * @param timeUnit
     */
    public void close(long timeout, TimeUnit timeUnit) {

        disable();

        for (GetJobsAction<K, V> getJobsAction : actions) {
            getJobsAction.close(timeout, timeUnit);
        }

        if (reconnectTrigger != null) {
            reconnectTrigger.unsubscribe();
            reconnectTrigger = null;
        }
    }

    /**
     * Enable job locality tracking.
     */
    void withJobLocalityTracking() {
        this.jobLocalityTracking = true;
    }

    void withNodeSwitching(long nodeReconnectCheckInterval, TimeUnit nodeReconnectCheckTimeUnit) {
        this.improveLocalityInterval = nodeReconnectCheckInterval;
        this.improveLocalityTimeUnit = nodeReconnectCheckTimeUnit;
    }

    /**
     * Initiate the switch nodes check.
     */
    void switchNodes() {
        for (GetJobsAction action : actions) {
            action.switchNodes();
        }
    }

    static class LocalityAwareConnection<K, V> {
        private final NodeIdAwareSocketAddressSupplier socketAddressSupplier;
        private final DisqueConnection<K, V> connection;

        public LocalityAwareConnection(NodeIdAwareSocketAddressSupplier socketAddressSupplier,
                DisqueConnection<K, V> connection) {
            this.socketAddressSupplier = socketAddressSupplier;
            this.connection = connection;
        }

        public NodeIdAwareSocketAddressSupplier getSocketAddressSupplier() {
            return socketAddressSupplier;
        }

        public DisqueConnection<K, V> getConnection() {
            return connection;
        }
    }

}
