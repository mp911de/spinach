package biz.paluch.spinach.api;

import com.lambdaworks.redis.protocol.CommandArgs;

/**
 * Arguments for adding a job.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class PauseArgs {

    private String option;
    private Boolean state;
    private Boolean bcast;

    /**
     *
     * @return the PAUSE option, IN, OUT, NONE or ALL
     */
    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    /**
     *
     * @return {@literal true} if the state should be queried
     */
    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    /**
     *
     * @return {@literal true} if the pause command should be broadcasted also to other cluster nodes
     */
    public Boolean getBcast() {
        return bcast;
    }

    public void setBcast(Boolean bcast) {
        this.bcast = bcast;
    }

    /**
     * Create a new builder for {@link PauseArgs}.
     * 
     * @return a new builder for {@link PauseArgs}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Static builder methods.
     */
    public static class Builder {

        private String option;
        private Boolean state;
        private Boolean bcast;

        /**
         * Utility constructor.
         */
        private Builder() {

        }

        /**
         * Set the queue as paused for incoming messages using the {@code IN} option.
         * 
         * @return the builder
         */
        public Builder in() {
            this.option = CommandKeyword.IN.name();
            return this;
        }

        /**
         * Set the queue as paused for outgoing messages using the {@code OUT} option.
         * 
         * @return the builder
         */
        public Builder out() {
            this.option = CommandKeyword.OUT.name();
            return this;
        }

        /**
         * Clear the pause if any, both {@code IN} and {@code OUT}.
         * 
         * @return the builder
         */
        public Builder none() {
            this.option = CommandKeyword.NONE.name();
            return this;
        }

        /**
         * Same as {@link #in()} and {@link #out()}.
         *
         * @return the builder
         */
        public Builder all() {
            this.option = CommandKeyword.ALL.name();
            return this;
        }

        /**
         * Query the current paused state and reports one of the strings "in", "out", "all", "none".
         *
         * @return the builder
         */
        public Builder state() {
            this.state = true;
            return this;
        }

        /**
         * Broadcast the PAUSE command to other nodes.
         *
         * @return the builder
         */
        public Builder bcast() {
            this.bcast = true;
            return this;
        }

        /**
         * Build the {@link PauseArgs}.
         * 
         * @return a new instance of {@link PauseArgs}
         */
        public PauseArgs build() {
            PauseArgs pauseArgs = new PauseArgs();
            pauseArgs.setBcast(bcast);
            pauseArgs.setOption(option);
            pauseArgs.setState(state);
            return pauseArgs;

        }
    }

    /**
     * Build argument sequence and populate {@code args}.
     * 
     * @param args the target command args, must not be {@literal null}
     */
    public <K, V> void build(CommandArgs<K, V> args) {

        // PAUSE <queue-name> [option option ... option]

        if (bcast != null) {
            args.add(CommandKeyword.BCAST.getBytes());
        }

        if (state != null) {
            args.add(CommandKeyword.STATE.getBytes());
        }

        if (option != null) {
            args.add(option);
        }
    }
}
