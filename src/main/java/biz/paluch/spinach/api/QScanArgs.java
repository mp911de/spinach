package biz.paluch.spinach.api;

import static com.lambdaworks.redis.protocol.CommandKeyword.*;

import com.lambdaworks.redis.protocol.CommandArgs;

/**
 * Arguments for scanning queues/jobs.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class QScanArgs {

    private Long count;
    private Integer minlen;
    private Integer maxlen;
    private Integer importrate;
    private boolean busyloop;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Integer getMinlen() {
        return minlen;
    }

    public void setMinlen(Integer minlen) {
        this.minlen = minlen;
    }

    public Integer getMaxlen() {
        return maxlen;
    }

    public void setMaxlen(Integer maxlen) {
        this.maxlen = maxlen;
    }

    public Integer getImportrate() {
        return importrate;
    }

    public void setImportrate(Integer importrate) {
        this.importrate = importrate;
    }

    public boolean isBusyloop() {
        return busyloop;
    }

    public void setBusyloop(boolean busyloop) {
        this.busyloop = busyloop;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Static builder methods.
     */
    public static class Builder {

        private Long count;
        private Integer minlen;
        private Integer maxlen;
        private Integer importrate;
        private boolean busyloop;

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
        public Builder count(long count) {
            this.count = count;
            return this;
        }

        public Builder minlen(int minlen) {
            this.minlen = minlen;
            return this;
        }

        public Builder maxlen(int maxlen) {
            this.maxlen = maxlen;
            return this;
        }

        public Builder importrate(int importrate) {
            this.importrate = importrate;
            return this;
        }

        /**
         * Enable blocking loop mode.
         *
         * @return the current builder
         */
        public Builder busyloop() {
            return busyloop(true);
        }

        /**
         * Enable/disable blocking loop mode
         *
         * @param busyloop {@literal true} or {@literal false}
         * @return the current builder
         */
        public Builder busyloop(boolean busyloop) {
            this.busyloop = busyloop;
            return this;
        }


        /**
         * Build an instance of {@link QScanArgs}
         *
         * @return a new instance of {@link QScanArgs}
         */
        public QScanArgs build() {

            QScanArgs result = new QScanArgs();
            result.setBusyloop(busyloop);
            result.setMinlen(minlen);
            result.setMaxlen(maxlen);
            result.setImportrate(importrate);
            result.setCount(count);

            return result;

        }
    }

    public <K, V> void build(CommandArgs<K, V> args) {

        // QSCAN [COUNT <count>] [BUSYLOOP] [MINLEN <len>] [MAXLEN <len>] [IMPORTRATE <rate>]

        if (count != null) {
            args.add(COUNT).add(count);
        }

        if (busyloop) {
            args.add(CommandKeyword.BUSYLOOP);
        }

        if (minlen != null) {
            args.add(CommandKeyword.MINLEN).add(minlen);
        }

        if (minlen != null) {
            args.add(CommandKeyword.MAXLEN).add(maxlen);
        }

        if (importrate != null) {
            args.add(CommandKeyword.IMPORTRATE).add(importrate);
        }
    }

}
