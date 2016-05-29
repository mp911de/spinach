package biz.paluch.spinach.cluster;

import java.io.Serializable;
import java.util.Set;

import com.lambdaworks.redis.internal.LettuceAssert;

/**
 * Representation of a redis cluster node.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 0.2
 */
@SuppressWarnings("serial")
public class DisqueNode implements Serializable {
    private String addr;
    private int port;
    private String nodeId;

    private boolean connected;
    private long pingSentTimestamp;
    private long pongReceivedTimestamp;

    private Set<NodeFlag> flags;

    public DisqueNode() {

    }

    public DisqueNode(String addr, int port, String nodeId, boolean connected, long pingSentTimestamp,
            long pongReceivedTimestamp, Set<NodeFlag> flags) {
        this.addr = addr;
        this.port = port;
        this.nodeId = nodeId;
        this.connected = connected;
        this.pingSentTimestamp = pingSentTimestamp;
        this.pongReceivedTimestamp = pongReceivedTimestamp;
        this.flags = flags;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        LettuceAssert.notNull(nodeId, "nodeId must not be null");
        this.nodeId = nodeId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public long getPingSentTimestamp() {
        return pingSentTimestamp;
    }

    public void setPingSentTimestamp(long pingSentTimestamp) {
        this.pingSentTimestamp = pingSentTimestamp;
    }

    public long getPongReceivedTimestamp() {
        return pongReceivedTimestamp;
    }

    public void setPongReceivedTimestamp(long pongReceivedTimestamp) {
        this.pongReceivedTimestamp = pongReceivedTimestamp;
    }

    public Set<NodeFlag> getFlags() {
        return flags;
    }

    public void setFlags(Set<NodeFlag> flags) {
        this.flags = flags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DisqueNode)) {
            return false;
        }

        DisqueNode that = (DisqueNode) o;

        if (nodeId != null ? !nodeId.equals(that.nodeId) : that.nodeId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * (nodeId != null ? nodeId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [addr=").append(addr);
        sb.append(", port='").append(port).append('\'');
        sb.append(", nodeId='").append(nodeId).append('\'');
        sb.append(", connected=").append(connected);
        sb.append(", pingSentTimestamp=").append(pingSentTimestamp);
        sb.append(", pongReceivedTimestamp=").append(pongReceivedTimestamp);
        sb.append(", flags=").append(flags);
        sb.append(']');
        return sb.toString();
    }

    public enum NodeFlag {
        NOFLAGS, MYSELF, EVENTUAL_FAIL, FAIL, HANDSHAKE, NOADDR;
    }
}
