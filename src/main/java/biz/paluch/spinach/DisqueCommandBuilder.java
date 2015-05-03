package biz.paluch.spinach;

import static biz.paluch.spinach.CommandKeyword.*;
import static biz.paluch.spinach.CommandType.*;

import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.output.JobOutput;

import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.output.StatusOutput;
import com.lambdaworks.redis.protocol.CommandArgs;
import com.lambdaworks.redis.protocol.RedisCommand;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @param <K>
 * @param <V>
 */
class DisqueCommandBuilder<K, V> extends BaseCommandBuilder<K, V> {

    public DisqueCommandBuilder(RedisCodec<K, V> codec) {
        super(codec);
    }

    public RedisCommand<K, V, String> addJob(K queue, V job, long duration, TimeUnit timeUnit, AddJobArgs addJobArgs) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).addKey(queue).addValue(job);
        if (timeUnit != null) {
            args.add(timeUnit.toMillis(duration));
        } else {
            args.add(0);
        }

        return createCommand(ADDJOB, new StatusOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, Job<K, V>> getJob(long duration, TimeUnit timeUnit, K... queues) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec);
        if (timeUnit != null) {
            args.add(TIMEOUT).add(timeUnit.toMillis(duration));
        }

        args.add(FROM).addKeys(queues);

        return createCommand(GETJOB, new JobOutput<K, V>(codec), args);
    }

}
