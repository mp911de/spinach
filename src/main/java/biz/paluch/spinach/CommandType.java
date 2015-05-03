package biz.paluch.spinach;

import com.lambdaworks.redis.protocol.LettuceCharsets;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public enum CommandType implements ProtocolKeyword {
    ADDJOB, GETJOB;

    public final byte[] bytes;

    private CommandType() {
        bytes = name().getBytes(LettuceCharsets.ASCII);
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

}
