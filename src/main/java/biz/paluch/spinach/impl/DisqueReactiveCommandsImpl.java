/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.paluch.spinach.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import rx.Observable;
import biz.paluch.spinach.api.*;
import biz.paluch.spinach.api.rx.DisqueReactiveCommands;

import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.KillArgs;
import com.lambdaworks.redis.ReactiveCommandDispatcher;
import com.lambdaworks.redis.ScanCursor;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.protocol.RedisCommand;

/**
 * Reactive and thread-safe API for a Disque connection.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 * @author Mark Paluch
 */

public class DisqueReactiveCommandsImpl<K, V> implements DisqueReactiveCommands<K, V> {

    protected RedisCodec<K, V> codec;
    protected DisqueCommandBuilder<K, V> commandBuilder;
    protected DisqueConnection<K, V> connection;

    /**
     * Initialize a new instance.
     *
     * @param connection the connection to operate on
     * @param codec the codec for command encoding
     */
    public DisqueReactiveCommandsImpl(DisqueConnection<K, V> connection, RedisCodec<K, V> codec) {
        this.connection = connection;
        this.codec = codec;
        commandBuilder = new DisqueCommandBuilder<K, V>(codec);
    }

    @Override
    public Observable<Long> ackjob(String... jobIds) {
        return createObservable(() -> commandBuilder.ackjob(jobIds));
    }

    @Override
    public Observable<String> addjob(K queue, V job, long commandTimeout, TimeUnit timeUnit) {
        return createObservable(() -> commandBuilder.addjob(queue, job, commandTimeout, timeUnit, null));
    }

    @Override
    public Observable<String> addjob(K queue, V job, long commandTimeout, TimeUnit timeUnit, AddJobArgs addJobArgs) {
        return createObservable(() -> commandBuilder.addjob(queue, job, commandTimeout, timeUnit, addJobArgs));
    }

    @Override
    public Observable<String> auth(String password) {
        return createObservable(() -> commandBuilder.auth(password));
    }

    @Override
    public Observable<String> bgrewriteaof() {
        return createObservable(() -> commandBuilder.bgrewriteaof());
    }

    @Override
    public Observable<K> clientGetname() {
        return createObservable(() -> commandBuilder.clientGetname());
    }

    @Override
    public Observable<Long> clientKill(KillArgs killArgs) {
        return createObservable(() -> commandBuilder.clientKill(killArgs));
    }

    @Override
    public Observable<String> clientKill(String addr) {
        return createObservable(() -> commandBuilder.clientKill(addr));
    }

    @Override
    public Observable<String> clientList() {
        return createObservable(() -> commandBuilder.clientList());
    }

    @Override
    public Observable<String> clientPause(long timeout) {
        return createObservable(() -> commandBuilder.clientPause(timeout));
    }

    @Override
    public Observable<String> clientSetname(String name) {
        return createObservable(() -> commandBuilder.clientSetname(name));
    }

    @Override
    public void close() {
        connection.close();
    }

    @Override
    public Observable<String> clusterForget(String nodeId) {
        return createObservable(() -> commandBuilder.clusterForget(nodeId));
    }

    @Override
    public Observable<String> clusterInfo() {
        return createObservable(() -> commandBuilder.clusterInfo());
    }

    @Override
    public Observable<String> clusterLeaving() {
        return createObservable(() -> commandBuilder.clusterLeaving());
    }

    @Override
    public Observable<String> clusterLeaving(boolean state) {
        return createObservable(() -> commandBuilder.clusterLeaving(state));
    }

    @Override
    public Observable<String> clusterMeet(String ip, int port) {
        return createObservable(() -> commandBuilder.clusterMeet(ip, port));
    }

    @Override
    public Observable<String> clusterMyId() {
        return createObservable(() -> commandBuilder.clusterMyId());
    }

    @Override
    public Observable<String> clusterNodes() {
        return createObservable(() -> commandBuilder.clusterNodes());
    }

