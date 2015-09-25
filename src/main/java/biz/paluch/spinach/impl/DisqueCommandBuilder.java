package biz.paluch.spinach.impl;

import static biz.paluch.spinach.api.CommandKeyword.*;
import static biz.paluch.spinach.api.CommandType.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.*;
import biz.paluch.spinach.output.JobListOutput;
import biz.paluch.spinach.output.JobOutput;
import biz.paluch.spinach.output.StringScanOutput;
import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.KillArgs;
import com.lambdaworks.redis.ScanCursor;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.output.*;
import com.lambdaworks.redis.protocol.Command;
import com.lambdaworks.redis.protocol.CommandType;

/**
 * @param <K>
 * @param <V>
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class DisqueCommandBuilder<K, V> extends BaseCommandBuilder<K, V> {

    public DisqueCommandBuilder(RedisCodec<K, V> codec) {
        super(codec);
    }

    public Command<K, V, String> addjob(K queue, V job, long duration, TimeUnit timeUnit, AddJobArgs addJobArgs) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).addKey(queue).addValue(job);
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

    public Command<K, V, Job<K, V>> getjob(long duration, TimeUnit timeUnit, K... queues) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec);
        if (timeUnit != null) {
            args.add(CommandKeyword.TIMEOUT).add(timeUnit.toMillis(duration));
        }

        args.add(CommandKeyword.FROM).addKeys(queues);

        return createCommand(GETJOB, new JobOutput<K, V>(codec), args);
    }

    public Command<K, V, List<Job<K, V>>> getjobs(long count, long duration, TimeUnit timeUnit, K... queues) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec);
        if (timeUnit != null && duration > 0) {
            args.add(CommandKeyword.TIMEOUT).add(timeUnit.toMillis(duration));
        }

        if (count > 0) {
            args.add(CommandKeyword.COUNT).add(count);
        }

        args.add(CommandKeyword.FROM).addKeys(queues);

        return createCommand(GETJOB, new JobListOutput<K, V>(codec), args);
    }

    public Command<K, V, String> debugFlushall() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec);

        args.add(CommandKeyword.FLUSHALL);

        return createCommand(DEBUG, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, Long> ackjob(String[] jobIds) {
        DisqueCommandArgs<K, V> args = withJobIds(jobIds);

        return createCommand(ACKJOB, new IntegerOutput<K, V>(codec), args);
    }

    public Command<K, V, Long> enqueue(String[] jobIds) {
        DisqueCommandArgs<K, V> args = withJobIds(jobIds);

        return createCommand(ENQUEUE, new IntegerOutput<K, V>(codec), args);
    }

    public Command<K, V, Long> dequeue(String[] jobIds) {
        DisqueCommandArgs<K, V> args = withJobIds(jobIds);

        return createCommand(DEQUEUE, new IntegerOutput<K, V>(codec), args);
    }

    public Command<K, V, Long> nack(String[] jobIds) {
        DisqueCommandArgs<K, V> args = withJobIds(jobIds);

        return createCommand(NACK, new IntegerOutput<K, V>(codec), args);
    }

    public Command<K, V, Long> deljob(String[] jobIds) {
        DisqueCommandArgs<K, V> args = withJobIds(jobIds);

        return createCommand(DELJOB, new IntegerOutput<K, V>(codec), args);
    }

    private DisqueCommandArgs<K, V> withJobIds(String[] jobIds) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec);

        for (String jobId : jobIds) {
            args.add(jobId);
        }
        return args;
    }

    public Command<K, V, Long> fastack(String[] jobIds) {
        DisqueCommandArgs<K, V> args = withJobIds(jobIds);

        return createCommand(FASTACK, new IntegerOutput<K, V>(codec), args);
    }

    public Command<K, V, Long> qlen(K queue) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).addKey(queue);

        return createCommand(QLEN, new IntegerOutput<K, V>(codec), args);
    }

    public Command<K, V, List<Job<K, V>>> qpeek(K queue, long count) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).addKey(queue).add(count);

        return createCommand(QPEEK, new JobListOutput<K, V>(codec), args);
    }

    public Command<K, V, List<Object>> hello() {
        return createCommand(HELLO, new NestedMultiOutput<K, V>(codec));
    }

    public Command<K, V, List<Object>> show(String jobId) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(jobId);

        return createCommand(SHOW, new NestedMultiOutput<K, V>(codec), args);
    }

    public Command<K, V, Long> working(String jobId) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(jobId);

        return createCommand(WORKING, new IntegerOutput<K, V>(codec), args);
    }

    public Command<K, V, KeyScanCursor<K>> qscan(ScanCursor scanCursor, QScanArgs scanArgs) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec);
        if (scanArgs != null) {
            scanArgs.build(args);
        }

        if (scanCursor != null) {
            args.add(scanCursor.getCursor());
        }

        return createCommand(QSCAN, new KeyScanOutput<K, V>(codec), args);
    }

    public Command<K, V, KeyScanCursor<String>> jscan(ScanCursor scanCursor, JScanArgs<K> scanArgs) {

        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec);
        if (scanArgs != null) {
            scanArgs.build(args);
        }

        if (scanCursor != null) {
            args.add(scanCursor.getCursor());
        }

        args.add(CommandKeyword.REPLY).add("id");

        return createCommand(JSCAN, new StringScanOutput<K, V>(codec), args);
    }

    public Command<K, V, String> auth(String password) {

        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(password);
        return createCommand(AUTH, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, K> clientGetname() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec)
                .add(com.lambdaworks.redis.protocol.CommandKeyword.GETNAME);
        return createCommand(CLIENT, new KeyOutput<K, V>(codec), args);
    }

    public Command<K, V, String> clientSetname(K name) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(
                com.lambdaworks.redis.protocol.CommandKeyword.SETNAME).addKey(name);
        return createCommand(CLIENT, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> clientKill(String addr) {

        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(
                com.lambdaworks.redis.protocol.CommandKeyword.KILL).add(addr);
        return createCommand(CLIENT, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, Long> clientKill(KillArgs killArgs) {

        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec)
                .add(com.lambdaworks.redis.protocol.CommandKeyword.KILL);
        // killArgs.build(args);
        return createCommand(CLIENT, new IntegerOutput<K, V>(codec), args);
    }

    public Command<K, V, String> clientPause(long timeout) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(
                com.lambdaworks.redis.protocol.CommandKeyword.PAUSE).add(timeout);
        return createCommand(CLIENT, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> clientList() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec)
                .add(com.lambdaworks.redis.protocol.CommandKeyword.LIST);
        return createCommand(CLIENT, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, List<Object>> command() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec);
        return createCommand(COMMAND, new ArrayOutput<K, V>(codec), args);
    }

    public Command<K, V, List<Object>> commandInfo(String... commands) {

        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec);
        args.add(INFO);

        for (String command : commands) {
            args.add(command);
        }

        return createCommand(COMMAND, new ArrayOutput<K, V>(codec), args);
    }

    public Command<K, V, Long> commandCount() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec)
                .add(com.lambdaworks.redis.protocol.CommandKeyword.COUNT);
        return createCommand(COMMAND, new IntegerOutput<K, V>(codec), args);
    }

    public Command<K, V, String> configRewrite() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec)
                .add(com.lambdaworks.redis.protocol.CommandKeyword.REWRITE);
        return createCommand(CONFIG, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, List<String>> configGet(String parameter) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(CommandType.GET).add(parameter);
        return createCommand(CONFIG, new StringListOutput<K, V>(codec), args);
    }

    public Command<K, V, String> configResetstat() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec)
                .add(com.lambdaworks.redis.protocol.CommandKeyword.RESETSTAT);
        return createCommand(CONFIG, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> configSet(String parameter, String value) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(CommandType.SET).add(parameter).add(value);
        return createCommand(CONFIG, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> info() {
        return createCommand(INFO, new StatusOutput<K, V>(codec));
    }

    public Command<K, V, String> info(String section) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(section);
        return createCommand(INFO, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, List<Object>> slowlogGet() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(CommandType.GET);
        return createCommand(SLOWLOG, new NestedMultiOutput<K, V>(codec), args);
    }

    public Command<K, V, List<Object>> slowlogGet(int count) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(CommandType.GET).add(count);
        return createCommand(SLOWLOG, new NestedMultiOutput<K, V>(codec), args);
    }

    public Command<K, V, Long> slowlogLen() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec)
                .add(com.lambdaworks.redis.protocol.CommandKeyword.LEN);
        return createCommand(SLOWLOG, new IntegerOutput<K, V>(codec), args);
    }

    public Command<K, V, String> slowlogReset() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec)
                .add(com.lambdaworks.redis.protocol.CommandKeyword.RESET);
        return createCommand(SLOWLOG, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, List<V>> time() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec);
        return createCommand(TIME, new ValueListOutput<K, V>(codec), args);
    }

    public Command<K, V, String> shutdown(boolean save) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec);
        return createCommand(SHUTDOWN, new StatusOutput<K, V>(codec),
                save ? args.add(CommandType.SAVE) : args.add(com.lambdaworks.redis.protocol.CommandKeyword.NOSAVE));
    }

    public Command<K, V, String> bgrewriteaof() {
        return createCommand(BGREWRITEAOF, new StatusOutput<K, V>(codec));
    }

    public Command<K, V, String> clusterMeet(String ip, int port) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(MEET).add(ip).add(port);
        return createCommand(CLUSTER, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> clusterForget(String nodeId) {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(FORGET).add(nodeId);
        return createCommand(CLUSTER, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> clusterInfo() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(INFO);

        return createCommand(CLUSTER, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> clusterMyId() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(CommandType.MYID);

        return createCommand(CLUSTER, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> clusterNodes() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(NODES);

        return createCommand(CLUSTER, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> clusterReset(boolean hard) {

        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(RESET);
        if (hard) {
            args.add(HARD);
        } else {
            args.add(SOFT);
        }
        return createCommand(CLUSTER, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> clusterSaveconfig() {
        DisqueCommandArgs<K, V> args = new DisqueCommandArgs<K, V>(codec).add(SAVECONFIG);

        return createCommand(CLUSTER, new StatusOutput<K, V>(codec), args);
    }

    public Command<K, V, String> ping() {
        return createCommand(PING, new StatusOutput<K, V>(codec));
    }

    public Command<K, V, String> quit() {
        return createCommand(QUIT, new StatusOutput<K, V>(codec));
    }

}
