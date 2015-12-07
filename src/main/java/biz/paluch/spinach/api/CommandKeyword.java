package biz.paluch.spinach.api;

import com.lambdaworks.redis.protocol.LettuceCharsets;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public enum CommandKeyword implements ProtocolKeyword {

    ALL, ASYNC, BLOCKING, BUSYLOOP, COUNT, DELAY, FLUSHALL, FORGET, FROM, GET, HARD, ID, IMPORTRATE, LEAVING,

    MAXLEN, MEET, MINLEN, NODES, NOHANG, QUEUE, REPLICATE, REPLY, RESET, RESETSTAT, RETRY,

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