    @Override
    public Observable<String> clusterReset(boolean hard) {
        return createObservable(() -> commandBuilder.clusterReset(hard));
    }

    @Override
    public Observable<String> clusterSaveconfig() {
        return createObservable(() -> commandBuilder.clusterSaveconfig());
    }

    @Override
    public Observable<Object> command() {
        return createDissolvingObservable(() -> commandBuilder.command());
    }

    @Override
    public Observable<Long> commandCount() {
        return createObservable(() -> commandBuilder.commandCount());
    }

    @Override
    public Observable<Object> commandInfo(CommandType... commands) {
        String[] stringCommands = new String[commands.length];
        for (int i = 0; i < commands.length; i++) {
            stringCommands[i] = commands[i].name();
        }

        return commandInfo(stringCommands);
    }

    @Override
    public Observable<Object> commandInfo(String... commands) {
        return createDissolvingObservable(() -> commandBuilder.commandInfo(commands));
    }

    @Override
    public Observable<String> configGet(String parameter) {
        return createDissolvingObservable(() -> commandBuilder.configGet(parameter));
    }

    @Override
    public Observable<String> configResetstat() {
        return createObservable(() -> commandBuilder.configResetstat());
    }

    @Override
    public Observable<String> configRewrite() {
        return createObservable(() -> commandBuilder.configRewrite());
    }

    @Override
    public Observable<String> configSet(String parameter, String value) {
        return createObservable(() -> commandBuilder.configSet(parameter, value));
    }

    @Override
    public Observable<String> debugFlushall() {
        return createObservable(() -> commandBuilder.debugFlushall());
    }

    @Override
    public Observable<Long> deljob(String... jobIds) {
        return createObservable(() -> commandBuilder.deljob(jobIds));
    }

    @Override
    public Observable<Long> dequeue(String... jobIds) {
        return createObservable(() -> commandBuilder.dequeue(jobIds));
    }

    @Override
    public Observable<Long> enqueue(String... jobIds) {
        return createObservable(() -> commandBuilder.enqueue(jobIds));
    }

    @Override
    public Observable<Long> fastack(String... jobIds) {
        return createObservable(() -> commandBuilder.fastack(jobIds));
    }

    @Override
    public Observable<Job<K, V>> getjob(GetJobArgs args, K... queues) {
        return createObservable(() -> commandBuilder.getjob(args, queues));
    }

    @Override
    public Observable<Job<K, V>> getjob(K... queues) {
        return createObservable(() -> commandBuilder.getjob(new GetJobArgs(), queues));
    }

    @Override
    public Observable<Job<K, V>> getjob(long duration, TimeUnit timeUnit, K... queues) {
        GetJobArgs args = GetJobArgs.builder().timeout(duration, timeUnit).build();
        return createObservable(() -> commandBuilder.getjob(args, queues));
    }

    @Override
    public Observable<Job<K, V>> getjobs(GetJobArgs args, long count, K... queues) {
        return createDissolvingObservable(() -> commandBuilder.getjobs(count, args, queues));
    }

    @Override
    public Observable<Job<K, V>> getjobs(K... queues) {
        return createDissolvingObservable(() -> commandBuilder.getjob(new GetJobArgs(), queues));
    }

    @Override
    public Observable<Job<K, V>> getjobs(long duration, TimeUnit timeUnit, long count, K... queues) {
        GetJobArgs args = GetJobArgs.builder().timeout(duration, timeUnit).build();
        return createDissolvingObservable(() -> commandBuilder.getjobs(count, args, queues));
    }

    @Override
    public Observable<Object> hello() {
        return createDissolvingObservable(() -> commandBuilder.hello());
    }

    @Override
    public Observable<String> info() {
        return createObservable(() -> commandBuilder.info());
    }

    @Override
    public Observable<String> info(String section) {
        return createObservable(() -> commandBuilder.info(section));
    }

    @Override
    public boolean isOpen() {
        return connection.isOpen();
    }

    @Override
    public Observable<KeyScanCursor<String>> jscan() {
        return createObservable(() -> commandBuilder.jscan(null, null));
    }

