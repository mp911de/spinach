/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package biz.paluch.spinach.impl;

import java.util.Map;

import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.protocol.CommandArgs;
import com.lambdaworks.redis.protocol.CommandKeyword;
import com.lambdaworks.redis.protocol.CommandType;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * @author Mark Paluch
 * @since 24.06.15 08:15
 */
class DisqueCommandArgs<K, V> extends CommandArgs<K, V> {

    public DisqueCommandArgs(RedisCodec<K, V> codec) {
        super(codec);
    }

    @Override
    public DisqueCommandArgs<K, V> add(String s) {
        return (DisqueCommandArgs<K, V>) super.add(s);
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
    public DisqueCommandArgs<K, V> add(long n) {
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

    @Override
    public DisqueCommandArgs<K, V> addKeys(Iterable<K> keys) {
        return (DisqueCommandArgs<K, V>) super.addKeys(keys);
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
    public DisqueCommandArgs<K, V> add(double n) {
        return (DisqueCommandArgs<K, V>) super.add(n);
    }
}
