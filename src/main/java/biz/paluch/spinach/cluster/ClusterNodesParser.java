package biz.paluch.spinach.cluster;

import java.util.*;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;
import com.lambdaworks.redis.RedisException;

/**
 * Parser for node information output of {@code CLUSTER NODES}.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ClusterNodesParser {
    public static final String CONNECTED = "connected";

    private static final char TOKEN_NODE_SEPARATOR = '\n';
    private static final Map<String, DisqueNode.NodeFlag> FLAG_MAPPING;

    static {
        ImmutableMap.Builder<String, DisqueNode.NodeFlag> builder = ImmutableMap.builder();

        builder.put("noflags", DisqueNode.NodeFlag.NOFLAGS);
        builder.put("myself", DisqueNode.NodeFlag.MYSELF);
        builder.put("fail?", DisqueNode.NodeFlag.EVENTUAL_FAIL);
        builder.put("fail", DisqueNode.NodeFlag.FAIL);
        builder.put("handshake", DisqueNode.NodeFlag.HANDSHAKE);
        builder.put("noaddr", DisqueNode.NodeFlag.NOADDR);
        FLAG_MAPPING = builder.build();
    }

    /**
     * Utility constructor.
     */
    private ClusterNodesParser() {

    }

    /**
     * Parse partition lines into Partitions object.
     * 
     * @param nodes output of CLUSTER NODES
     * @return the partitions object.
     */
    public static Collection<DisqueNode> parse(String nodes) {
        List<DisqueNode> result = Lists.newArrayList();

        Iterator<String> iterator = Splitter.on(TOKEN_NODE_SEPARATOR).omitEmptyStrings().split(nodes).iterator();

        try {
            while (iterator.hasNext()) {
                String node = iterator.next();
                DisqueNode partition = parseNode(node);
                result.add(partition);
            }

        } catch (Exception e) {
            throw new RedisException("Cannot parse " + nodes, e);
        }

        return result;
    }

    private static DisqueNode parseNode(String nodeInformation) {

        Iterable<String> split = Splitter.on(' ').split(nodeInformation);
        Iterator<String> iterator = split.iterator();

        String nodeId = iterator.next();
        boolean connected = false;

        HostAndPort hostAndPort = HostAndPort.fromString(iterator.next());

        String flags = iterator.next();
        List<String> flagStrings = Lists.newArrayList(Splitter.on(',').trimResults().split(flags).iterator());

        Set<DisqueNode.NodeFlag> nodeFlags = readFlags(flagStrings);

        long pingSentTs = getLongFromIterator(iterator, 0);
        long pongReceivedTs = getLongFromIterator(iterator, 0);

        String connectedFlags = iterator.next(); // "connected" : "disconnected"

        if (CONNECTED.equals(connectedFlags)) {
            connected = true;
        }

        DisqueNode partition = new DisqueNode(hostAndPort.getHostText(), hostAndPort.getPortOrDefault(0), nodeId, connected,
                pingSentTs, pongReceivedTs, nodeFlags);

        return partition;

    }

    private static Set<DisqueNode.NodeFlag> readFlags(List<String> flagStrings) {

        Set<DisqueNode.NodeFlag> flags = Sets.newHashSet();
        for (String flagString : flagStrings) {
            if (FLAG_MAPPING.containsKey(flagString)) {
                flags.add(FLAG_MAPPING.get(flagString));
            }
        }
        return Collections.unmodifiableSet(flags);
    }

    private static long getLongFromIterator(Iterator<?> iterator, long defaultValue) {
        if (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof String) {
                return Long.parseLong((String) object);
            }
        }
        return defaultValue;
    }

}
