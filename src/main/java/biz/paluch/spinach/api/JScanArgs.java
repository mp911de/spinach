package biz.paluch.spinach.api;

import static com.lambdaworks.redis.protocol.CommandKeyword.*;

import java.util.Set;

import com.google.common.collect.Sets;
import com.lambdaworks.redis.protocol.CommandArgs;

/**
 * Arguments for scanning queues/jobs.
 * 
 * @param <K> the queue id type.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class JScanArgs<K> {

    private Long count;
    private boolean busyloop;
    private K queue;
    private Set<JobState> jobStates = Sets.newHashSet();

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public boolean isBusyloop() {
        return busyloop;
    }

    public void setBusyloop(boolean busyloop) {
        this.busyloop = busyloop;
    }

    public K getQueue() {
        return queue;
    }

    public void setQueue(K queue) {
        this.queue = queue;
    }

    public Set<JobState> getJobStates() {
        return jobStates;
    }

    public void setJobStates(Set<JobState> jobStates) {
        this.jobStates = jobStates;
    }

    public static <K> Builder builder() {
        return new Builder<K>();
    }

    /**
     * Static builder methods.
     */
    public static class Builder<K> {

        private Long count;
        private boolean busyloop;
        private K queue;
        private Set<JobState> jobStates = Sets.newHashSet();

        /**
         * Utility constructor.
         */
        private Builder() {

        }

        /**
         * Limit result to {@code count} items.
         * 
         * @param count number of items
         * @return the current builder
         */
        public Builder<K> count(long count) {
            this.count = count;
            return this;
        }

        /**
         * Enable blocking loop mode.
         * 
         * @return the current builder
         */
        public Builder<K> busyloop() {
            return busyloop(true);
        }

        /**
         * Enable/disable blocking loop mode
         * 
         * @param busyloop {@literal true} or {@literal false}
         * @return the current builder
         */
        public Builder<K> busyloop(boolean busyloop) {
            this.busyloop = busyloop;
            return this;
        }

        /**
         * Scan a specific queue
         * 
         * @param queue the queue name
         * @return the current builder
         */
        public Builder<K> queue(K queue) {
            this.queue = queue;
            return this;
        }

        /**
         * Limit to specific {@link biz.paluch.spinach.api.JScanArgs.JobState}'s.
         * 
         * @param jobState Array of job states. Duplicate states are omitted.
         * @return the current builder
         */
        public Builder<K> jobstates(JobState... jobState) {
            for (JobState state : jobState) {
                this.jobStates.add(state);
            }
            return this;
        }

        /**
         * Build an instance of {@link JScanArgs}
         * 
         * @return a new instance of {@link JScanArgs}
         */
        public JScanArgs<K> build() {

            JScanArgs<K> result = new JScanArgs<K>();
            result.setBusyloop(busyloop);
            result.setCount(count);
            result.getJobStates().addAll(jobStates);
            result.setQueue(queue);

            return result;

        }
    }

    public <K, V> void build(CommandArgs<K, V> args) {

        // JSCAN [<cursor>] [COUNT <count>] [BUSYLOOP] [QUEUE <queue>] [STATE <state1> STATE <state2> ... STATE <stateN>] [REPLY
        // all|id]

        if (count != null) {
            args.add(COUNT).add(count);
        }

        if (busyloop) {
            args.add(CommandKeyword.BUSYLOOP);
        }

        if (queue != null) {
            args.add(CommandKeyword.QUEUE).addKey((K) queue);
        }

        for (JobState jobState : jobStates) {
            args.add(CommandKeyword.STATE).add(jobState.id);
        }

    }

    public enum JobState {
        /**
         * Waiting to be replicated enough times.
         */
        WAIT_REPL("wait-repl"),

        /**
         * Not acked, not queued, still active job.
         */
        ACTIVE("active"),

        /**
         * Not acked, but queued in this node.
         */
        QUEUED("queued"),

        /**
         * Acked, no longer active, to garbage collect.
         */
        ACKED("acked");

        private final String id;

        JobState(String id) {
            this.id = id;
        }
    }

}
