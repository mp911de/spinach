package biz.paluch.spinach;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.lambdaworks.redis.RedisException;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class JobTest extends AbstractCommandTest {

    @Test
    public void addJob() throws Exception {

        String result = disque.addjob(queue, value, 5, TimeUnit.SECONDS);
        assertThat(result).startsWith("DI").endsWith("SQ");
    }

    @Test
    public void addJobWithArgs() throws Exception {

        AddJobArgs args = AddJobArgs.builder().async(true).delay(1).replicate(1).retry(1, TimeUnit.SECONDS)
                .ttl(10, TimeUnit.MINUTES).build();
        String result = disque.addjob(queue, value, 5, TimeUnit.SECONDS, args);

        assertThat(result).startsWith("DI").endsWith("SQ");

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

        Job job = disque.getjob(queue);

        assertThat(job.getQueue()).isEqualTo(queue);
        assertThat(job.getId()).startsWith("DI").endsWith("SQ").isEqualTo(result);
        assertThat(job.getBody()).isEqualTo(value);
    }

    @Test
    public void working() throws Exception {

        disque.addjob(queue, value, 5, TimeUnit.SECONDS);
        Job job = disque.getjob(queue);

        long result = disque.working(job.getId());

        assertThat(result).isGreaterThan(1);
    }

    @Test(expected = RedisException.class)
    public void workingWithoutJob() throws Exception {
        disque.working("DIb043347c89a98df8dea195c47bcd715f2f78ee7705a1SQ");
    }

    @Test
    public void getJobWithoutJob() throws Exception {

        assertThat(disque.qlen(queue)).isEqualTo(0);

        Job job = disque.getjob(1, TimeUnit.SECONDS, queue);

        assertThat(job).isNull();
    }

    @Test
    public void ackJob() throws Exception {

        disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        Job job = disque.getjob(queue);

        long result = disque.ackjob(job.getId());
        assertThat(result).isEqualTo(1);

        assertThat(disque.qlen(queue)).isEqualTo(0);

    }

    @Test
    public void fastackJob() throws Exception {

        disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        Job job = disque.getjob(queue);

        long result = disque.fastack(job.getId());
        assertThat(result).isEqualTo(1);

        assertThat(disque.qlen(queue)).isEqualTo(0);

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
        assertThat(job1.getId()).startsWith("DI");

        Job<String, String> job2 = result.get(1);

        assertThat(job2.getBody()).isEqualTo(value);
        assertThat(job2.getQueue()).isEqualTo(queue);
        assertThat(job2.getId()).startsWith("DI");
    }

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
    public void dequeue() throws Exception {

        String jobid = disque.addjob(queue, value, 2, TimeUnit.SECONDS);
        long result = disque.dequeue(jobid);
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

        List<Object> result = disque.show("DI81c186ad5ea6105d040bb684ddbcb117af91716605s0SQ");
        assertThat(result).hasSize(1);
    }

}
