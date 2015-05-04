package biz.paluch.spinach;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.lambdaworks.redis.KeyValue;
import com.lambdaworks.redis.ScoredValue;

public abstract class AbstractCommandTest {
    public static final String host = TestSettings.host();
    public static final int port = TestSettings.port();
    public static final String passwd = TestSettings.password();

    protected static DisqueClient client;
    protected Logger log = Logger.getLogger(getClass());
    protected DisqueConnection<String, String> disque;
    protected String key = "key";
    protected String queue = "queue";
    protected String value = "value";

    @BeforeClass
    public static void setupClient() {
        client = getDisqueClient();
    }

    protected static DisqueClient getDisqueClient() {
        return new DisqueClient(host, port);
    }

    @AfterClass
    public static void shutdownClient() {
        client.shutdown(0, 0, TimeUnit.MILLISECONDS);
    }

    @Before
    public void openConnection() throws Exception {
        disque = client.connect();
        disque.debugFlushall();
    }

    @After
    public void closeConnection() throws Exception {
        disque.close();
    }

    protected List<String> list(String... args) {
        return Arrays.asList(args);
    }

    protected List<Object> list(Object... args) {
        return Arrays.asList(args);
    }

    protected KeyValue<String, String> kv(String key, String value) {
        return new KeyValue<String, String>(key, value);
    }

    protected ScoredValue<String> sv(double score, String value) {
        return new ScoredValue<String>(score, value);
    }

    protected Set<String> set(String... args) {
        return new HashSet<String>(Arrays.asList(args));
    }

}