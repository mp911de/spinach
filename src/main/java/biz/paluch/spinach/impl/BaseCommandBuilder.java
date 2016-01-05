package biz.paluch.spinach.impl;

import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.output.CommandOutput;
import com.lambdaworks.redis.protocol.Command;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

class BaseCommandBuilder<K, V> {
    protected RedisCodec<K, V> codec;

    public BaseCommandBuilder(RedisCodec<K, V> codec) {
        this.codec = codec;
    }

    protected <T> Command<K, V, T> createCommand(ProtocolKeyword type, CommandOutput<K, V, T> output) {
        return createCommand(type, output, null);
    }

    protected <T> Command<K, V, T> createCommand(ProtocolKeyword type, CommandOutput<K, V, T> output,
            DisqueCommandArgs<K, V> args) {
        return new DisqueCommand<K, V, T>(type, output, args);
    }

}