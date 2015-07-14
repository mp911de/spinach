package biz.paluch.spinach.examples;

import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.DisqueClient;
import biz.paluch.spinach.DisqueURI;
import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.api.sync.DisqueCommands;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class Standalone {

    public static void main(String[] args) {
        DisqueClient disqueClient = new DisqueClient(DisqueURI.create("disque://password@localhost:7711"));
        DisqueCommands<String, String> sync = disqueClient.connect().sync();

        sync.addjob("queue", "body", 1, TimeUnit.MINUTES);

        Job<String, String> job = sync.getjob("queue");

        sync.ackjob(job.getId());

        sync.close();
        disqueClient.shutdown();
    }
}
