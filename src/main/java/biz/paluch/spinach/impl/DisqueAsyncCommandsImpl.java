package biz.paluch.spinach.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.*;
import biz.paluch.spinach.api.async.DisqueAsyncCommands;

import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.KillArgs;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.ScanCursor;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.protocol.RedisCommand;

/**
 * An asynchronous and thread-safe API for a Disque connection.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */

public class DisqueAsyncCommandsImpl<K, V> implements DisqueAsyncCommands<K, V> {

    protected RedisCodec<K, V> codec;
    protected DisqueCommandBuilder<K, V> commandBuilder;
    protected DisqueConnection<K, V> connection;

    /**
     * Initialize a new instance.
     *
     * @param connection the connection to operate on
     * @param codec the codec for command encoding
     */
    public DisqueAsyncCommandsImpl(DisqueConnection<K, V> connection, RedisCodec<K, V> codec) {
        this.connection = connection;
        this.codec = codec;
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
    public RedisFuture<Job<K, V>> getjob(K... queues) {
        GetJobArgs args = GetJobArgs.builder().noHang(true).build();
        return dispatch(commandBuilder.getjob(args, queues));
    }

    @Override
    public RedisFuture<Job<K, V>> getjob(long duration, TimeUnit timeUnit, K... queues) {
        GetJobArgs args = GetJobArgs.builder().timeout(duration, timeUnit).build();
        return dispatch(commandBuilder.getjob(args, queues));
    }

    @Override
    public RedisFuture<Job<K, V>> getjob(GetJobArgs args, K... queues) {
        return dispatch(commandBuilder.getjob(args, queues));
    }

    @Override
    public RedisFuture<List<Job<K, V>>> getjobs(K... queues) {
        GetJobArgs args = GetJobArgs.builder().noHang(true).build();
        return dispatch(commandBuilder.getjobs(1L, args, queues));
    }

    @Override
    public RedisFuture<List<Job<K, V>>> getjobs(long duration, TimeUnit timeUnit, long count, K... queues) {
        GetJobArgs args = GetJobArgs.builder()
                .timeout(duration, timeUnit)
                .count(count)
                .build();
        return dispatch(commandBuilder.getjobs(count, args, queues));
    }

    @Override
    public RedisFuture<List<Job<K, V>>> getjobs(GetJobArgs args, long count, K... queues) {
        return dispatch(commandBuilder.getjobs(count, args, queues));
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
    public RedisFuture<Long> nack(String... jobIds) {
        return dispatch(commandBuilder.nack(jobIds));
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

    @Override
    public RedisFuture<KeyScanCursor<K>> qscan() {
        return dispatch(commandBuilder.qscan(null, null));
    }

    @Override
    public RedisFuture<KeyScanCursor<K>> qscan(QScanArgs scanArgs) {
        return dispatch(commandBuilder.qscan(null, scanArgs));
    }

    @Override
    public RedisFuture<KeyScanCursor<K>> qscan(ScanCursor scanCursor, QScanArgs scanArgs) {
        return dispatch(commandBuilder.qscan(scanCursor, scanArgs));
    }

    @Override
    public RedisFuture<KeyScanCursor<K>> qscan(ScanCursor scanCursor) {
        return dispatch(commandBuilder.qscan(scanCursor, null));
    }

    @Override
    public RedisFuture<KeyScanCursor<String>> jscan() {
        return dispatch(commandBuilder.jscan(null, null));
    }

    @Override
    public RedisFuture<KeyScanCursor<String>> jscan(JScanArgs<K> scanArgs) {
        return dispatch(commandBuilder.jscan(null, scanArgs));
    }

    @Override
    public RedisFuture<KeyScanCursor<String>> jscan(ScanCursor scanCursor, JScanArgs<K> scanArgs) {
        return dispatch(commandBuilder.jscan(scanCursor, scanArgs));
    }

    @Override
    public RedisFuture<KeyScanCursor<String>> jscan(ScanCursor scanCursor) {
        return dispatch(commandBuilder.jscan(scanCursor, null));
    }

    public <T> RedisCommand<K, V, T> dispatch(RedisCommand<K, V, T> cmd) {
        return connection.dispatch(cmd);
    }

    @Override
    public RedisFuture<String> auth(String password) {
        return dispatch(commandBuilder.auth(password));
    }

    @Override
    public RedisFuture<String> ping() {
        return dispatch(commandBuilder.ping());
    }

    @Override
    public RedisFuture<String> quit() {
        return dispatch(commandBuilder.quit());
    }

    @Override
    public RedisFuture<K> clientGetname() {
        return dispatch(commandBuilder.clientGetname());
    }

    @Override
    public RedisFuture<String> clientSetname(String name) {
        return dispatch(commandBuilder.clientSetname(name));
    }

    @Override
    public RedisFuture<String> clientKill(String addr) {
        return dispatch(commandBuilder.clientKill(addr));
    }

    @Override
    public RedisFuture<Long> clientKill(KillArgs killArgs) {
        return dispatch(commandBuilder.clientKill(killArgs));
    }

    @Override
    public RedisFuture<String> clientPause(long timeout) {
        return dispatch(commandBuilder.clientPause(timeout));
    }

    @Override
    public RedisFuture<String> clientList() {
        return dispatch(commandBuilder.clientList());
    }

    @Override
    public RedisFuture<List<Object>> command() {
        return dispatch(commandBuilder.command());
    }

    @Override
    public RedisFuture<List<Object>> commandInfo(String... commands) {
        return dispatch(commandBuilder.commandInfo(commands));
    }

    @Override
    public RedisFuture<List<Object>> commandInfo(CommandType... commands) {
        String[] stringCommands = new String[commands.length];
        for (int i = 0; i < commands.length; i++) {
            stringCommands[i] = commands[i].name();
        }

        return commandInfo(stringCommands);
    }

    @Override
    public RedisFuture<Long> commandCount() {
        return dispatch(commandBuilder.commandCount());
    }

    @Override
    public RedisFuture<List<String>> configGet(String parameter) {
        return dispatch(commandBuilder.configGet(parameter));
    }

    @Override
    public RedisFuture<String> configResetstat() {
        return dispatch(commandBuilder.configResetstat());
    }

    @Override
    public RedisFuture<String> configSet(String parameter, String value) {
        return dispatch(commandBuilder.configSet(parameter, value));
    }

    @Override
    public RedisFuture<String> configRewrite() {
        return dispatch(commandBuilder.configRewrite());
    }

    @Override
    public RedisFuture<List<V>> time() {
        return dispatch(commandBuilder.time());
    }

    @Override
    public RedisFuture<List<Object>> slowlogGet() {
        return dispatch(commandBuilder.slowlogGet());
    }

    @Override
    public RedisFuture<List<Object>> slowlogGet(int count) {
        return dispatch(commandBuilder.slowlogGet(count));
    }

    @Override
    public RedisFuture<Long> slowlogLen() {
        return dispatch(commandBuilder.slowlogLen());
    }

    @Override
    public RedisFuture<String> slowlogReset() {
        return dispatch(commandBuilder.slowlogReset());
    }

    @Override
    public RedisFuture<String> info() {
        return dispatch(commandBuilder.info());
    }

    @Override
    public RedisFuture<String> info(String section) {
        return dispatch(commandBuilder.info(section));
    }

    @Override
    public void shutdown(boolean save) {
        dispatch(commandBuilder.shutdown(save));
    }

    @Override
    public RedisFuture<String> bgrewriteaof() {
        return dispatch(commandBuilder.bgrewriteaof());
    }

    @Override
    public RedisFuture<String> clusterMeet(String ip, int port) {
        return dispatch(commandBuilder.clusterMeet(ip, port));
    }

    @Override
    public RedisFuture<String> clusterForget(String nodeId) {
        return dispatch(commandBuilder.clusterForget(nodeId));
    }

    @Override
    public RedisFuture<String> clusterInfo() {
        return dispatch(commandBuilder.clusterInfo());
    }

    @Override
    public RedisFuture<String> clusterMyId() {
        return dispatch(commandBuilder.clusterMyId());
    }

    @Override
    public RedisFuture<String> clusterNodes() {
        return dispatch(commandBuilder.clusterNodes());
    }

    @Override
    public RedisFuture<String> clusterReset(boolean hard) {
        return dispatch(commandBuilder.clusterReset(hard));
    }

    @Override
    public RedisFuture<String> clusterSaveconfig() {
        return dispatch(commandBuilder.clusterSaveconfig());
    }

    @Override
    public void close() {
        connection.close();
    }

    @Override
    public boolean isOpen() {
        return connection.isOpen();
    }

    @Override
    public DisqueConnection<K, V> getConnection() {
        return connection;
    }
}
