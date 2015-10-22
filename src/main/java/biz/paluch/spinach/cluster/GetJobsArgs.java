package biz.paluch.spinach.cluster;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
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
