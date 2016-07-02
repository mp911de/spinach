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
package biz.paluch.spinach.api;

import com.lambdaworks.redis.protocol.LettuceCharsets;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * @author Mark Paluch
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
