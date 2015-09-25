package biz.paluch.spinach.api;

import com.lambdaworks.redis.protocol.LettuceCharsets;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public enum CommandType implements ProtocolKeyword {
    // Jobs
    ADDJOB, GETJOB, ACKJOB, FASTACK, DELJOB, SHOW, JSCAN,

    // Queues
    QLEN, QPEEK, ENQUEUE, NACK, DEQUEUE, QSCAN, WORKING,

    // AOF
    BGREWRITEAOF,

    // Server commands
    AUTH, PING, INFO, SHUTDOWN, DEBUG, CONFIG, CLUSTER, CLIENT, SLOWLOG, TIME, COMMAND, /* LATENCY, */HELLO, QUIT;

    public final byte[] bytes;

    CommandType() {
        bytes = name().getBytes(LettuceCharsets.ASCII);
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

}
