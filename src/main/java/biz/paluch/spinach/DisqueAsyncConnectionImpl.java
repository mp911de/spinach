package biz.paluch.spinach;

import java.util.concurrent.TimeUnit;

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
    public RedisFuture<String> addJob(K queue, V job, long duration, TimeUnit timeUnit) {
        return dispatch(commandBuilder.addJob(queue, job, duration, timeUnit, null));
    }

    @Override
    public RedisFuture<String> addJob(K queue, V job, long duration, TimeUnit timeUnit, AddJobArgs addJobArgs) {
        return dispatch(commandBuilder.addJob(queue, job, duration, timeUnit, addJobArgs));
    }

    @Override
    public RedisFuture<Job<K, V>> getJob(K queue) {
        return dispatch(commandBuilder.getJob(0, null, queue));
    }

    @Override
    public RedisFuture<Job<K, V>> getJob(long duration, TimeUnit timeUnit, K queue) {
        return dispatch(commandBuilder.getJob(duration, timeUnit, queue));
    }
}
