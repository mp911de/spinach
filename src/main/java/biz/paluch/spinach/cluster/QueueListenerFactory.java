package biz.paluch.spinach.cluster;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.internal.LettuceAssert;

import biz.paluch.spinach.DisqueClient;
import biz.paluch.spinach.DisqueURI;
import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.cluster.QueueListener.LocalityAwareConnection;
import biz.paluch.spinach.impl.SocketAddressSupplier;
import biz.paluch.spinach.impl.SocketAddressSupplierFactory;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Cluster-aware Job listener. This listener emits {@link biz.paluch.spinach.api.Job jobs} by listening on one or multiple
 * queues by using an observable subject. Instances are designed to be long-living objects. A {@link QueueListenerFactory} can
 * keep track of the originating cluster node. If the majority of received jobs originate from a node to which the client is not
 * connected to, the {@link QueueListenerFactory} tries to switch the server to minimize latency.
 *
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class QueueListenerFactory<K, V> {

    public static final int DEFAULT_TIMEOUT = 10;
    public static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
    public static final int DEFAULT_COUNT = 1;

    private final Scheduler scheduler;
    private final DisqueClient disqueClient;
    private final boolean sharedClient;
    private final DisqueURI disqueURI;
    private final RedisCodec<K, V> codec;
    private final List<QueueListener<K, V>> resources = new CopyOnWriteArrayList<>();
    private final K[] queues;

    /**
     * @param scheduler the scheduler for blocking operations, must not be {@literal null}
     * @param disqueURI the URI, must not be {@literal null}
     * @param codec use this codec to encode/decode keys and values, must not be {@literal null}
     * @param queues queue names to listen on, must not be {@literal null} and not empty
     */
    protected QueueListenerFactory(Scheduler scheduler, DisqueURI disqueURI, RedisCodec<K, V> codec, K[] queues) {
        this(null, scheduler, disqueURI, codec, queues);
    }

    /**
     * @param disqueClient a shared client instance. Creates a new client instance if {@literal null}
     * @param scheduler the scheduler for blocking operations, must not be {@literal null}
     * @param disqueURI the URI, must not be {@literal null}
     * @param codec use this codec to encode/decode keys and values, must not be {@literal null}
     * @param queues queue names to listen on, must not be {@literal null} and not empty
     */
    protected QueueListenerFactory(DisqueClient disqueClient, Scheduler scheduler, DisqueURI disqueURI, RedisCodec<K, V> codec,
            K[] queues) {

        LettuceAssert.notNull(scheduler, "Scheduler must not be null");
        LettuceAssert.notNull(disqueURI, "DisqueURI must not be null");
        LettuceAssert.notNull(codec, "RedisCodec must not be null");
        LettuceAssert.isTrue(queues != null && queues.length > 0, "Queues must not be empty");

        if (disqueClient != null) {
            sharedClient = true;
            this.disqueClient = disqueClient;
        } else {
            sharedClient = false;
            this.disqueClient = DisqueClient.create();
        }
        this.disqueURI = disqueURI;
        this.codec = codec;
        this.queues = queues;
        this.scheduler = scheduler;
    }

    /**
     * Create a new {@link QueueListenerFactory}. The default {@link Schedulers#io()} scheduler is used for listener
     * notification and I/O operations.
     *
     * @param disqueURI the DisqueURI
     * @param codec use this codec to encode/decode keys and values, must note be {@literal null}
     * @param queues the queue names
     * @param <K> Key type
     * @param <V> Value type
     * @return a new instance of {@link QueueListenerFactory}
     */
    public static <K, V> QueueListenerFactory<K, V> create(DisqueURI disqueURI, RedisCodec<K, V> codec, K... queues) {
        return new QueueListenerFactory<K, V>(Schedulers.io(), disqueURI, codec, queues);
    }

    /**
     * Create a new {@link QueueListenerFactory}. The provided {@code scheduler} is used for listener notification and I/O
     * operations.
     *
     * @param scheduler a scheduler from rxjava for I/O operations
     * @param disqueURI the DisqueURI
     * @param codec use this codec to encode/decode keys and values, must note be {@literal null}
     * @param queues the queue names
     * @param <K> Key type
     * @param <V> Value type
     * @return a new instance of {@link QueueListenerFactory}
     */
    public static <K, V> QueueListenerFactory<K, V> create(Scheduler scheduler, DisqueURI disqueURI, RedisCodec<K, V> codec,
            K... queues) {
        return new QueueListenerFactory<K, V>(scheduler, disqueURI, codec, queues);
    }

    /**
     * Create a new {@link QueueListenerFactory}. The provided {@code scheduler} is used for listener notification and I/O
     * operations.
     *
     * @param disqueClient a shared client instance for reuse
     * @param scheduler a scheduler from rxjava for I/O operations, must not be {@literal null}
     * @param disqueURI the DisqueURI
     * @param codec use this codec to encode/decode keys and values, must note be {@literal null}
     * @param queues the queue names
     * @param <K> Key type
     * @param <V> Value type
     * @return a new instance of {@link QueueListenerFactory}
     */
    public static <K, V> QueueListenerFactory<K, V> create(DisqueClient disqueClient, Scheduler scheduler, DisqueURI disqueURI,
            RedisCodec<K, V> codec, K... queues) {
        return new QueueListenerFactory<K, V>(disqueClient, scheduler, disqueURI, codec, queues);
    }

    /**
     * Get jobs from the specified queues. By default COUNT is 1, so just one job will be returned. A default TIMEOUT of 10
     * MILLISECONDS is used to enable graceful connection shutdown.
     * <p>
     * When there are jobs in more than one of the queues, the command guarantees to return jobs in the order the queues are
     * specified. If COUNT allows more jobs to be returned, queues are scanned again and again in the same order popping more
     * elements.
     * </p>
     * <p>
     * The {@link Observable} emits {@link Job} objects as soon as a job is received from Disque. The terminal event is emitted
     * as soon as the {@link rx.Subscriber subscriber} unsubscribes from the {@link Observable}.
     * </p>
     *
     * @return an Observable that emits {@link Job} elements until the subscriber terminates the subscription
     */
    public Observable<Job<K, V>> getjobs() {
        return new GetJobsBuilder().getjobs();
    }

    /**
     * Get jobs from the specified queues.
     *
     * <p>
     * When there are jobs in more than one of the queues, the command guarantees to return jobs in the order the queues are
     * specified. If COUNT allows more jobs to be returned, queues are scanned again and again in the same order popping more
     * elements.
     * </p>
     * <p>
     * The {@link Observable} emits {@link Job} objects as soon as a job is received from Disque. The terminal event is emitted
     * as soon as the {@link rx.Subscriber subscriber} unsubscribes from the {@link Observable}.
     * </p>
     *
     * @param timeout timeout to wait
     * @param timeUnit timeout unit
     * @param count count of jobs to return
     * @return an Observable that emits {@link Job} elements until the subscriber terminates the subscription
     */
    public Observable<Job<K, V>> getjobs(long timeout, TimeUnit timeUnit, long count) {
        return new GetJobsBuilder().getjobs(timeout, timeUnit, count);
    }

    private QueueListener<K, V> newOnSubscribe(long timeout, TimeUnit timeUnit, long count) {
        Supplier<LocalityAwareConnection<K, V>> connectionSupplier = createDisqueConnectionSupplier();
        QueueListener<K, V> onSubscribe = new QueueListener<K, V>(scheduler, connectionSupplier,
                GetJobsArgs.create(timeout, timeUnit, count, queues));
        resources.add(onSubscribe);
        return onSubscribe;
    }

    /**
     * Create a new GetJobsBuilder with enabled locality tracking.
     * <p>
     * Locality tracking records statistics about the creating node of a job. If the majority of received jobs originate from a
     * different node, the client should consider moving off the current node to the node which created the jobs. This makes a
     * good use of locality.
     * </p>
     *
     * @return the LocalityTrackingGetJobsBuilder.
     */
    public LocalityTrackingGetJobsBuilder withLocalityTracking() {
        return new LocalityTrackingGetJobsBuilder();
    }

    private Supplier<LocalityAwareConnection<K, V>> createDisqueConnectionSupplier() {
        return new Supplier<LocalityAwareConnection<K, V>>() {
            @Override
            public LocalityAwareConnection<K, V> get() {

                final NodeIdAwareSocketAddressSupplier socketAddressSupplier = createSocketAddressSupplier();
                DisqueConnection<K, V> connection = disqueClient.connect(codec, disqueURI, new SocketAddressSupplierFactory() {
                    @Override
                    public SocketAddressSupplier newSupplier(DisqueURI disqueURI) {
                        return socketAddressSupplier;
                    }
                });

                return new LocalityAwareConnection<K, V>(socketAddressSupplier, connection);
            }
        };
    }

    private NodeIdAwareSocketAddressSupplier createSocketAddressSupplier() {
        return new NodeIdAwareSocketAddressSupplier(SocketAddressSupplierFactory.Factories.ROUND_ROBIN.newSupplier(disqueURI));
    }

    /**
     * Shut down the {@link QueueListenerFactory} and close all open connections. Shared clients are not shut down by this
     * method. The instance should be discarded after calling shutdown.
     */
    public void shutdown() {
        shutdown(2, 15, TimeUnit.SECONDS);
    }

    /**
     * Shut down the {@link QueueListenerFactory} and close all open connections. Shared clients are not shut down by this
     * method. The instance should be discarded after calling shutdown.
     *
     * @param quietPeriod the quiet period as described in the documentation
     * @param timeout the maximum amount of time to wait until the executor is shutdown regardless if a task was submitted
     *        during the quiet period
     * @param timeUnit the unit of {@code quietPeriod} and {@code timeout}
     */
    public void shutdown(long quietPeriod, long timeout, TimeUnit timeUnit) {

        // disable all resources to benefit from concurrent shutdowns
        for (QueueListener<K, V> resource : resources) {
            resource.disable();
        }

        for (QueueListener<K, V> resource : resources) {
            resource.close(timeout, timeUnit);
        }
        resources.clear();

        if (!sharedClient) {
            disqueClient.shutdown(quietPeriod, timeout, timeUnit);
        }
    }

    /**
     * Initiates a node switch if neccessary for all produced listeners by this {@link QueueListenerFactory}.
     */
    public void switchNodes() {
        for (QueueListener<K, V> resource : resources) {
            resource.switchNodes();
        }
    }

    /**
     * Builder for the getjobs Queue Listener {@link Observable}.
     */
    public class GetJobsBuilder {

        /**
         * Get jobs from the specified queues. By default COUNT is 1, so just one job will be returned. A default TIMEOUT of 10
         * MILLISECONDS is used to enable graceful connection shutdown.
         *
         * <p>
         * When there are jobs in more than one of the queues, the command guarantees to return jobs in the order the queues are
         * specified. If COUNT allows more jobs to be returned, queues are scanned again and again in the same order popping
         * more elements.
         * </p>
         * <p>
         * The {@link Observable} emits {@link Job} objects as soon as a job is received from Disque. The terminal event is
         * emitted as soon as the {@link rx.Subscriber subscriber} unsubscribes from the {@link Observable}.
         * </p>
         *
         * @return an Observable that emits {@link Job} elements until the subscriber terminates the subscription
         */
        public Observable<Job<K, V>> getjobs() {
            return Observable.create(newOnSubscribe(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT, DEFAULT_COUNT)).observeOn(scheduler);
        }

        /**
         * Get jobs from the specified queues.
         *
         * <p>
         * When there are jobs in more than one of the queues, the command guarantees to return jobs in the order the queues are
         * specified. If COUNT allows more jobs to be returned, queues are scanned again and again in the same order popping
         * more elements.
         * </p>
         * <p>
         * The {@link Observable} emits {@link Job} objects as soon as a job is received from Disque. The terminal event is
         * emitted as soon as the {@link rx.Subscriber subscriber} unsubscribes from the {@link Observable}.
         * </p>
         *
         * @param timeout timeout to wait
         * @param timeUnit timeout unit
         * @param count count of jobs to return
         * @return an Observable that emits {@link Job} elements until the subscriber terminates the subscription
         */
        public Observable<Job<K, V>> getjobs(long timeout, TimeUnit timeUnit, long count) {
            QueueListener<K, V> onSubscribe = newOnSubscribe(timeout, timeUnit, count);
            return Observable.create(onSubscribe).observeOn(scheduler);
        }
    }

    public class LocalityTrackingGetJobsBuilder extends GetJobsBuilder {

        private long interval;
        private TimeUnit timeUnit;
        private boolean withReconnect;

        public LocalityTrackingGetJobsBuilder() {
        }

        private QueueListener<K, V> newOnSubscribe(long timeout, TimeUnit timeUnit, long count) {
            QueueListener<K, V> onSubscribe = QueueListenerFactory.this.newOnSubscribe(timeout, timeUnit, count);
            onSubscribe.withJobLocalityTracking();
            if (withReconnect) {
                onSubscribe.withNodeSwitching(interval, this.timeUnit);
            }
            return onSubscribe;
        }

        /**
         * Get jobs from the specified queues. By default COUNT is 1, so just one job will be returned. If there are no jobs in
         * any of the specified queues the command will block.
         *
         * When there are jobs in more than one of the queues, the command guarantees to return jobs in the order the queues are
         * specified. If COUNT allows more jobs to be returned, queues are scanned again and again in the same order popping
         * more elements.
         *
         * @return an Observable that emits {@link Job} elements until the subscriber terminates the subscription
         */
        public Observable<Job<K, V>> getjobs() {
            return Observable.create(newOnSubscribe(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT, DEFAULT_COUNT)).observeOn(scheduler);
        }

        /**
         * Get jobs from the specified queues. If there are no jobs in any of the specified queues the command will block.
         *
         * When there are jobs in more than one of the queues, the command guarantees to return jobs in the order the queues are
         * specified. If COUNT allows more jobs to be returned, queues are scanned again and again in the same order popping
         * more elements.
         *
         * @param timeout timeout to wait
         * @param timeUnit timeout unit
         * @param count count of jobs to return
         * @return an Observable that emits {@link Job} elements until the subscriber terminates the subscription
         */
        public Observable<Job<K, V>> getjobs(long timeout, TimeUnit timeUnit, long count) {
            QueueListener<K, V> onSubscribe = newOnSubscribe(timeout, timeUnit, count);
            return Observable.create(onSubscribe).observeOn(scheduler);
        }

        /**
         * Enables the periodic node switching based on the
         *
         * @param nodeReconnectCheckInterval interval between node reconnect checks
         * @param nodeReconnectCheckTimeUnit the time unit
         * @return the builder
         */
        public LocalityTrackingGetJobsBuilder withNodeSwitching(long nodeReconnectCheckInterval,
                TimeUnit nodeReconnectCheckTimeUnit) {
            withReconnect = true;
            this.interval = nodeReconnectCheckInterval;
            this.timeUnit = nodeReconnectCheckTimeUnit;
            return this;
        }
    }
}
