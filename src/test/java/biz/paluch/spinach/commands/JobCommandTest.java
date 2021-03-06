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

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.api.AddJobArgs;
import biz.paluch.spinach.api.GetJobArgs;
import biz.paluch.spinach.api.JScanArgs;
import biz.paluch.spinach.api.Job;
import com.lambdaworks.redis.KeyScanCursor;
import org.junit.Test;

/**
 * @author Mark Paluch
 */
public class JobCommandTest extends AbstractCommandTest {

    @Test
    public void addJob() throws Exception {

        String result = disque.addjob(queue, value, 5, TimeUnit.SECONDS);
        assertThat(result).startsWith("D-");
    }

    @Test
    public void addJobWithArgs() throws Exception {

        AddJobArgs args = AddJobArgs.builder().async(true).delay(1).replicate(1).retry(1).ttl(10).maxlen(5).build();
        String result = disque.addjob(queue, value, 5, TimeUnit.SECONDS, args);

        assertThat(result).startsWith("D-");

        assertThat(args.getAsync()).isTrue();
        assertThat(args.getDelay()).isEqualTo(1);
        assertThat(args.getRetry()).isEqualTo(1);
        assertThat(args.getTtl()).isEqualTo(10);

    }

    @Test
    public void addJobWithTimeUnits() throws Exception {

        AddJobArgs args = AddJobArgs.builder().async(true).delay(1, TimeUnit.SECONDS).replicate(1).retry(1, TimeUnit.SECONDS)
                .ttl(10, TimeUnit.MINUTES).build();
        disque.addjob(queue, value, 5, TimeUnit.SECONDS, args);

        assertThat(args.getAsync()).isTrue();
        assertThat(args.getDelay()).isEqualTo(1);
        assertThat(args.getRetry()).isEqualTo(1);
        assertThat(args.getTtl()).isEqualTo(600);

    }

    @Test
    public void getJob() throws Exception {

        String result = disque.addjob(queue, value, 5, TimeUnit.SECONDS);
        long qlen = disque.qlen(queue);

        assertThat(qlen).isEqualTo(1);

        Job<String, String> job = disque.getjob(queue);

        assertThat(job.getQueue()).isEqualTo(queue);
        assertThat(job.getId()).startsWith("D-").isEqualTo(result);
        assertThat(job.getBody()).isEqualTo(value);
        assertThat(job.getCounters()).isEmpty();
    }

    @Test
    public void getJobWithCounters() throws Exception {
        String id = disque.addjob(queue, value, 5, TimeUnit.SECONDS);
        long qlen = disque.qlen(queue);

        assertThat(qlen).isEqualTo(1);

        disque.getjob(queue);
        disque.nack(id);
        disque.getjob(queue);
        disque.nack(id);
        disque.getjob(queue);
        disque.enqueue(id);

        GetJobArgs args = GetJobArgs.builder().withCounters(true).build();
        Job<String, String> job = disque.getjob(args, queue);

        assertThat(job.getQueue()).isEqualTo(queue);
        assertThat(job.getBody()).isEqualTo(value);
        assertThat(job.getCounters()).containsKeys("nacks", "additional-deliveries");
        assertThat(job.getCounters()).containsEntry("nacks", 2L);
        assertThat(job.getCounters()).containsEntry("additional-deliveries", 1L);
    }

    @Test
    public void getJobTwoWithCounters() throws Exception {
        disque.addjob(queue, value, 5, TimeUnit.SECONDS);
        disque.addjob(queue, value, 5, TimeUnit.SECONDS);

        assertThat(disque.qlen(queue)).isEqualTo(2);
        String id1 = disque.getjob(queue).getId();
        String id2 = disque.getjob(queue).getId();
        disque.nack(id1);
        disque.nack(disque.getjob(queue).getId());
        disque.nack(id2);

        disque.getjob(queue);
        disque.getjob(queue);
  
        disque.enqueue(id1);
        disque.enqueue(id2);

        GetJobArgs args = GetJobArgs.builder().withCounters(true).build();
        List<Job<String, String>> getjobs = disque.getjobs(args, 2, queue);

        Job<String, String> job1 = getjobs.get(0);

        assertThat(job1.getId()).isEqualTo(id1); // assume here we got the right job
        assertThat(job1.getQueue()).isEqualTo(queue);
        assertThat(job1.getBody()).isEqualTo(value);
        assertThat(job1.getCounters()).containsKeys("nacks", "additional-deliveries");
        assertThat(job1.getCounters()).containsEntry("nacks", 2L);
        assertThat(job1.getCounters()).containsEntry("additional-deliveries", 1L);
        
        Job<String, String> job2 = getjobs.get(1);

        assertThat(job2.getId()).isEqualTo(id2); // assume here we got the right job
        assertThat(job2.getQueue()).isEqualTo(queue);
        assertThat(job2.getBody()).isEqualTo(value);
        assertThat(job2.getCounters()).containsKeys("nacks", "additional-deliveries");
        assertThat(job2.getCounters()).containsEntry("nacks", 1L);
        assertThat(job2.getCounters()).containsEntry("additional-deliveries", 1L);
    }

