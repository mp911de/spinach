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
package biz.paluch.spinach.commands.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.functions.Func1;
import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.api.rx.DisqueReactiveCommands;
import biz.paluch.spinach.commands.JobCommandTest;

/**
 * @author Mark Paluch
 */
public class RxJobCommandTest extends JobCommandTest {

    protected DisqueReactiveCommands<String, String> rx;

    @Before
    public void openConnection() throws Exception {
        disque = RxSyncInvocationHandler.sync(client.connect());
        disque.debugFlushall();
        rx = disque.getConnection().reactive();
    }

    @Test
    public void addJob() throws Exception {

        String result = rx.addjob(queue, value, 5, TimeUnit.SECONDS).toBlocking().first();
        assertThat(result).startsWith("D-");
    }

    @Test
    public void rxChaining() throws Exception {

        addJob();
        long qlen = rx.qlen(queue).toBlocking().first();

        assertThat(qlen).isEqualTo(1);

        final DisqueReactiveCommands<String, String> rx = client.connect().reactive();
        rx.getjob(queue).flatMap(new Func1<Job<String, String>, Observable<Long>>() {
            @Override
            public Observable<Long> call(Job<String, String> job) {
                return rx.ackjob(job.getId());
            }
        }).subscribe();

        assertThat(rx.qlen(queue).toBlocking().first()).isEqualTo(0);

    }

}
