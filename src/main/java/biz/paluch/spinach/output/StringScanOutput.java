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
package biz.paluch.spinach.output;

import java.nio.ByteBuffer;

import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.output.ScanOutput;

/**
 * Output handler for string-based {@code SCAN} commands.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author Mark Paluch
 */
public class StringScanOutput<K, V> extends ScanOutput<K, V, KeyScanCursor<String>> {

    public StringScanOutput(RedisCodec<K, V> codec) {
        super(codec, new KeyScanCursor<String>());
    }

    @Override
    protected void setOutput(ByteBuffer bytes) {
        output.getKeys().add(bytes == null ? null : decodeAscii(bytes));
    }

}
