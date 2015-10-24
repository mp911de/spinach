package biz.paluch.spinach.api;

import com.lambdaworks.redis.protocol.LettuceCharsets;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public enum CommandKeyword implements ProtocolKeyword {

    REPLICATE, BLOCKING, DELAY, RETRY, TTL, MAXLEN, ASYNC, TIMEOUT, QUEUE, COUNT, FROM, FLUSHALL, NOHANG, WITHCOUNTERS,

    BUSYLOOP, MINLEN, RESET, NODES, MEET, FORGET, HARD, SOFT, REPLY, ALL, ID, IMPORTRATE, GET, SET, RESETSTAT, REWRITE, STATE, SAVECONFIG;

    public final byte[] bytes;

    private CommandKeyword() {
        bytes = name().getBytes(LettuceCharsets.ASCII);
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }
}
