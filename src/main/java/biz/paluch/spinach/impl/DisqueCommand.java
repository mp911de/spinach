package biz.paluch.spinach.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.lambdaworks.redis.RedisCommandExecutionException;
import com.lambdaworks.redis.RedisCommandInterruptedException;
import com.lambdaworks.redis.output.CommandOutput;
import com.lambdaworks.redis.protocol.Command;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * Command based on the original lettuce command but the command throws a {@link RedisCommandExecutionException} if Disque
 * reports an error while command execution.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class DisqueCommand<K, V, T> extends Command<K, V, T> {

    public DisqueCommand(ProtocolKeyword type, CommandOutput<K, V, T> output, DisqueCommandArgs<K, V> args) {
        super(type, output, args);
    }

    @Override
    public DisqueCommandArgs<K, V> getArgs() {
        return (DisqueCommandArgs<K, V>) super.getArgs();
    }

}
