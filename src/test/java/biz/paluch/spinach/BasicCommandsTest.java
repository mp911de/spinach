package biz.paluch.spinach;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class BasicCommandsTest extends AbstractCommandTest {

    @Test
    public void hello() throws Exception {

        List<Object> result = disque.hello();
        // 1, nodeId, two nested nodes
        assertThat(result).hasSize(4);
    }

    @Test
    public void info() throws Exception {

        String result = disque.info();
        assertThat(result).isNotEmpty();
    }
}
