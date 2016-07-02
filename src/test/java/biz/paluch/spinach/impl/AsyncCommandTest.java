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

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.protocol.AsyncCommand;
import org.junit.Test;

import biz.paluch.spinach.commands.AbstractCommandTest;

import com.lambdaworks.redis.RedisCommandExecutionException;
import com.lambdaworks.redis.RedisFuture;

/**
 * @author Mark Paluch
 * @since 24.06.15 09:04
 */
public class AsyncCommandTest extends AbstractCommandTest {

    @Test(expected = ExecutionException.class)
    public void asyncThrowsExecutionException() throws Exception {
        disque.getConnection().async().clientKill("do not exist").get();
    }

    @Test
    public void testAsyncCommand() throws Exception {
        RedisFuture<String> ping = disque.getConnection().async().ping();
        assertThat(ping).isInstanceOf(AsyncCommand.class);
        assertThat(ping.isCancelled()).isFalse();
        assertThat(ping.getError()).isNull();
        assertThat(ping.get(1, TimeUnit.MINUTES)).isEqualTo("PONG");

    }

}
