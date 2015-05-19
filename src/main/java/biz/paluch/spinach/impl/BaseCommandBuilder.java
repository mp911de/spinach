package biz.paluch.spinach.impl;

import com.lambdaworks.redis.ScriptOutputType;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.output.BooleanOutput;
import com.lambdaworks.redis.output.IntegerOutput;
import com.lambdaworks.redis.output.StatusOutput;
import com.lambdaworks.redis.protocol.Command;
import com.lambdaworks.redis.protocol.CommandArgs;
import com.lambdaworks.redis.protocol.CommandOutput;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

class BaseCommandBuilder<K, V> {
    protected RedisCodec<K, V> codec;

    public BaseCommandBuilder(RedisCodec<K, V> codec) {
        this.codec = codec;
    }

    protected <T> Command<K, V, T> createCommand(ProtocolKeyword type, CommandOutput<K, V, T> output) {
        return createCommand(type, output, (CommandArgs<K, V>) null);
    }

    protected <T> Command<K, V, T> createCommand(ProtocolKeyword type, CommandOutput<K, V, T> output, CommandArgs<K, V> args) {
        return new Command<K, V, T>(type, output, args);
    }

}