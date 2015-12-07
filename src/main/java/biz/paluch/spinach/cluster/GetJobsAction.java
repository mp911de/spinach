package biz.paluch.spinach.cluster;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.api.sync.DisqueCommands;

import com.google.common.base.Supplier;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.lambdaworks.redis.RedisChannelHandler;
import com.lambdaworks.redis.RedisChannelWriter;
import com.lambdaworks.redis.RedisException;
import com.lambdaworks.redis.protocol.CommandHandler;

import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * Subscription action to emit {@link Job} objects. This action is intended to be {@link #call() called} regularly by a
 * {@link rx.Scheduler} and emits {@link Job jobs} upon reception from Disque.
 * <p>
 * The subscription action allows tracking of the producer nodeId when receiving messages from Disque and a reconnect/locality
 * improvement by switching the Disque node. Instances are stateful and need to be {@link #close(long, TimeUnit)} closed. The
 * shutdown is graceful and waits up to the specified getjobs timeout. The connection is force closed on timeout expiry.
 * </p>
 * 
 */
class GetJobsAction<K, V> implements Action0 {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(GetJobsAction.class);

    private final DisqueConnection<K, V> disqueConnection;
    private final String subscriptionId;
    private final Subscriber<? super Job<K, V>> subscriber;
    private final boolean jobLocalityTracking;
    private final GetJobsArgs<K> getJobsArgs;

    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final AtomicBoolean open = new AtomicBoolean(true);

    private final Multiset<String> nodePrefixes = ConcurrentHashMultiset.create();
    private final NodeIdAwareSocketAddressSupplier socketAddressSupplier;
    private ReentrantLock reentrantLock = new ReentrantLock();

    private volatile boolean switchNodesCheck = false;
    private volatile Subscription schedulerSubscription;

    public GetJobsAction(Supplier<QueueListener.LocalityAwareConnection<K, V>> disqueConnectionSupplier, String subscriptionId,
            Subscriber<? super Job<K, V>> subscriber, boolean jobLocalityTracking, GetJobsArgs<K> getJobsArgs) {

        QueueListener.LocalityAwareConnection<K, V> localityAwareConnection = disqueConnectionSupplier.get();

        this.disqueConnection = localityAwareConnection.getConnection();
        this.socketAddressSupplier = localityAwareConnection.getSocketAddressSupplier();

        this.subscriptionId = subscriptionId;
        this.subscriber = subscriber;
        this.jobLocalityTracking = jobLocalityTracking;
        this.getJobsArgs = getJobsArgs;

        disqueConnection.async().clientSetname(subscriptionId);
    }

    @Override
    public void call() {

        if (!enabled.get()) {
            return;
        }

        if (subscriber.isUnsubscribed() && open.get()) {
            close(getJobsArgs.getTimeout(), getJobsArgs.getTimeUnit());
            return;
        }

        if (switchNodesCheck) {
            switchNodesCheck = false;
            reconnectToNearestProducer(disqueConnection, false);
        }

        if (!disqueConnection.isOpen()) {
            return;
        }

        try {
            reentrantLock.lock();
            if (!open.get()) {
                return;
            }

            try {
                List<Job<K, V>> getjobs = disqueConnection.sync().getjobs(getJobsArgs.getTimeout(), getJobsArgs.getTimeUnit(),
                        getJobsArgs.getCount(), getJobsArgs.getQueues());
                for (Job<K, V> job : getjobs) {
                    trackNodeStats(job.getId());
                    subscriber.onNext(job);
                }
            } catch (RedisException e) {
                if (e.getMessage() != null && e.getMessage().startsWith("LEAVING")) {
                    String nodeIdPrefix = getCurrentNodeIdPrefix();
                    nodePrefixes.remove(nodeIdPrefix);
                    log.info("Received LEAVING from NodeId with prefix {}", nodeIdPrefix);
                    forcedReconnect();
                    return;
                }

                throw e;
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    /**
     * Forced reconnect. Avoid stats biasing the reconnect towards the current host, so clear the stats for the current nodeId.
     */
    private void forcedReconnect() {
        socketAddressSupplier.reloadNodes();
        reconnectToNearestProducer(disqueConnection, true);
    }

    protected void setSelfSubscription(Subscription self) {
        this.schedulerSubscription = self;
    }

    /**
     * Unsubscribe and try to close the resources gracefully. If the grace period times out (minimum of 100 ms, max timeout plus
     * 100ms), the connection is closed forcibly.
     *
     * @param timeout
     * @param timeUnit
     */
    void close(long timeout, TimeUnit timeUnit) {
        disable();

        try {
            if (!reentrantLock.tryLock(timeout, timeUnit)) {
                log.warn("Could not gracefully close the subscription connection " + subscriptionId + " within " + timeout
                        + " " + timeUnit + ", forcing close of connection");
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            if (reentrantLock.isLocked() && reentrantLock.isHeldByCurrentThread()) {
                reentrantLock.unlock();
            }
        }

        closeConnection();
        unsubscribe();
    }

    /**
     * Close the connection.
     */
    private void closeConnection() {
        if (open.compareAndSet(true, false)) {
            disqueConnection.close();
        }
    }

    /**
     * Disable the listener.
     */
    protected void disable() {
        enabled.compareAndSet(true, false);
    }

    /**
     * Unsubscribe from the scheduler.
     */
    protected void unsubscribe() {
        if (schedulerSubscription != null) {
            schedulerSubscription.unsubscribe();
            schedulerSubscription = null;
        }
    }

    private void trackNodeStats(String id) {
        if (jobLocalityTracking && id.length() >= QueueListener.DISQUE_JOB_ID_LENGTH
                && id.startsWith(QueueListener.JOB_ID_PREFIX) && id.endsWith(QueueListener.JOB_ID_SUFFIX)) {
            String nodePrefix = getNodeIdPrefix(id);
            nodePrefixes.add(nodePrefix);
        }
    }

    protected static String getNodeIdPrefix(String id) {
        return id.substring(2, 10);
    }

    private void reconnectToNearestProducer(DisqueConnection<K, V> disqueConnection, boolean forcedReconnect) {
        log.debug("reconnectToNearestProducer()");
        Set<Multiset.Entry<String>> stats = Multisets.copyHighestCountFirst(nodePrefixes).entrySet();
        nodePrefixes.clear();

        if (!isNodeSwitchNecessary(stats) && !forcedReconnect) {
            return;
        }

        String nodeIdPrefix = getNodeIdPrefix(stats);
        if (nodeIdPrefix != null) {
            log.debug("Set preferred node prefix to {}", nodeIdPrefix);
            socketAddressSupplier.setPreferredNodeIdPrefix(nodeIdPrefix);
        }

        if (disqueConnection.isOpen()) {
            if (nodeIdPrefix == null) {
                log.info("Initiating reconnect");
            } else {
                log.info("Initiating reconnect to preferred node with prefix {}", nodeIdPrefix);
            }
            disconnect((RedisChannelHandler<?, ?>) disqueConnection);
        }
    }

    /**
     * Arm check for switch nodes.
     */
    void switchNodes() {
        switchNodesCheck = true;
    }

    /**
     * Disconnect the channel. Why {@link Channel#disconnect()} and not {@link DisqueCommands#quit()}? The quit command produces
     * an {@code OK} result which is read by the channel. Sometimes the pipeline is not finished with reading but the disconnect
     * is already initiated and this raises an {@link java.io.IOException}.
     *
     * The {@link Channel} is retrieved by reflection because it is not exposed.
     *
     * @param redisChannelHandler the channel handler.
     */
    private void disconnect(RedisChannelHandler<?, ?> redisChannelHandler) {

        RedisChannelHandler<?, ?> rch = redisChannelHandler;
        RedisChannelWriter<?, ?> channelWriter = rch.getChannelWriter();

        try {
            Field field = CommandHandler.class.getDeclaredField("channel");
            field.setAccessible(true);
            Channel o = (Channel) field.get(channelWriter);
            if (o != null && o.isOpen()) {
                o.disconnect();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot look up/retrieve the channel from the " + CommandHandler.class.getName());
        }
    }

    private boolean isNodeSwitchNecessary(Set<Multiset.Entry<String>> stats) {
        if (stats.isEmpty()) {
            return false;
        }

        String nodeIdPrefix = getNodeIdPrefix(stats);

        if (nodeIdPrefix == null) {
            return true;
        }

        if (isConnectedToNode(nodeIdPrefix)) {
            return false;
        }

        if (socketAddressSupplier.getPreferredNodeIdPrefix() == null
                || !nodeIdPrefix.equals(socketAddressSupplier.getPreferredNodeIdPrefix())) {
            return true;
        }

        return false;
    }

    private String getNodeIdPrefix(Set<Multiset.Entry<String>> entries) {

        if (entries.isEmpty()) {
            return null;
        }

        Multiset.Entry<String> entry = entries.iterator().next();
        return entry.getElement();
    }

    private boolean isConnectedToNode(String nodeIdPrefix) {
        return socketAddressSupplier.getCurrentNodeId() != null
                && socketAddressSupplier.getCurrentNodeId().startsWith(nodeIdPrefix);
    }

    private String getCurrentNodeIdPrefix() {
        if (socketAddressSupplier.getCurrentNodeId() != null) {
            return getNodeIdPrefix(socketAddressSupplier.getCurrentNodeId());
        }
        return getNodeIdPrefix(disqueConnection.sync().clusterMyId());
    }

}
