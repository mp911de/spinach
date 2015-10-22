package biz.paluch.spinach.impl;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.CommandType;
import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.api.async.DisqueAsyncCommands;
import biz.paluch.spinach.api.rx.DisqueReactiveCommands;
import biz.paluch.spinach.api.sync.DisqueCommands;

import com.google.common.util.concurrent.MoreExecutors;
import com.lambdaworks.redis.AbstractRedisClient;
import com.lambdaworks.redis.RedisChannelHandler;
import com.lambdaworks.redis.RedisChannelWriter;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.protocol.CommandKeyword;
import com.lambdaworks.redis.protocol.RedisCommand;
import io.netty.channel.ChannelHandler;

/**
 * An thread-safe connection to a disque server. Multiple threads may share one {@link DisqueConnectionImpl}. A
 * {@link com.lambdaworks.redis.protocol.ConnectionWatchdog} monitors each connection and reconnects automatically until
 * {@link #close} is called. All pending commands will be (re)sent after successful reconnection.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */

@ChannelHandler.Sharable
public class DisqueConnectionImpl<K, V> extends RedisChannelHandler<K, V> implements DisqueConnection<K, V> {

    protected RedisCodec<K, V> codec;
    protected DisqueCommands<K, V> sync;
    protected DisqueAsyncCommandsImpl<K, V> async;
    protected DisqueReactiveCommandsImpl<K, V> reactive;

    private char[] password;
    private String clientName;

    /**
     * Initialize a new connection.
     *
     * @param writer the channel writer
     * @param codec Codec used to encode/decode keys and values.
     * @param timeout Maximum time to wait for a response.
     * @param unit Unit of time for the timeout.
     */
    public DisqueConnectionImpl(RedisChannelWriter<K, V> writer, RedisCodec<K, V> codec, long timeout, TimeUnit unit) {
        super(writer, timeout, unit);
        this.codec = codec;
    }

    public DisqueAsyncCommands<K, V> async() {
        return getAsyncCommands();
    }

    protected DisqueAsyncCommandsImpl<K, V> getAsyncCommands() {
        if (async == null) {
            async = newDisqueAsyncCommandsImpl();
        }

        return async;
    }

    /**
     * Create a new instance of {@link DisqueAsyncCommandsImpl}. Can be overridden to extend.
     *
     * @return a new instance
     */
    protected DisqueAsyncCommandsImpl<K, V> newDisqueAsyncCommandsImpl() {
        return new DisqueAsyncCommandsImpl<K, V>(this, codec);
    }

    @Override
    public DisqueReactiveCommands<K, V> reactive() {
        if (reactive == null) {
            reactive = newDisqueReactiveCommandsImpl();
        }

        return reactive;
    }

    /**
     * Create a new instance of {@link DisqueReactiveCommandsImpl}. Can be overridden to extend.
     *
     * @return a new instance
     */
    protected DisqueReactiveCommandsImpl<K, V> newDisqueReactiveCommandsImpl() {
        return new DisqueReactiveCommandsImpl<K, V>(this, codec);
    }

    public DisqueCommands<K, V> sync() {
        if (sync == null) {
            sync = syncHandler(async(), DisqueCommands.class);
        }
        return sync;
    }

    @Override
    public TimeUnit getTimeoutUnit() {
        return unit;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public <T> RedisCommand<K, V, T> dispatch(RedisCommand<K, V, T> cmd) {

        if (cmd instanceof DisqueCommand) {
            final DisqueCommand<K, V, T> local = (DisqueCommand<K, V, T>) cmd;

            if (local.getType() == CommandType.AUTH) {
                local.addListener(new Runnable() {
                    @Override
                    public void run() {
                        if ("OK".equals(local.getOutput().get())) {
                            DisqueConnectionImpl.this.password = local.getArgs().getStrings().get(0).toCharArray();
                        }
                    }
                }, MoreExecutors.sameThreadExecutor());
            }

            if (local.getType() == CommandType.CLIENT && local.getArgs() != null
                    && local.getArgs().getKeywords().indexOf(CommandKeyword.SETNAME) == 0) {
                local.addListener(new Runnable() {
                    @Override
                    public void run() {
                        if ("OK".equals(local.getOutput().get())) {
                            DisqueConnectionImpl.this.clientName = local.getArgs().getStrings().get(0);
                        }
                    }
                }, MoreExecutors.sameThreadExecutor());
            }
        }

        return super.dispatch(cmd);
    }

    protected <T> T syncHandler(Object asyncApi, Class<?>... interfaces) {
        FutureSyncInvocationHandler<K, V> h = new FutureSyncInvocationHandler<K, V>(this, asyncApi);
        return (T) Proxy.newProxyInstance(AbstractRedisClient.class.getClassLoader(), interfaces, h);
    }

    @Override
    public void activated() {

        super.activated();
        // do not block in here, since the channel flow will be interrupted.
        if (password != null) {
            getAsyncCommands().auth(new String(password));
        }

        if (clientName != null) {
            getAsyncCommands().clientSetname(clientName);
        }

    }
}
