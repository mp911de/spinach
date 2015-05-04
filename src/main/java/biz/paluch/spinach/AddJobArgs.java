package biz.paluch.spinach;

import static com.lambdaworks.redis.protocol.CommandKeyword.*;

import java.util.concurrent.TimeUnit;

import biz.paluch.spinach.impl.CommandKeyword;
import com.lambdaworks.redis.protocol.CommandArgs;

/**
 * Arguments for adding a job.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class AddJobArgs {

    private Integer replicate;
    private Long delay;
    private Long retry;
    private Long ttl;
    private Integer maxlen;
    private Boolean async;

    public Integer getReplicate() {
        return replicate;
    }

    public void setReplicate(Integer replicate) {
        this.replicate = replicate;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Long getRetry() {
        return retry;
    }

    public void setRetry(Long retry) {
        this.retry = retry;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public Integer getMaxlen() {
        return maxlen;
    }

    public void setMaxlen(Integer maxlen) {
        this.maxlen = maxlen;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Static builder methods.
     */
    public static class Builder {

        private Integer replicate;
        private Long delay;
        private Long retry;
        private Long ttl;
        private Integer maxlen;
        private Boolean async;

        /**
         * Utility constructor.
         */
        private Builder() {

        }

        public Builder replicate(int replicate) {
            this.replicate = replicate;
            return this;
        }

        public Builder delay(long delayMs) {
            this.delay = delayMs;
            return this;
        }

        public Builder delay(long duration, TimeUnit timeUnit) {
            this.delay = timeUnit.toSeconds(duration);
            return this;
        }

        public Builder retry(long retrySec) {
            this.retry = retrySec;
            return this;
        }

        public Builder retry(long duration, TimeUnit timeUnit) {
            this.retry = timeUnit.toSeconds(duration);
            return this;
        }

        public Builder ttl(long ttlSec) {
            this.ttl = ttlSec;
            return this;
        }

        public Builder ttl(long duration, TimeUnit timeUnit) {
            this.ttl = timeUnit.toSeconds(duration);
            return this;
        }

        public Builder maxlen(int maxlen) {
            this.maxlen = maxlen;
            return this;
        }

        public Builder async(boolean async) {
            this.async = async;
            return this;
        }

        public AddJobArgs build() {

            AddJobArgs addJobArgs = new AddJobArgs();
            addJobArgs.setAsync(async);
            addJobArgs.setDelay(delay);
            addJobArgs.setMaxlen(maxlen);
            addJobArgs.setReplicate(replicate);
            addJobArgs.setRetry(retry);
            addJobArgs.setTtl(ttl);
            return addJobArgs;

        }
    }

    <K, V> void build(CommandArgs<K, V> args) {

        // ADDJOB queue_name job <ms-timeout> [REPLICATE <count>] [DELAY <sec>] [RETRY <sec>] [TTL <sec>] [MAXLEN <count>]
        // [ASYNC]

        if (replicate != null) {
            args.add(REPLICATE.bytes).add(replicate);
        }

        if (delay != null) {
            args.add(CommandKeyword.DELAY.bytes).add(delay);
        }

        if (retry != null) {
            args.add(CommandKeyword.RETRY.bytes).add(retry);
        }

        if (ttl != null) {
            args.add(CommandKeyword.TTL.bytes).add(ttl);
        }

        if (maxlen != null) {
            args.add(CommandKeyword.MAXLEN.bytes).add(maxlen);
        }

        if (async != null && async.booleanValue()) {
            args.add(CommandKeyword.ASYNC.bytes);
        }

    }

}
