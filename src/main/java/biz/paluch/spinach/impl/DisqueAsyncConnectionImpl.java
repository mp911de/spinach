package biz.paluch.spinach.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.AddJobArgs;
import biz.paluch.spinach.DisqueAsyncConnection;
import biz.paluch.spinach.Job;

import com.lambdaworks.redis.RedisAsyncConnectionImpl;
import com.lambdaworks.redis.RedisChannelWriter;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.codec.RedisCodec;
import io.netty.channel.ChannelHandler;

/**
 * An asynchronous thread-safe connection to a disque server. Multiple threads may share one {@link DisqueAsyncConnectionImpl} A
 * {@link com.lambdaworks.redis.protocol.ConnectionWatchdog} monitors each connection and reconnects automatically until
 * {@link #close} is called. All pending commands will be (re)sent after successful reconnection.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */

@ChannelHandler.Sharable
public class DisqueAsyncConnectionImpl<K, V> extends RedisAsyncConnectionImpl<K, V> implements DisqueAsyncConnection<K, V> {

    protected DisqueCommandBuilder<K, V> commandBuilder;

    public DisqueAsyncConnectionImpl(RedisChannelWriter<K, V> writer, RedisCodec<K, V> codec, long timeout, TimeUnit unit) {
        super(writer, codec, timeout, unit);
        commandBuilder = new DisqueCommandBuilder<K, V>(codec);
    }

    @Override
    public RedisFuture<String> addjob(K queue, V job, long duration, TimeUnit timeUnit) {
        return dispatch(commandBuilder.addjob(queue, job, duration, timeUnit, null));
    }

    @Override
    public RedisFuture<String> addjob(K queue, V job, long duration, TimeUnit timeUnit, AddJobArgs addJobArgs) {
        return dispatch(commandBuilder.addjob(queue, job, duration, timeUnit, addJobArgs));
    }

    @Override
    public RedisFuture<Job<K, V>> getjob(K queue) {
        return dispatch(commandBuilder.getjob(0, null, queue));
    }

    @Override
    public RedisFuture<Job<K, V>> getjob(long duration, TimeUnit timeUnit, K queue) {
        return dispatch(commandBuilder.getjob(duration, timeUnit, queue));
    }

    @Override
    public RedisFuture<List<Job<K, V>>> getjob(K... queues) {
        return dispatch(commandBuilder.getjobs(0, 0, null, queues));
    }

    @Override
    public RedisFuture<List<Job<K, V>>> getjob(long duration, TimeUnit timeUnit, long count, K... queues) {
        return dispatch(commandBuilder.getjobs(count, duration, timeUnit, queues));
    }

    @Override
    public RedisFuture<List<Object>> show(String jobId) {
        return dispatch(commandBuilder.show(jobId));
    }

    @Override
    public RedisFuture<Long> enqueue(String... jobIds) {
        return dispatch(commandBuilder.enqueue(jobIds));
    }

    @Override
    public RedisFuture<Long> dequeue(String... jobIds) {
        return dispatch(commandBuilder.dequeue(jobIds));
    }

    @Override
    public RedisFuture<Long> deljob(String... jobIds) {
        return dispatch(commandBuilder.deljob(jobIds));
    }

    @Override
    public RedisFuture<Long> ackjob(String... jobIds) {
        return dispatch(commandBuilder.ackjob(jobIds));
    }

    @Override
    public RedisFuture<Long> fastack(String... jobIds) {
        return dispatch(commandBuilder.fastack(jobIds));
    }

    @Override
    public RedisFuture<Long> working(String jobId) {
        return dispatch(commandBuilder.working(jobId));
    }

    @Override
    public RedisFuture<Long> qlen(K queue) {
        return dispatch(commandBuilder.qlen(queue));
    }

    @Override
    public RedisFuture<List<Job<K, V>>> qpeek(K queue, long count) {
        return dispatch(commandBuilder.qpeek(queue, count));
    }

    @Override
    public RedisFuture<List<Object>> hello() {
        return dispatch(commandBuilder.hello());
    }

    @Override
    public RedisFuture<String> debugFlushall() {
        return dispatch(commandBuilder.debugFlushall());
    }
}
