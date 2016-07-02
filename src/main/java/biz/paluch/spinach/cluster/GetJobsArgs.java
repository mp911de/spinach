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
package biz.paluch.spinach.cluster;

import java.util.concurrent.TimeUnit;

/**
 * @author Mark Paluch
 */
class GetJobsArgs<Q> {

    private final long timeout;
    private final TimeUnit timeUnit;
    private final long count;
    private final Q[] queues;

    public static <Q> GetJobsArgs<Q> create(long timeout, TimeUnit timeUnit, long count, Q[] queues) {
        return new GetJobsArgs(timeout, timeUnit, count, queues);
    }

    GetJobsArgs(long timeout, TimeUnit timeUnit, long count, Q[] queues) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.count = count;
        this.queues = queues;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long getCount() {
        return count;
    }

    public Q[] getQueues() {
        return queues;
    }
}
