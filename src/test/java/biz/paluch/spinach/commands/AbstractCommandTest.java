package biz.paluch.spinach.commands;

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

import biz.paluch.spinach.DisqueClient;
import biz.paluch.spinach.TestSettings;
import biz.paluch.spinach.api.sync.DisqueCommands;

import com.lambdaworks.redis.KeyValue;
import com.lambdaworks.redis.ScoredValue;

public abstract class AbstractCommandTest {
    public static final String host = TestSettings.host();
    public static final int port = TestSettings.port();
    public static final String passwd = TestSettings.password();

    protected static DisqueClient client;
    protected Logger log = Logger.getLogger(getClass());
    protected DisqueCommands<String, String> disque;
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
        disque = client.connect().sync();
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

    protected void addJobs(int jobsPerQueue, String queue, int queues, String body) {

        for (int i = 0; i < queues; i++) {
            String queueName = getQueueName(queue, i, queues);
            for (int j = 0; j < jobsPerQueue; j++) {
                disque.addjob(queueName, body, 5, TimeUnit.MINUTES);
            }
        }

    }

    protected String getQueueName(String prefix, int i, int queues) {

        if (queues != 1) {
            return prefix + i;
        }
        return prefix;
    }

}