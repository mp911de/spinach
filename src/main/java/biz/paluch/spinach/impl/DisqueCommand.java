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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.lambdaworks.redis.RedisCommandExecutionException;
import com.lambdaworks.redis.RedisCommandInterruptedException;
import com.lambdaworks.redis.output.CommandOutput;
import com.lambdaworks.redis.protocol.Command;
import com.lambdaworks.redis.protocol.ProtocolKeyword;

/**
 * Command based on the original lettuce command but the command throws a {@link RedisCommandExecutionException} if Disque
 * reports an error while command execution.
 * 
 * @author Mark Paluch
 */
class DisqueCommand<K, V, T> extends Command<K, V, T> {

    public DisqueCommand(ProtocolKeyword type, CommandOutput<K, V, T> output, DisqueCommandArgs<K, V> args) {
        super(type, output, args);
    }

    @Override
    public DisqueCommandArgs<K, V> getArgs() {
        return (DisqueCommandArgs<K, V>) super.getArgs();
    }

}
