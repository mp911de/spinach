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
package biz.paluch.spinach.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;

import biz.paluch.spinach.api.CommandType;

import com.lambdaworks.redis.RedisCommandExecutionException;
import com.lambdaworks.redis.models.command.CommandDetail;
import com.lambdaworks.redis.models.command.CommandDetailParser;

public class ServerCommandTest extends AbstractCommandTest {


    @Test
    public void bgrewriteaof() throws Exception {
        String msg = "Background append only file rewriting";
        assertThat(disque.bgrewriteaof(), containsString(msg));
    }

    @Test
    public void clientGetSetname() throws Exception {
        assertThat(disque.clientGetname()).isNull();
        assertThat(disque.clientSetname("test")).isEqualTo("OK");
        assertThat(disque.clientGetname()).isEqualTo("test");
        assertThat(disque.clientSetname("")).isEqualTo("OK");
        assertThat(disque.clientGetname()).isNull();
    }

    @Test
    public void clientPause() throws Exception {
        assertThat(disque.clientPause(1000)).isEqualTo("OK");
    }

    @Test
    public void clientKill() throws Exception {
        Pattern p = Pattern.compile(".*addr=([^ ]+).*");
        String clients = disque.clientList();
        Matcher m = p.matcher(clients);

        assertThat(m.lookingAt()).isTrue();
        assertThat(disque.clientKill(m.group(1))).isEqualTo("OK");
    }

    @Test(expected = RedisCommandExecutionException.class)
    public void clientKillUnknown() throws Exception {
        disque.clientKill("afdsfads");
    }

    @Test
    public void clientList() throws Exception {
        assertThat(disque.clientList().contains("addr=")).isTrue();
    }

    @Test
    public void commandCount() throws Exception {
        assertThat(disque.commandCount()).isGreaterThan(20);
    }

    @Test
    public void command() throws Exception {

        List<Object> result = disque.command();

        assertThat(result.size()).isGreaterThan(10);

        List<CommandDetail> commands = CommandDetailParser.parse(result);
        assertThat(commands).hasSameSizeAs(result);
    }

    @Test
    public void commandInfo() throws Exception {

        List<Object> result = disque.commandInfo(CommandType.ACKJOB);

        assertThat(result.size()).isEqualTo(1);

        List<CommandDetail> commands = CommandDetailParser.parse(result);
        assertThat(commands).hasSameSizeAs(result);

        result = disque.commandInfo("a missing command");

        assertThat(result.size()).isEqualTo(0);

    }

    @Test
    public void configGet() throws Exception {
        assertThat(disque.configGet("appendfsync")).isEqualTo(list("appendfsync", "everysec"));
    }

    @Test
    public void configResetstat() throws Exception {
        assertThat(disque.configResetstat()).isEqualTo("OK");
    }

    @Test
    @Ignore("server crashes, disabling therefore")
    public void configSet() throws Exception {
        String maxmemory = disque.configGet("maxmemory").get(1);
        assertThat(disque.configSet("maxmemory", "1024")).isEqualTo("OK");
        assertThat(disque.configGet("maxmemory").get(1)).isEqualTo("1024");
        disque.configSet("maxmemory", maxmemory);
    }

    @Test
    public void configRewrite() throws Exception {

        String result = disque.configRewrite();
        assertThat(result).isEqualTo("OK");
    }

    @Test
    public void info() throws Exception {
        assertThat(disque.info().contains("disque_version")).isTrue();
        assertThat(disque.info("server").contains("disque_version")).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void slowlog() throws Exception {
        long start = System.currentTimeMillis() / 1000;

        assertThat(disque.configSet("slowlog-log-slower-than", "1")).isEqualTo("OK");
        assertThat(disque.slowlogReset()).isEqualTo("OK");
        disque.qlen(key);

        List<Object> log = disque.slowlogGet();
        assertThat(log).hasSize(2);

        List<Object> entry = (List<Object>) log.get(0);
        assertThat(entry).hasSize(4);
        assertThat(entry.get(0) instanceof Long).isTrue();
        assertThat((Long) entry.get(1) >= start).isTrue();
        assertThat(entry.get(2) instanceof Long).isTrue();
        assertThat(entry.get(3)).isEqualTo(list("QLEN", key));

        entry = (List<Object>) log.get(1);
        assertThat(entry).hasSize(4);
        assertThat(entry.get(0) instanceof Long).isTrue();
        assertThat((Long) entry.get(1) >= start).isTrue();
        assertThat(entry.get(2) instanceof Long).isTrue();
        assertThat(entry.get(3)).isEqualTo(list("SLOWLOG", "RESET"));

        assertThat(disque.slowlogGet(1)).hasSize(1);
        assertThat((long) disque.slowlogLen()).isGreaterThanOrEqualTo(4);

        disque.configSet("slowlog-log-slower-than", "0");
    }

    @Test
    public void time() throws Exception {
        assertThat(disque.time()).hasSize(2);
    }

}