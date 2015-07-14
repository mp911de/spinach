package biz.paluch.spinach.output;

import rx.Subscriber;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 13.07.15 14:04
 */
public interface SupportsObservables {

    /**
     * Provide a subscriber for a certain output so results can be streamed to the subscriber in the moment of receiption
     * instead of waiting for the command to finish.
     * 
     * @param subscriber the subscriber.
     * @param <T>
     */
    <T> void setSubscriber(Subscriber<T> subscriber);
}
