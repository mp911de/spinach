package biz.paluch.spinach;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class JobTest extends AbstractCommandTest {

    @Test
    public void addJob() throws Exception {

        String result = disque.addJob(queue, value, 0, TimeUnit.SECONDS);
        assertThat(result).startsWith("DI").endsWith("SQ");
    }

    @Test
    public void getJob() throws Exception {

        String result = disque.addJob(queue, value, 0, TimeUnit.SECONDS);
        Job job = disque.getJob(queue);

        assertThat(job.getQueue()).isEqualTo(queue);
        assertThat(job.getId()).startsWith("DI").endsWith("SQ");
        assertThat(job.getBody()).isEqualTo(value);
    }

}
