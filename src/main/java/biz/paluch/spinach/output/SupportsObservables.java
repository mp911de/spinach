package biz.paluch.spinach.output;

import rx.Subscriber;

/**
 * Facet interface that allows streaming support for {@link rx.Observable} result types. Implementors emit elements during
 * command parsing.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface SupportsObservables {

    /**
     * Provide a subscriber for a certain output so results can be streamed to the subscriber in the moment of reception instead
     * of waiting for the command to finish.
     * 
     * @param subscriber the subscriber
     * @param <T> Result type
     */
    <T> void setSubscriber(Subscriber<T> subscriber);
}
