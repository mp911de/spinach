package biz.paluch.spinach.api;

import com.lambdaworks.redis.protocol.LettuceCharsets;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public enum CommandKeyword implements ProtocolKeyword {

    REPLICATE, DELAY, RETRY, TTL, MAXLEN, ASYNC, TIMEOUT, COUNT, FROM, FLUSHALL, BUSYLOOP, MINLEN, IMPORTRATE, GET, SET, RESETSTAT, REWRITE;

    public final byte[] bytes;

    private CommandKeyword() {
        bytes = name().getBytes(LettuceCharsets.ASCII);
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }
}
