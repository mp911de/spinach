package biz.paluch.spinach.api;

import com.lambdaworks.redis.protocol.LettuceCharsets;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public enum CommandKeyword implements ProtocolKeyword {

    ALL, ASYNC, BCAST, BLOCKING, BUSYLOOP, COUNT, DELAY, FLUSHALL, FORGET, FROM, GET, HARD, ID, IMPORTRATE, IN, LEAVING,

    MAXLEN, MEET, MINLEN, NODES, NOHANG, NONE, OUT, QUEUE, REPLICATE, REPLY, RESET, RESETSTAT, RETRY,

    REWRITE, SAVECONFIG, SET, SOFT, STATE, TIMEOUT, TTL, WITHCOUNTERS;

    public final byte[] bytes;

    private CommandKeyword() {
        bytes = name().getBytes(LettuceCharsets.ASCII);
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }
}
