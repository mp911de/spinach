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
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 0.3
 */
public interface SocketAddressSupplier extends Supplier<SocketAddress> {

}
