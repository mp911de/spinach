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
import static org.assertj.core.api.Fail.fail;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import biz.paluch.spinach.DisqueURI;
import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.api.PauseArgs;
import biz.paluch.spinach.api.QScanArgs;

import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.RedisCommandExecutionException;
import com.lambdaworks.redis.RedisException;

/**
 * @author Mark Paluch
 */
public class QueueCommandTest extends AbstractCommandTest {

    @Test
    public void enqueue() throws Exception {

        String jobid = disque.addjob(queue, value, 2, TimeUnit.SECONDS);

        disque.getjob(queue);

        assertThat(disque.qlen(queue)).isEqualTo(0);

        long result = disque.enqueue(jobid);
        assertThat(result).isEqualTo(1);

        assertThat(disque.qlen(queue)).isEqualTo(1);
    }

    @Test
    public void nack() throws Exception {

        String jobid = disque.addjob(queue, value, 2, TimeUnit.SECONDS);

        disque.getjob(queue);

        long result = disque.nack(jobid);
        assertThat(result).isEqualTo(1);

        assertThat(disque.qlen(queue)).isEqualTo(1);
    }

    @Test
    public void dequeue() throws Exception {

        String jobid = disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        long result = disque.dequeue(jobid);
        assertThat(result).isEqualTo(1);

        assertThat(disque.qlen(queue)).isEqualTo(0);
    }

    @Test
    public void working() throws Exception {

        disque.addjob(queue, value, 5, TimeUnit.SECONDS);
        Job<String, String> job = disque.getjob(queue);

        long result = disque.working(job.getId());

        assertThat(result).isGreaterThan(1);
    }

    @Test(expected = RedisException.class)
    public void workingWithoutJob() throws Exception {
        disque.working("D-ff010e8a-ld0SEoOs7Pi9PROZmU+w8EQE-05a1AA");
    }

    @Test
    public void pause() throws Exception {

        String pause = disque.pause(queue, PauseArgs.builder().none().build());
        assertThat(pause).isEqualToIgnoringCase("none");

        pause = disque.pause(queue, PauseArgs.builder().in().out().none().all().bcast().build());
        assertThat(pause).isEqualToIgnoringCase("all");

        pause = disque.pause(queue, PauseArgs.builder().none().bcast().build());
        assertThat(pause).isEqualToIgnoringCase("none");

        pause = disque.pause(queue, PauseArgs.builder().state().build());
        assertThat(pause).isEqualToIgnoringCase("none");
    }

    @Test
    public void pauseOperations() throws Exception {

        disque.pause(queue, PauseArgs.builder().all().bcast().build());

        try {
            disque.addjob(queue, "job", 1, TimeUnit.MINUTES);
            fail("Missing RedisCommandExecutionException");
        } catch (RedisCommandExecutionException e) {
            assertThat(e).hasMessageContaining("PAUSED");
        }

        assertThat(disque.getjob(1, TimeUnit.SECONDS, queue)).isNull();

        disque.pause(queue, PauseArgs.builder().none().bcast().build());
    }

    @Test
    public void qpeek() throws Exception {

        disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        disque.addjob(queue, value, 2, TimeUnit.SECONDS);

        List<Job<String, String>> result = disque.qpeek(queue, 2);

        assertThat(result).hasSize(2);

        Job<String, String> job1 = result.get(0);

        assertThat(job1.getBody()).isEqualTo(value);
        assertThat(job1.getQueue()).isEqualTo(queue);
        assertThat(job1.getId()).startsWith("D-");

        Job<String, String> job2 = result.get(1);

        assertThat(job2.getBody()).isEqualTo(value);
        assertThat(job2.getQueue()).isEqualTo(queue);
    }

    @Test
    public void qscan() throws Exception {

        addJobs(1, "q", 100, value);

        KeyScanCursor<String> result = disque.qscan();
        assertThat(result.getKeys()).hasSize(100);
    }

    @Test
    public void qscanWithArgs() throws Exception {

        addJobs(1, "q", 120, value);

        KeyScanCursor<String> result = disque.qscan(QScanArgs.builder().count(5).build());
        assertThat(result.getKeys().size()).isGreaterThan(4).isLessThan(99);
        assertThat(result.isFinished()).isFalse();

        result = disque.qscan(QScanArgs.builder().importrate(0).maxlen(1).maxlen(10).build());
        assertThat(result.getKeys().size()).isGreaterThan(99);

        assertThat(result.isFinished()).isFalse();
    }

    @Test
    public void qscanWithContinue() throws Exception {

        addJobs(1, "q", 100, value);

        QScanArgs scanArgs = QScanArgs.builder().count(5).build();
        KeyScanCursor<String> result = disque.qscan(scanArgs);
        assertThat(result.getKeys().size()).isGreaterThan(4);
        result = disque.qscan(result, scanArgs);

        assertThat(result.getKeys().size()).isGreaterThan(4);
        assertThat(result.isFinished()).isFalse();

        result = disque.qscan(scanArgs);
        result = disque.qscan(result);
        assertThat(result.getKeys().size()).isGreaterThan(80);
        assertThat(result.isFinished()).isTrue();
    }

    @Test
    public void qstat() throws Exception {
        client.connect(DisqueURI.create("localhost", 7712)).sync().addjob(queue, "job", 1, TimeUnit.MINUTES);
        client.connect(DisqueURI.create("localhost", 7713)).sync().addjob(queue, "job", 1, TimeUnit.MINUTES);
        disque.getjob(1, TimeUnit.SECONDS, queue);
        disque.getjob(1, TimeUnit.SECONDS, queue);

        Map<String, Object> qstat = disque.qstat(queue);
        assertThat(qstat.get("import-from")).isInstanceOf(List.class);
        assertThat(qstat.get("jobs-in")).isInstanceOf(Number.class); // "jobs-in" and "jobs-out" come are returned after
                                                                     // "import-from", so check that there is no bug in parsing
                                                                     // the output
        assertThat(qstat.get("jobs-out")).isInstanceOf(Number.class);
        assertThat(qstat).isNotNull().containsEntry("name", queue);
        assertThat(qstat.size()).isGreaterThan(2);

        Map<String, Object> doNotExistResult = disque.qstat("i-do-not-exist");
        assertThat(doNotExistResult).isNotNull().isEmpty();
    }
}
