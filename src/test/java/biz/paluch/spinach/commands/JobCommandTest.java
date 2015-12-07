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
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
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

        disque.nack(id);
        disque.nack(id);

        GetJobArgs args = GetJobArgs.builder().withCounters(true).build();
        Job<String, String> job = disque.getjob(args, queue);

        assertThat(job.getQueue()).isEqualTo(queue);
        assertThat(job.getBody()).isEqualTo(value);
        assertThat(job.getCounters()).containsKeys("nacks", "additional-deliveries");
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

        KeyScanCursor<String> result = disque.jscan(JScanArgs.builder().queue("q13").busyloop()
                .jobstates(JScanArgs.JobState.QUEUED).build());
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
