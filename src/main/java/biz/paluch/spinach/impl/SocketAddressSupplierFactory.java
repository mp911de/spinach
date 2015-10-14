package biz.paluch.spinach.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.copyOf;

import biz.paluch.spinach.DisqueURI;

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
        /**
         * Round-Robin address supplier that runs circularly over the provided connection points within the {@link DisqueURI}.
         */
        ROUND_ROBIN {
            @Override
            public SocketAddressSupplier newSupplier(DisqueURI disqueURI) {
                checkNotNull(disqueURI != null, "DisqueURI must not be null");
                return new RoundRobinSocketAddressSupplier(copyOf(disqueURI.getConnectionPoints()));
            }
        },

        /**
         * Cluster-topology-aware address supplier that obtains its initial connection point from the {@link DisqueURI} and then
         * looks up the cluster topology using the {@code HELLO} command. Connections are established using the cluster node IP
         * addresses.
         */
        HELLO_CLUSTER {
            @Override
            public SocketAddressSupplier newSupplier(DisqueURI disqueURI) {
                checkNotNull(disqueURI != null, "DisqueURI must not be null");
                return new HelloClusterSocketAddressSupplier(ROUND_ROBIN.newSupplier(disqueURI));
            }
        };

        @Override
        public SocketAddressSupplier newSupplier(DisqueURI disqueURI) {
            return null;
        }
    }
}
