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
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
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
