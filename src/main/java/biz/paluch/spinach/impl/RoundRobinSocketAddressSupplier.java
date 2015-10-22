package biz.paluch.spinach.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.SocketAddress;
import java.util.Collection;

import biz.paluch.spinach.DisqueURI;

import com.lambdaworks.redis.ConnectionPoint;

/**
 * Round-Robin socket address supplier. Connection points are iterated circular/infinitely.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RoundRobinSocketAddressSupplier implements SocketAddressSupplier {

    protected final Collection<? extends ConnectionPoint> connectionPoint;
    protected RoundRobin<? extends ConnectionPoint> roundRobin;

    /**
     * 
     * @param connectionPoints the collection of {@link ConnectionPoint connection points}, must not be {@literal null}.
     */
    public RoundRobinSocketAddressSupplier(Collection<? extends ConnectionPoint> connectionPoints) {
        this(connectionPoints, null);
    }

    /**
     * 
     * @param connectionPoints the collection of {@link ConnectionPoint connection points}, must not be {@literal null}.
     * @param offset {@link ConnectionPoint connection point} offset for starting the round robin cycle at that point, can be
     *        {@literal null}.
     */
    public RoundRobinSocketAddressSupplier(Collection<? extends ConnectionPoint> connectionPoints, ConnectionPoint offset) {
        checkArgument(connectionPoints != null, "ConnectionPoints must not be null");
        this.connectionPoint = connectionPoints;
        this.roundRobin = new RoundRobin<ConnectionPoint>(connectionPoints, offset);
    }

    @Override
    public SocketAddress get() {
        ConnectionPoint connectionPoint = roundRobin.next();
        return getSocketAddress(connectionPoint);
    }

    protected static SocketAddress getSocketAddress(ConnectionPoint connectionPoint) {
        if (connectionPoint instanceof DisqueURI.DisqueSocket) {
            return ((DisqueURI.DisqueSocket) connectionPoint).getResolvedAddress();
        }
        return ((DisqueURI.DisqueHostAndPort) connectionPoint).getResolvedAddress();
    }
}
