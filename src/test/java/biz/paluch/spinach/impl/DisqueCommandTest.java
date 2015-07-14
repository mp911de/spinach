package biz.paluch.spinach.impl;

import static org.assertj.core.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import biz.paluch.spinach.api.CommandKeyword;

import com.lambdaworks.redis.RedisCommandExecutionException;
import com.lambdaworks.redis.RedisCommandInterruptedException;
import com.lambdaworks.redis.output.StatusOutput;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class DisqueCommandTest {

    private DisqueCommand<String, String, String> sut = new DisqueCommand<String, String, String>(CommandKeyword.GET,
            new StatusOutput<String, String>(null), new DisqueCommandArgs<String, String>(null));

    @Test
    public void sucessfulGet() throws Exception {

        sut.getOutput().set(ByteBuffer.wrap("OK".getBytes()));
        sut.complete();
        assertThat(sut.get()).isEqualTo("OK");
    }

    @Test(expected = ExecutionException.class)
    public void exceptionOnGet() throws Exception {

        sut.setException(new IllegalStateException());
        sut.complete();
        sut.get();
    }

    @Test(expected = RedisCommandExecutionException.class)
    public void exceptionOnOutputError() throws Exception {

        sut.getOutput().setError("blubb");
        sut.complete();
        sut.get();
    }

    @Test(expected = RedisCommandInterruptedException.class)
    public void interrupt() throws Exception {

        Thread.currentThread().interrupt();
        sut.complete();
        sut.get();
    }

    @Test(expected = TimeoutException.class)
    public void timeoutGet() throws Exception {
        sut.get(1, TimeUnit.MILLISECONDS);
    }

    @Test
    public void sucessfulGetWithTimeout() throws Exception {

        sut.getOutput().set(ByteBuffer.wrap("OK".getBytes()));
        sut.complete();
        assertThat(sut.get(1, TimeUnit.MILLISECONDS)).isEqualTo("OK");
    }

    @Test(expected = RedisCommandInterruptedException.class)
    public void interruptGetWithTimeout() throws Exception {

        Thread.currentThread().interrupt();
        sut.get(1, TimeUnit.MILLISECONDS);
    }

    @Test(expected = ExecutionException.class)
    public void exceptionOnGetWithTimeout() throws Exception {

        sut.setException(new IllegalStateException());
        sut.complete();
        sut.get(1, TimeUnit.SECONDS);
    }

    @Test(expected = RedisCommandExecutionException.class)
    public void exceptionOnGetWithTimeoutOutputError() throws Exception {

        sut.getOutput().setError("blubb");
        sut.complete();
        sut.get(1, TimeUnit.SECONDS);
    }

    @Test
    public void timeout() throws Exception {
        assertThat(sut.await(1, TimeUnit.MILLISECONDS)).isFalse();
    }

    @Test
    public void sucessfulAwait() throws Exception {

        sut.complete();
        assertThat(sut.await(1, TimeUnit.MILLISECONDS)).isTrue();
    }

    @Test(expected = RedisCommandInterruptedException.class)
    public void interruptAwait() throws Exception {

        Thread.currentThread().interrupt();
        assertThat(sut.await(1, TimeUnit.MILLISECONDS)).isTrue();
    }

}