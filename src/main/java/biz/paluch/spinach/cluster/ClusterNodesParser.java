package biz.paluch.spinach.cluster;

import java.util.*;
import java.util.regex.Pattern;

import com.lambdaworks.redis.RedisException;
import com.lambdaworks.redis.internal.HostAndPort;
import com.lambdaworks.redis.internal.LettuceLists;

import biz.paluch.spinach.DisqueURI;

/**
 * Parser for node information output of {@code CLUSTER NODES}.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ClusterNodesParser {
    public static final String CONNECTED = "connected";

    private static final char TOKEN_NODE_SEPARATOR = '\n';
    private static final Pattern TOKEN_PATTERN = Pattern.compile(Character.toString(TOKEN_NODE_SEPARATOR));
    private static final Pattern SPACE_PATTERN = Pattern.compile(" ");
    private static final Pattern COMMA_PATTERN = Pattern.compile("\\,");

    private static final Map<String, DisqueNode.NodeFlag> FLAG_MAPPING;

    static {
        Map<String, DisqueNode.NodeFlag> map = new HashMap<>();

        map.put("noflags", DisqueNode.NodeFlag.NOFLAGS);
        map.put("myself", DisqueNode.NodeFlag.MYSELF);
        map.put("fail?", DisqueNode.NodeFlag.EVENTUAL_FAIL);
        map.put("fail", DisqueNode.NodeFlag.FAIL);
        map.put("handshake", DisqueNode.NodeFlag.HANDSHAKE);
        map.put("noaddr", DisqueNode.NodeFlag.NOADDR);
        FLAG_MAPPING = Collections.unmodifiableMap(map);
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
        List<DisqueNode> result = new ArrayList<>();

        Iterator<String> iterator = TOKEN_PATTERN.splitAsStream(nodes).iterator();

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

        Iterator<String> iterator = SPACE_PATTERN.splitAsStream(nodeInformation).iterator();

        String nodeId = iterator.next();
        boolean connected = false;

        HostAndPort hostAndPort = HostAndPort.parse(iterator.next());

        String flags = iterator.next();
        List<String> flagStrings = LettuceLists.newList(COMMA_PATTERN.splitAsStream(flags).iterator());

        Set<DisqueNode.NodeFlag> nodeFlags = readFlags(flagStrings);

        long pingSentTs = getLongFromIterator(iterator, 0);
        long pongReceivedTs = getLongFromIterator(iterator, 0);

        String connectedFlags = iterator.next(); // "connected" : "disconnected"

        if (CONNECTED.equals(connectedFlags)) {
            connected = true;
        }

        DisqueNode partition = new DisqueNode(hostAndPort.getHostText(),
                hostAndPort.hasPort() ? hostAndPort.getPort() : DisqueURI.DEFAULT_DISQUE_PORT, nodeId, connected, pingSentTs,
                pongReceivedTs, nodeFlags);

        return partition;

    }

    private static Set<DisqueNode.NodeFlag> readFlags(List<String> flagStrings) {

        Set<DisqueNode.NodeFlag> flags = new HashSet<>();
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
