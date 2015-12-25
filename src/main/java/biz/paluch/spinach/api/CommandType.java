package biz.paluch.spinach.api;

import com.lambdaworks.redis.protocol.LettuceCharsets;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public enum CommandType implements ProtocolKeyword {
    // Jobs
    ADDJOB, ACKJOB, DELJOB, FASTACK, GETJOB, JSCAN, SHOW,

    // Queues
    ENQUEUE, DEQUEUE, NACK, PAUSE, QLEN, QPEEK, QSCAN, QSTAT, WORKING,

    // AOF
    BGREWRITEAOF,

    // Server commands
    AUTH, CONFIG, CLUSTER, CLIENT, COMMAND, DEBUG, INFO, /* LATENCY, */HELLO, PING, QUIT, SHUTDOWN, SLOWLOG, TIME;

    public final byte[] bytes;

    CommandType() {
        bytes = name().getBytes(LettuceCharsets.ASCII);
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

}