    @Test
    public void getJobNohang() throws Exception {
        GetJobArgs args = GetJobArgs.builder().noHang(true).build();
        Job<String, String> job = disque.getjob(args, queue);

        assertThat(job).isNull();
    }

    @Test
    public void getJobs() throws Exception {
        addJob();
        List<Job<String, String>> result = disque.getjobs(queue);

        assertThat(result).hasSize(1);
    }

    @Test
    public void getJobsWithTimeout() throws Exception {
        List<Job<String, String>> result = disque.getjobs(200, TimeUnit.MILLISECONDS, 1, "other-queue");

        assertThat(result).hasSize(0);
    }

    @Test
    public void getJobWithoutJob() throws Exception {

        assertThat(disque.qlen("other-queue")).isEqualTo(0);

        Job<String, String> job = disque.getjob(200, TimeUnit.MILLISECONDS, "other-queue");

        assertThat(job).isNull();
    }

    @Test
    public void ackJob() throws Exception {

        disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        Job<String, String> job = disque.getjob(queue);

        long result = disque.ackjob(job.getId());
        assertThat(result).isEqualTo(1);

        assertThat(disque.qlen(queue)).isEqualTo(0);

    }

    @Test
    public void fastackJob() throws Exception {

        disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        Job<String, String> job = disque.getjob(queue);

        long result = disque.fastack(job.getId());
        assertThat(result).isEqualTo(1);

        assertThat(disque.qlen(queue)).isEqualTo(0);
    }

    @Test
    public void deljob() throws Exception {

        String jobid = disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        long result = disque.deljob(jobid);
        assertThat(result).isEqualTo(1);

        assertThat(disque.qlen(queue)).isEqualTo(0);
    }

    @Test
    public void show() throws Exception {
        String jobid = disque.addjob(queue, value, 2, TimeUnit.SECONDS);

        List<Object> result = disque.show(jobid);
        assertThat(result).isNotEmpty();
    }

    @Test
    public void showNonExistent() throws Exception {
        List<Object> result = disque.show("D-ff010e8a-ld0SEoOs7Pi9PROZmU+w8EQE-05a1AA");
        assertThat(result).hasSize(1);
    }

    @Test
    public void jscan() throws Exception {

        addJobs(1, "q", 120, value);

        KeyScanCursor<String> result = disque.jscan();
        assertThat(result.getKeys().size()).isGreaterThan(99);
        assertThat(result.isFinished()).isFalse();
    }

    @Test
    public void jscanWithArgs() throws Exception {

        addJobs(1, "q", 120, value);

        KeyScanCursor<String> result = disque
                .jscan(JScanArgs.builder().queue("q13").busyloop().jobstates(JScanArgs.JobState.QUEUED).build());
        assertThat(result.getKeys()).hasSize(1);
        assertThat(result.isFinished()).isTrue();

        result = disque.jscan(JScanArgs.builder().build());
        assertThat(result.getKeys().size()).isGreaterThan(99);
        assertThat(result.isFinished()).isFalse();
    }

    @Test
    public void jscanWithContinue() throws Exception {

        addJobs(1, "q", 120, value);

        JScanArgs scanArgs = JScanArgs.builder().count(5).build();
        KeyScanCursor<String> result = disque.jscan(scanArgs);
        assertThat(result.getKeys().size()).isGreaterThan(4);
        result = disque.jscan(result, scanArgs);

        assertThat(result.getKeys().size()).isGreaterThan(4);
        assertThat(result.isFinished()).isFalse();

        result = disque.jscan(scanArgs);
        result = disque.jscan(result);
        assertThat(result.getKeys().size()).isGreaterThan(80);
        assertThat(result.isFinished()).isFalse();
    }

}
