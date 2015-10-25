package biz.paluch.spinach.api;

import com.lambdaworks.redis.protocol.CommandArgs;

import java.util.concurrent.TimeUnit;

/**
 * Arguments for adding a job.
 *
 * @author <a href="mailto:karl@heapanalytics.com">Karl-Aksel Puulmann</a>
 */
public class GetJobArgs {

    private Boolean noHang;
    private Long timeout;
    private Boolean withCounters;

    /**
     *
     * @return {@literal true} if the command should return immediately if there are no jobs in the queue.
     */
    public Boolean getNoHang() {
        return noHang;
    }

    public void setNoHang(Boolean noHang) {
        this.noHang = noHang;
    }

    /**
     *
     * @return the maximal timeout for waiting in milliseconds until the command comes back with either a job or an empty
     *         result.
     */
    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    /**
     *
     * @return {@literal true} if the command should return counters related to the job
     */
    public Boolean getWithCounters() {
        return withCounters;
    }

    public void setWithCounters(Boolean withCounters) {
        this.withCounters = withCounters;
    }

    /**
     * Create a new builder populated with the current settings.
     * 
     * @return a new {@link biz.paluch.spinach.api.GetJobArgs.Builder}
     */
    public Builder copyBuilder() {
        return GetJobArgs.builder().noHang(noHang).timeout(timeout).withCounters(withCounters);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Static builder methods.
     */
    public static class Builder {

        private Boolean noHang;
        private Long timeout;
        private Long count;
        private Boolean withCounters;

        /**
         * Utility constructor.
         */
        private Builder() {

        }

        /**
         * Set if the command should return immediately if there are no jobs in the queue.
         * 
         * @param noHang {@literal true} if the command should return immediately if there are no jobs in the queue.
         * @return the builder
         */
        public Builder noHang(Boolean noHang) {
            this.noHang = noHang;
            return this;
        }

        /**
         * Set the maximal timeout for waiting until the command comes back with either a job or an empty result.
         * 
         * @param timeoutMs the timeout in milliseconds
         * @return the builder
         */
        public Builder timeout(Long timeoutMs) {
            this.timeout = timeoutMs;
            return this;
        }

        /**
         * Set the maximal timeout for waiting until the command comes back with either a job or an empty result.
         * 
         * @param timeout timeout duration
         * @param timeUnit timeout unit
         * @return the builder
         */
        public Builder timeout(long timeout, TimeUnit timeUnit) {
            this.timeout = timeUnit.toMillis(timeout);
            return this;
        }

        /**
         * Set whether the command should return counters related to the job
         * 
         * @param withCounters {@literal true} if the command should return counters related to the job
         * @return the builder
         */
        public Builder withCounters(Boolean withCounters) {
            this.withCounters = withCounters;
            return this;
        }

        /**
         * Create a new builder for {@link GetJobArgs}.
         * 
         * @return a new builder for {@link GetJobArgs}
         */
        public GetJobArgs build() {
            GetJobArgs getJobArgs = new GetJobArgs();
            getJobArgs.setNoHang(noHang);
            getJobArgs.setTimeout(timeout);
            getJobArgs.setWithCounters(withCounters);
            return getJobArgs;

        }
    }

    /**
     * Build argument sequence and populate {@code args}.
     *
     * @param args the target command args, must not be {@literal null}
     * @param count the count
     * @param queues the queue names
     */
    public <K, V> void build(CommandArgs<K, V> args, Long count, K... queues) {

        // GETJOB [NOHANG] [TIMEOUT <ms-timeout>] [COUNT <count>] [WITHCOUNTERS] FROM queue1 queue2 ... queueN

        if (noHang != null && noHang.booleanValue()) {
            args.add(CommandKeyword.NOHANG.bytes);
        }

        if (timeout != null) {
            args.add(CommandKeyword.TIMEOUT.bytes).add(timeout);
        }

        if (count != null && count.longValue() != 1L) {
            args.add(CommandKeyword.COUNT.bytes).add(count);
        }

        if (withCounters != null && withCounters.booleanValue()) {
            args.add(CommandKeyword.WITHCOUNTERS.bytes);
        }
        args.add(CommandKeyword.FROM.bytes).addKeys(queues);
    }

}
