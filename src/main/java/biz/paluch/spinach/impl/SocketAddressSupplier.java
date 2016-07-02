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
package biz.paluch.spinach.impl;

import java.net.SocketAddress;
import java.util.function.Supplier;

/**
 * Supplier API for {@link SocketAddress}. A {@code SocketAddressSupplier} is typically used to provide a {@link SocketAddress}
 * for connecting to Disque. The client requests a socket address from the supplier to establish initially a connection or to
 * reconnect. The supplier is required to supply an infinite number of elements. The sequence and ordering of elements are a
 * detail of the particular implementation.
 * <p>
 * {@link SocketAddressSupplier} instances should not be shared between connections although this is possible.
 * </p>
 *
 * @author Mark Paluch
 * @since 0.3
 */
public interface SocketAddressSupplier extends Supplier<SocketAddress> {

}
