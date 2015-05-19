package biz.paluch.spinach.impl;

import static biz.paluch.spinach.impl.CommandType.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.AddJobArgs;
import biz.paluch.spinach.Job;
import biz.paluch.spinach.ScanArgs;
import biz.paluch.spinach.output.JobListOutput;
import biz.paluch.spinach.output.JobOutput;

import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.ScanCursor;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.output.IntegerOutput;
import com.lambdaworks.redis.output.KeyScanOutput;
import com.lambdaworks.redis.output.NestedMultiOutput;
import com.lambdaworks.redis.output.StatusOutput;
import com.lambdaworks.redis.protocol.CommandArgs;
import com.lambdaworks.redis.protocol.RedisCommand;

/**
 * @param <K>
 * @param <V>
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class DisqueCommandBuilder<K, V> extends BaseCommandBuilder<K, V> {

    public DisqueCommandBuilder(RedisCodec<K, V> codec) {
        super(codec);
    }

    public RedisCommand<K, V, String> addjob(K queue, V job, long duration, TimeUnit timeUnit, AddJobArgs addJobArgs) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).addKey(queue).addValue(job);
        if (timeUnit != null) {
            args.add(timeUnit.toMillis(duration));
        } else {
            args.add(0);
        }

        if (addJobArgs != null) {
            addJobArgs.build(args);
        }

        return createCommand(ADDJOB, new StatusOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, Job<K, V>> getjob(long duration, TimeUnit timeUnit, K... queues) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec);
        if (timeUnit != null) {
            args.add(CommandKeyword.TIMEOUT).add(timeUnit.toMillis(duration));
        }

        args.add(CommandKeyword.FROM).addKeys(queues);

        return createCommand(GETJOB, new JobOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, List<Job<K, V>>> getjobs(long count, long duration, TimeUnit timeUnit, K... queues) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec);
        if (timeUnit != null && duration > 0) {
            args.add(CommandKeyword.TIMEOUT).add(timeUnit.toMillis(duration));
        }

        if (count > 0) {
            args.add(CommandKeyword.COUNT).add(count);
        }

        args.add(CommandKeyword.FROM).addKeys(queues);

        return createCommand(GETJOB, new JobListOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, String> debugFlushall() {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec);

        args.add(CommandKeyword.FLUSHALL);

        return createCommand(DEBUG, new StatusOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, Long> ackjob(String[] jobIds) {
        CommandArgs<K, V> args = withJobIds(jobIds);

        return createCommand(ACKJOB, new IntegerOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, Long> enqueue(String[] jobIds) {
        CommandArgs<K, V> args = withJobIds(jobIds);

        return createCommand(ENQUEUE, new IntegerOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, Long> dequeue(String[] jobIds) {
        CommandArgs<K, V> args = withJobIds(jobIds);

        return createCommand(DEQUEUE, new IntegerOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, Long> deljob(String[] jobIds) {
        CommandArgs<K, V> args = withJobIds(jobIds);

        return createCommand(DELJOB, new IntegerOutput<K, V>(codec), args);
    }

    private CommandArgs<K, V> withJobIds(String[] jobIds) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec);

        for (String jobId : jobIds) {
            args.add(jobId);
        }
        return args;
    }

    public RedisCommand<K, V, Long> fastack(String[] jobIds) {
        CommandArgs<K, V> args = withJobIds(jobIds);

        return createCommand(FASTACK, new IntegerOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, Long> qlen(K queue) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).addKey(queue);

        return createCommand(QLEN, new IntegerOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, List<Job<K, V>>> qpeek(K queue, long count) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).addKey(queue).add(count);

        return createCommand(QPEEK, new JobListOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, List<Object>> hello() {
        return createCommand(HELLO, new NestedMultiOutput<K, V>(codec));
    }

    public RedisCommand<K, V, List<Object>> show(String jobId) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).add(jobId);

        return createCommand(SHOW, new NestedMultiOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, Long> working(String jobId) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec).add(jobId);

        return createCommand(WORKING, new IntegerOutput<K, V>(codec), args);
    }

    public RedisCommand<K, V, KeyScanCursor<K>> qscan(ScanCursor scanCursor, ScanArgs scanArgs) {
        CommandArgs<K, V> args = new CommandArgs<K, V>(codec);
        if (scanArgs != null) {
            scanArgs.build(args);
        }

        if (scanCursor != null) {
            args.add(scanCursor.getCursor());
        }

        return createCommand(QSCAN, new KeyScanOutput<K, V>(codec), args);
    }
}
