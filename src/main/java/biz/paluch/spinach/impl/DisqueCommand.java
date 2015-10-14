package biz.paluch.spinach.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.lambdaworks.redis.RedisCommandExecutionException;
import com.lambdaworks.redis.RedisCommandInterruptedException;
import com.lambdaworks.redis.protocol.Command;
import com.lambdaworks.redis.protocol.CommandOutput;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * Command based on the original lettuce command but the command throws a {@link RedisCommandExecutionException} if Disque
 * reports an error while command execution.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class DisqueCommand<K, V, T> extends Command<K, V, T> {

    protected final ProtocolKeyword type;

    public DisqueCommand(ProtocolKeyword type, CommandOutput<K, V, T> output, DisqueCommandArgs<K, V> args) {
        super(type, output, args);
        this.type = type;
    }

    public T get() throws ExecutionException {
        try {
            this.latch.await();
            if (getException() != null) {
                throw new ExecutionException(this.getException());
            }

            if (output.hasError()) {
                throw new RedisCommandExecutionException(output.getError());
            }

            return output.get();

        } catch (InterruptedException e) {
            throw new RedisCommandInterruptedException(e);
        }
    }

    public T get(long timeout, TimeUnit unit) throws TimeoutException, ExecutionException {
        try {
            if (!this.latch.await(timeout, unit)) {
                throw new TimeoutException("Command timed out");
            }
        } catch (InterruptedException e) {
            throw new RedisCommandInterruptedException(e);
        }

        return get();
    }

    @Override
    public DisqueCommandArgs<K, V> getArgs() {
        return (DisqueCommandArgs<K, V>) super.getArgs();
    }

    public ProtocolKeyword getType() {
        return type;
    }
}
