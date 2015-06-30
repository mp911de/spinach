package biz.paluch.spinach;

import biz.paluch.spinach.api.DisqueConnection;
import biz.paluch.spinach.api.sync.DisqueCommands;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class Example {

    public static void main(String[] args) {
        String nodes = System.getenv("TYND_DISQUE_NODES");
        String auth = System.getenv("TYND_DISQUE_AUTH");
        DisqueClient disqueClient = new DisqueClient("disque://" + auth + "@" + nodes);

        DisqueCommands<String, String> connection = disqueClient.connect().sync();
        System.out.println(connection.ping());
        connection.close();
        disqueClient.shutdown();

    }
}
