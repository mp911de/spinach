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
package biz.paluch.spinach.output;

import rx.Subscriber;

/**
 * Facet interface that allows streaming support for {@link rx.Observable} result types. Implementors emit elements during
 * command parsing.
 * 
 * @author Mark Paluch
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
