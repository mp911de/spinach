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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import com.lambdaworks.redis.codec.Utf8StringCodec;
import com.lambdaworks.redis.protocol.AsyncCommand;
import org.junit.After;
import org.junit.Test;

import biz.paluch.spinach.api.CommandKeyword;

import com.lambdaworks.redis.output.StatusOutput;

/**
 * @author Mark Paluch
 */
public class DisqueCommandTest {

    private Utf8StringCodec codec = new Utf8StringCodec();
    private DisqueCommand<String, String, String> command = new DisqueCommand<String, String, String>(CommandKeyword.GET,
            new StatusOutput<>(codec), new DisqueCommandArgs<>(codec));
    private AsyncCommand<String, String, String> sut = new AsyncCommand<>(command);

    @After
    public void tearDown() throws Exception {
        // clear interrupted flag
        Thread.interrupted();
    }

    @Test
    public void sucessfulGet() throws Exception {

        sut.getOutput().set(ByteBuffer.wrap("OK".getBytes()));
        sut.complete();
        assertThat(sut.get()).isEqualTo("OK");
    }

    @Test(expected = ExecutionException.class)
    public void exceptionOnGet() throws Exception {

        sut.completeExceptionally(new IllegalStateException());
        sut.get();
    }

    @Test(expected = ExecutionException.class)
    public void exceptionOnOutputError() throws Exception {

        sut.getOutput().setError("blubb");
        sut.complete();
        sut.get();
    }

    @Test
    public void interrupt() throws Exception {

        sut.complete();
        Thread.currentThread().interrupt();
        sut.get();
    }

    @Test(expected = InterruptedException.class)
    public void interruptGetWithTimeout() throws Exception {

        Thread.currentThread().interrupt();
        sut.get();
    }

    @Test(expected = ExecutionException.class)
    public void exceptionOnGetWithTimeout() throws Exception {

        sut.completeExceptionally(new IllegalStateException());
        sut.get();
    }

    @Test(expected = ExecutionException.class)
    public void exceptionOnGetWithTimeoutOutputError() throws Exception {

        sut.getOutput().setError("blubb");
        sut.complete();
        sut.get();
    }
}
