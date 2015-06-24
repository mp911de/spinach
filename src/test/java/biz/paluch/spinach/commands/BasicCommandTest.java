package biz.paluch.spinach.commands;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class BasicCommandTest extends AbstractCommandTest {

    @Test
    public void hello() throws Exception {

        List<Object> result = disque.hello();
        // 1, nodeId, two nested nodes
        assertThat(result).hasSize(4);
    }

    @Test
    public void info() throws Exception {

        String result = disque.ping();
        assertThat(result).isNotEmpty();
    }
}
