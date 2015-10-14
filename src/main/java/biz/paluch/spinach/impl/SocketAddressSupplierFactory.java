package biz.paluch.spinach.impl;

import biz.paluch.spinach.DisqueURI;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.copyOf;

/**
 * Factory for a {@link SocketAddressSupplier}. This factory creates new instances of {@link SocketAddressSupplier} based on a
 * {@link DisqueURI} for every logical connection.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface SocketAddressSupplierFactory {

    /**
     * Creates a new {@link SocketAddressSupplier} from a {@link SocketAddressSupplier}.
     * 
     * @param disqueURI the connection URI object, must not be {@literal null}
     * @return an new instance of {@link SocketAddressSupplier}
     */
    SocketAddressSupplier newSupplier(DisqueURI disqueURI);

    enum Factories implements SocketAddressSupplierFactory {
        ROUND_ROBIN {
            @Override
            public SocketAddressSupplier newSupplier(DisqueURI disqueURI) {
                checkNotNull(disqueURI != null, "DisqueURI must not be null");
                return new RoundRobinSocketAddressSupplier(copyOf(disqueURI.getConnectionPoints()));
            }
        };

        @Override
        public SocketAddressSupplier newSupplier(DisqueURI disqueURI) {
            return null;
        }
    }
}
