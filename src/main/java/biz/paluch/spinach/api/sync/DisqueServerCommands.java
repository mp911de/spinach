package biz.paluch.spinach.api.sync;

import java.util.List;

import biz.paluch.spinach.api.CommandType;

import com.lambdaworks.redis.KillArgs;

/**
 * Synchronous executed commands related with Disque Server Control.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueServerCommands<K, V> {

    /**
     * Asynchronously rewrite the append-only file.
     *
     * @return String simple-string-reply always {@code OK}.
     */
    String bgrewriteaof();

    /**
     * Get the current connection name.
     * 
     * @return K bulk-string-reply The connection name, or a null bulk reply if no name is set.
     */
    K clientGetname();

    /**
     * Set the current connection name.
     * 
     * @param name the client name
     * @return simple-string-reply {@code OK} if the connection name was successfully set.
     */
    String clientSetname(K name);

    /**
     * Kill the connection of a client identified by ip:port.
     * 
     * @param addr ip:port
     * @return String simple-string-reply {@code OK} if the connection exists and has been closed
     */
    String clientKill(String addr);

    /**
     * Kill connections of clients which are filtered by {@code killArgs}
     *
     * @param killArgs args for the kill operation
     * @return Long integer-reply number of killed connections
     */
    Long clientKill(KillArgs killArgs);

    /**
     * Stop processing commands from clients for some time.
     * 
     * @param timeout the timeout value in milliseconds
     * @return String simple-string-reply The command returns OK or an error if the timeout is invalid.
     */
    String clientPause(long timeout);

    /**
     * Get the list of client connections.
     * 
     * @return String bulk-string-reply a unique string, formatted as follows: One client connection per line (separated by LF),
     *         each line is composed of a succession of property=value fields separated by a space character.
     */
    String clientList();

    /**
     * Returns an array reply of details about all Redis commands.
     * 
     * @return List&lt;Object&gt; array-reply
     */
    List<Object> command();

    /**
     * Returns an array reply of details about the requested commands.
     * 
     * @param commands the commands to query for
     * @return List&lt;Object&gt; array-reply
     */
    List<Object> commandInfo(String... commands);

    /**
     * Returns an array reply of details about the requested commands.
     * 
     * @param commands the commands to query for
     * @return List&lt;Object&gt; array-reply
     */
    List<Object> commandInfo(CommandType... commands);

    /**
     * Get total number of Redis commands.
     * 
     * @return Long integer-reply of number of total commands in this Redis server.
     */
    Long commandCount();

    /**
     * Get the value of a configuration parameter.
     * 
     * @param parameter name of the parameter
     * @return List&lt;String&gt; bulk-string-reply
     */
    List<String> configGet(String parameter);

    /**
     * Reset the stats returned by INFO.
     * 
     * @return String simple-string-reply always {@code OK}.
     */
    String configResetstat();

    /**
     * Rewrite the configuration file with the in memory configuration.
     * 
     * @return String simple-string-reply {@code OK} when the configuration was rewritten properly. Otherwise an error is
     *         returned.
     */
    String configRewrite();

    /**
     * Set a configuration parameter to the given value.
     * 
     * @param parameter the parameter name
     * @param value the parameter value
     * @return String simple-string-reply: {@code OK} when the configuration was set properly. Otherwise an error is returned.
     */
    String configSet(String parameter, String value);

    /**
     * Get information and statistics about the server.
     * 
     * @return String bulk-string-reply as a collection of text lines.
     */
    String info();

    /**
     * Get information and statistics about the server.
     * 
     * @param section the section type: string
     * @return String bulk-string-reply as a collection of text lines.
     */
    String info(String section);

    /**
     * Synchronously save the dataset to disk and then shut down the server.
     * 
     * @param save {@literal true} force save operation
     */
    void shutdown(boolean save);

    /**
     * Read the slow log.
     * 
     * @return List&lt;Object&gt; deeply nested multi bulk replies
     */
    List<Object> slowlogGet();

    /**
     * Read the slow log.
     * 
     * @param count the count
     * @return List&lt;Object&gt; deeply nested multi bulk replies
     */
    List<Object> slowlogGet(int count);

    /**
     * Obtaining the current length of the slow log.
     * 
     * @return Long length of the slow log.
     */
    Long slowlogLen();

    /**
     * Resetting the slow log.
     * 
     * @return String simple-string-reply The commands returns OK on success.
     */
    String slowlogReset();

    /**
     * Return the current server time.
     * 
     * @return List&lt;V&gt; array-reply specifically:
     * 
     *         A multi bulk reply containing two elements:
     * 
     *         unix time in seconds. microseconds.
     */
    List<V> time();

    /**
     * Remove all the server state: jobs, queues, and everything else to start like a fresh instance just rebooted.
     * 
     * @return always OK
     */
    String debugFlushall();

    List<Object> hello();

}