package biz.paluch.spinach.impl;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.protocol.CommandArgs;
import com.lambdaworks.redis.protocol.CommandKeyword;
import com.lambdaworks.redis.protocol.CommandType;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 24.06.15 08:15
 */
class DisqueCommandArgs<K, V> extends CommandArgs<K, V> {

    private List<String> strings = Lists.newArrayList();

    public DisqueCommandArgs(RedisCodec<K, V> codec) {
        super(codec);
    }

    @Override
    public DisqueCommandArgs<K, V> add(String s) {
        strings.add(s);
        return (DisqueCommandArgs<K, V>) super.add(s);
    }

    public List<String> getStrings() {
        return strings;
    }

    @Override
    public DisqueCommandArgs<K, V> addKey(K key) {
        return (DisqueCommandArgs<K, V>) super.addKey(key);
    }

    @Override
    public DisqueCommandArgs<K, V> addKeys(K... keys) {
        return (DisqueCommandArgs<K, V>) super.addKeys(keys);
    }

    @Override
    public DisqueCommandArgs<K, V> addValue(V value) {
        return (DisqueCommandArgs<K, V>) super.addValue(value);
    }

    @Override
    public DisqueCommandArgs<K, V> addValues(V... values) {
        return (DisqueCommandArgs<K, V>) super.addValues(values);
    }

    @Override
    public DisqueCommandArgs<K, V> add(Map<K, V> map) {
        return (DisqueCommandArgs<K, V>) super.add(map);
    }

    @Override
    public DisqueCommandArgs<K, V> add(long n) {
        return (DisqueCommandArgs<K, V>) super.add(n);
    }

    @Override
    public DisqueCommandArgs<K, V> add(double n) {
        return (DisqueCommandArgs<K, V>) super.add(n);
    }

    @Override
    public DisqueCommandArgs<K, V> add(byte[] value) {
        return (DisqueCommandArgs<K, V>) super.add(value);
    }

    @Override
    public DisqueCommandArgs<K, V> add(CommandKeyword keyword) {
        return (DisqueCommandArgs<K, V>) super.add(keyword);
    }

    @Override
    public DisqueCommandArgs<K, V> add(CommandType type) {
        return (DisqueCommandArgs<K, V>) super.add(type);
    }

    @Override
    public DisqueCommandArgs<K, V> add(ProtocolKeyword keyword) {
        return (DisqueCommandArgs<K, V>) super.add(keyword);
    }
}