    @Override
    public Observable<KeyScanCursor<String>> jscan(JScanArgs<K> scanArgs) {
        return createObservable(() -> commandBuilder.jscan(null, scanArgs));
    }

    @Override
    public Observable<KeyScanCursor<String>> jscan(ScanCursor scanCursor) {
        return createObservable(() -> commandBuilder.jscan(scanCursor, null));
    }

    @Override
    public Observable<KeyScanCursor<String>> jscan(ScanCursor scanCursor, JScanArgs<K> scanArgs) {
        return createObservable(() -> commandBuilder.jscan(scanCursor, scanArgs));
    }

    @Override
    public Observable<Long> nack(String... jobIds) {
        return createObservable(() -> commandBuilder.nack(jobIds));
    }

    @Override
    public Observable<String> pause(K queue, PauseArgs pauseArgs) {
        return createObservable(() -> commandBuilder.pause(queue, pauseArgs));
    }

    @Override
    public Observable<String> ping() {
        return createObservable(() -> commandBuilder.ping());
    }

    @Override
    public Observable<Long> qlen(K queue) {
        return createObservable(() -> commandBuilder.qlen(queue));
    }

    @Override
    public Observable<Job<K, V>> qpeek(K queue, long count) {
        return createDissolvingObservable(() -> commandBuilder.qpeek(queue, count));
    }

    @Override
    public Observable<KeyScanCursor<K>> qscan() {
        return createObservable(() -> commandBuilder.qscan(null, null));
    }

    @Override
    public Observable<KeyScanCursor<K>> qscan(QScanArgs scanArgs) {
        return createObservable(() -> commandBuilder.qscan(null, scanArgs));
    }

    @Override
    public Observable<KeyScanCursor<K>> qscan(ScanCursor scanCursor) {
        return createObservable(() -> commandBuilder.qscan(scanCursor, null));
    }

    @Override
    public Observable<KeyScanCursor<K>> qscan(ScanCursor scanCursor, QScanArgs scanArgs) {
        return createObservable(() -> commandBuilder.qscan(scanCursor, scanArgs));
    }

    @Override
    public Observable<Map<String, Object>> qstat(K queue) {
        return createObservable(() -> commandBuilder.qstat(queue));
    }

    @Override
    public Observable<String> quit() {
        return createObservable(() -> commandBuilder.quit());
    }

    @Override
    public Observable<List<Object>> show(String jobId) {
        return createDissolvingObservable(() -> commandBuilder.show(jobId));
    }

    @Override
    public void shutdown(boolean save) {
        connection.dispatch(commandBuilder.shutdown(save));
    }

    @Override
    public Observable<Object> slowlogGet() {
        return createDissolvingObservable(() -> commandBuilder.slowlogGet());
    }

    @Override
    public Observable<Object> slowlogGet(int count) {
        return createDissolvingObservable(() -> commandBuilder.slowlogGet(count));
    }

    @Override
    public Observable<Long> slowlogLen() {
        return createObservable(() -> commandBuilder.slowlogLen());
    }

    @Override
    public Observable<String> slowlogReset() {
        return createObservable(() -> commandBuilder.slowlogReset());
    }

    @Override
    public Observable<V> time() {
        return createDissolvingObservable(() -> commandBuilder.time());
    }

    @Override
    public Observable<Long> working(String jobId) {
        return createObservable(() -> commandBuilder.working(jobId));
    }

    @Override
    public DisqueConnection<K, V> getConnection() {
        return connection;
    }

    @SuppressWarnings("unchecked")
    protected <T, R> R createDissolvingObservable(Supplier<RedisCommand<K, V, T>> commandSupplier) {
        return (R) Observable.create(new ReactiveCommandDispatcher<>(commandSupplier, connection, true));
    }

    protected <T> Observable<T> createObservable(Supplier<RedisCommand<K, V, T>> commandSupplier) {
        return Observable.create(new ReactiveCommandDispatcher<>(commandSupplier, connection, false));
    }
}
