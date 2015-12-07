package biz.paluch.spinach.api.rx;

import rx.Observable;

/**
 * Reactive commands related with Disque Cluster.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueClusterReactiveCommands<K, V> {

    /**
     * Blacklist and remove the cluster node from the cluster.
     *
     * @param nodeId the node Id
     * @return String simple-string-reply
     */
    Observable<String> clusterForget(String nodeId);

    /**
     * Get information and statistics about the cluster viewed by the current node.
     *
     * @return String bulk-string-reply as a collection of text lines.
     */
    Observable<String> clusterInfo();

    /**
     * Retrieve cluster leaving state.
     *
     * @return String simple-string-reply
     */
    Observable<String> clusterLeaving();

    /**
     * Enable/disable cluster leaving state for a graceful cluster leave.
     *
     * @param state {@literal true} to set the leaving state, {@literal false} to un-set the leaving state
     * @return String simple-string-reply
     */
    Observable<String> clusterLeaving(boolean state);

    /**
     * Meet another cluster node to include the node into the cluster. The command starts the cluster handshake and returns with
     * {@literal OK} when the node was added to the cluster.
     *
     * @param ip IP address of the host
     * @param port port number.
     * @return String simple-string-reply
     */
    Observable<String> clusterMeet(String ip, int port);

    /**
     * Obtain the nodeId for the currently connected node.
     *
     * @return String simple-string-reply
     */
    Observable<String> clusterMyId();

    /**
     * Obtain details about all cluster nodes. Can be parsed using {@link biz.paluch.spinach.cluster.ClusterNodesParser#parse}
     *
     * @return String bulk-string-reply as a collection of text lines
     */
    Observable<String> clusterNodes();

    /**
     * Reset a node performing a soft or hard reset:
     * <ul>
     * <li>All other nodes are forgotten</li>
     * <li>All the assigned / open slots are released</li>
     * <li>If the node is a slave, it turns into a master</li>
     * <li>Only for hard reset: a new Node ID is generated</li>
     * <li>Only for hard reset: currentEpoch and configEpoch are set to 0</li>
     * <li>The new configuration is saved and the cluster state updated</li>
     * <li>If the node was a slave, the whole data set is flushed away</li>
     * </ul>
     *
     * @param hard {@literal true} for hard reset. Generates a new nodeId and currentEpoch/configEpoch are set to 0
     * @return String simple-string-reply
     */
    Observable<String> clusterReset(boolean hard);

    /**
     * Save the cluster config.
     *
     * @return String simple-string-reply
     */
    Observable<String> clusterSaveconfig();

}
