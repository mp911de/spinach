package biz.paluch.spinach.impl;

import java.net.SocketAddress;
import java.util.Collection;

import biz.paluch.spinach.DisqueURI;

import com.lambdaworks.redis.ConnectionPoint;

/**
 * Round-Robin socket address supplier. Connection points are iterated circular without an end.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RoundRobinSocketAddressSupplier implements SocketAddressSupplier {

    protected final Collection<? extends ConnectionPoint> connectionPoint;
    protected ConnectionPoint offset;

    public RoundRobinSocketAddressSupplier(Collection<? extends ConnectionPoint> connectionPoints) {
        this(connectionPoints, null);
    }

    public RoundRobinSocketAddressSupplier(Collection<? extends ConnectionPoint> connectionPoints, ConnectionPoint offset) {
        this.connectionPoint = connectionPoints;
        this.offset = offset;
    }

    @Override
    public SocketAddress get() {
        ConnectionPoint connectionPoint = getConnectionPoint();
        return getSocketAddress(connectionPoint);
    }

    private ConnectionPoint getConnectionPoint() {
        if (offset != null) {
            boolean accept = false;
            for (ConnectionPoint point : connectionPoint) {
                if (point == offset) {
                    accept = true;
                    continue;
                }

                if (accept) {
                    return offset = point;
                }
            }
        }

        return offset = connectionPoint.iterator().next();
    }

    protected static SocketAddress getSocketAddress(ConnectionPoint connectionPoint) {
        if (connectionPoint instanceof DisqueURI.DisqueSocket) {
            return ((DisqueURI.DisqueSocket) connectionPoint).getResolvedAddress();
        }
        return ((DisqueURI.DisqueHostAndPort) connectionPoint).getResolvedAddress();
    }
}
