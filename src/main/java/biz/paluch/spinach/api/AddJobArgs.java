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

import static com.lambdaworks.redis.protocol.CommandKeyword.*;

import java.util.concurrent.TimeUnit;

import com.lambdaworks.redis.protocol.CommandArgs;

/**
 * Arguments for adding a job.
 * 
 * @author Mark Paluch
 */
public class AddJobArgs {

    private Integer replicate;
    private Long delay;
    private Long retry;
    private Long ttl;
    private Integer maxlen;
    private Boolean async;

    /**
     * 
     * @return the number of nodes the job should be replicated to
     */
    public Integer getReplicate() {
        return replicate;
    }

    public void setReplicate(Integer replicate) {
        this.replicate = replicate;
    }

    /**
     * 
     * @return the number of seconds that should elapse before the job is queued by any server
     */
    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    /**
     *
     * @return the period after which, if no ACK is received, the job is put again into the queue for delivery
     */
    public Long getRetry() {
        return retry;
    }

    public void setRetry(Long retry) {
        this.retry = retry;
    }

    /**
     *
     * @return the the max job life time
     */
    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    /**
     *
     * @return the upper queue bound for adding the job
     */
    public Integer getMaxlen() {
        return maxlen;
    }

    public void setMaxlen(Integer maxlen) {
        this.maxlen = maxlen;
    }

    /**
     *
     * @return {@literal true} if the job should be replicated asynchronously
     */
    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    /**
     * Create a new builder for {@link AddJobArgs}.
     * @return a new builder for {@link AddJobArgs}
     */
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

        /**
         * Set the number of nodes the job should be replicated to.
         * 
         * @param replicate the number of nodes the job should be replicated to
         * @return the builder
         */
        public Builder replicate(int replicate) {
            this.replicate = replicate;
            return this;
        }

        /**
         * Set the number of seconds that should elapse before the job is queued by any server.
         * 
         * @param delaySec the number of seconds that should elapse before the job is queued by any server.
         * @return the builder
         */
        public Builder delay(long delaySec) {
            this.delay = delaySec;
            return this;
        }

        /**
         * Set the time that should elapse before the job is queued by any server.
         * 
         * @param duration delay duration
         * @param timeUnit delay unit
         * @return the builder
         */
        public Builder delay(long duration, TimeUnit timeUnit) {
            this.delay = timeUnit.toSeconds(duration);
            return this;
        }

        /**
         * Set the period after which, if no ACK is received, the job is put again into the queue for delivery. If RETRY is 0,
         * the job has at-most-once delivery semantics.
         * 
         * @param retrySec duration in seconds
         * @return the builder
         */
        public Builder retry(long retrySec) {
            this.retry = retrySec;
            return this;
        }

        /**
         * Set the period after which, if no ACK is received, the job is put again into the queue for delivery. If RETRY is 0,
         * the job has at-most-once delivery semantics.
         * 
         * @param duration duration
         * @param timeUnit time unit
         * @return the builder
         */
        public Builder retry(long duration, TimeUnit timeUnit) {
            this.retry = timeUnit.toSeconds(duration);
            return this;
        }

        /**
         * Set the the max job life time.
         * 
         * @param ttlSec timeout duration in seconds
         * @return the builder.
         */
        public Builder ttl(long ttlSec) {
            this.ttl = ttlSec;
            return this;
        }

        /**
         * Set the the max job life time.
         * 
         * @param duration timeout duration
         * @param timeUnit timeout unit
         * @return the builder
         */
        public Builder ttl(long duration, TimeUnit timeUnit) {
            this.ttl = timeUnit.toSeconds(duration);
            return this;
        }

        /**
         * Set the upper queue bound for adding the job. If there are already {@code maxlen} messages in the queue, the command
         * is refused with an error.
         * 
         * @param maxlen the upper queue bound for adding the job
         * @return the builder
         */
        public Builder maxlen(int maxlen) {
            this.maxlen = maxlen;
            return this;
        }

        /**
         * Set whether the job should be replicated asynchronously.
         * 
         * @param async {@literal true} if the job should be replicated asynchronously
         * @return the builder
         */
        public Builder async(boolean async) {
            this.async = async;
            return this;
        }

        /**
         * Build the {@link AddJobArgs}.
         * 
         * @return a new instance of {@link AddJobArgs}
         */
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

    /**
     * Build argument sequence and populate {@code args}.
     * 
     * @param args the target command args, must not be {@literal null}
     */
    public <K, V> void build(CommandArgs<K, V> args) {

        // ADDJOB queue_name job <ms-timeout> [REPLICATE <count>] [DELAY <sec>] [RETRY <sec>] [TTL <sec>] [MAXLEN <count>]
        // [ASYNC]

        if (replicate != null) {
            args.add(REPLICATE).add(replicate);
        }

        if (delay != null) {
            args.add(CommandKeyword.DELAY).add(delay);
        }

        if (retry != null) {
            args.add(CommandKeyword.RETRY).add(retry);
        }

        if (ttl != null) {
            args.add(CommandKeyword.TTL).add(ttl);
        }

        if (maxlen != null) {
            args.add(CommandKeyword.MAXLEN).add(maxlen);
        }

        if (async != null && async.booleanValue()) {
            args.add(CommandKeyword.ASYNC);
        }
    }
}
