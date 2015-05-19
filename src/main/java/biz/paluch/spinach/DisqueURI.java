package biz.paluch.spinach;

import static com.google.common.base.Preconditions.*;
import static com.lambdaworks.redis.LettuceStrings.*;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.net.HostAndPort;
import com.lambdaworks.redis.ConnectionPoint;
import io.netty.channel.unix.DomainSocketAddress;

/**
 * Disque URI. Contains connection details for the Disque connections. You can provide as well the database, password and
 * timeouts within the DisqueURI. Either build your self the object
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 3.0
 */
@SuppressWarnings("serial")
public class DisqueURI implements Serializable {

    public static final String URI_SCHEME_DISQUE = "disque";
    public static final String URI_SCHEME_DISQUE_SOCKET = "disque-socket";
    public static final String URI_SCHEME_DISQUE_SECURE = "disquess";

    /**
     * The default disque port.
     */
    public static final int DEFAULT_DISQUE_PORT = 7711;

    private int database;
    private char[] password;
    private boolean ssl = false;
    private boolean verifyPeer = true;
    private boolean startTls = false;
    private long timeout = 60;
    private TimeUnit unit = TimeUnit.SECONDS;
    private final List<ConnectionPoint> connectionPoints = new ArrayList<ConnectionPoint>();

    /**
     * Default empty constructor.
     */
    public DisqueURI() {
    }

    /**
     * Constructor with host/port and timeout.
     * 
     * @param host the host
     * @param port the port
     * @param timeout timeout value
     * @param unit unit of the timeout value
     */
    public DisqueURI(String host, int port, long timeout, TimeUnit unit) {

        DisqueHostAndPort hap = new DisqueHostAndPort();
        hap.setHost(host);
        hap.setPort(port);
        connectionPoints.add(hap);

        this.timeout = timeout;
        this.unit = unit;
    }

    /**
     * Create a Disque URI from an URI string. Supported formats are:
     * <ul>
     * <li>disque://[password@]host[:port][/databaseNumber]</li>
     * </ul>
     *
     * The uri must follow conventions of {@link java.net.URI}
     * 
     * @param uri The URI string.
     * @return An instance of {@link DisqueURI} containing details from the URI.
     */
    public static DisqueURI create(String uri) {
        return create(URI.create(uri));
    }

    /**
     * Create a Disque URI from an URI string. Supported formats are:
     * <ul>
     * <li>disque://[password@]host[:port][/databaseNumber]</li>
     * </ul>
     *
     * The uri must follow conventions of {@link java.net.URI}
     *
     * @param uri The URI.
     * @return An instance of {@link DisqueURI} containing details from the URI.
     */
    public static DisqueURI create(URI uri) {

        DisqueURI.Builder builder;
        builder = configureDisque(uri);

        if (URI_SCHEME_DISQUE_SECURE.equals(uri.getScheme())) {
            builder.withSsl(true);
        }

        String userInfo = uri.getUserInfo();

        if (isEmpty(userInfo) && isNotEmpty(uri.getAuthority()) && uri.getAuthority().indexOf('@') > 0) {
            userInfo = uri.getAuthority().substring(0, uri.getAuthority().indexOf('@'));
        }

        if (isNotEmpty(userInfo)) {
            String password = userInfo;
            if (password.startsWith(":")) {
                password = password.substring(1);
            }
            builder.withPassword(password);
        }

        if (!URI_SCHEME_DISQUE_SOCKET.equals(uri.getScheme())) {
            if (isNotEmpty(uri.getPath())) {
                String pathSuffix = uri.getPath().substring(1);

                if (isNotEmpty(pathSuffix)) {

                    builder.withDatabase(Integer.parseInt(pathSuffix));
                }
            }
        }

        return builder.build();

    }

    private static DisqueURI.Builder configureDisque(URI uri) {
        DisqueURI.Builder builder = null;

        if (URI_SCHEME_DISQUE_SOCKET.equals(uri.getScheme())) {
            builder = Builder.disqueSocket(uri.getPath());
        } else

        if (isNotEmpty(uri.getHost())) {
            if (uri.getPort() != -1) {
                builder = DisqueURI.Builder.disque(uri.getHost(), uri.getPort());
            } else {
                builder = DisqueURI.Builder.disque(uri.getHost());
            }
        }

        if (builder == null && isNotEmpty(uri.getAuthority())) {
            String authority = uri.getAuthority();
            if (authority.indexOf('@') > -1) {
                authority = authority.substring(authority.indexOf('@') + 1);
            }

            String[] hosts = authority.split("\\,");
            for (String host : hosts) {
                if (uri.getScheme().equals(URI_SCHEME_DISQUE_SOCKET)) {
                    if (builder == null) {
                        builder = DisqueURI.Builder.disqueSocket(host);
                    } else {
                        builder.withSocket(host);
                    }
                } else {
                    HostAndPort hostAndPort = HostAndPort.fromString(host);
                    if (builder == null) {
                        if (hostAndPort.hasPort()) {
                            builder = DisqueURI.Builder.disque(hostAndPort.getHostText(), hostAndPort.getPort());
                        } else {
                            builder = DisqueURI.Builder.disque(hostAndPort.getHostText());
                        }
                    } else {
                        if (hostAndPort.hasPort()) {
                            builder.withDisque(hostAndPort.getHostText(), hostAndPort.getPort());
                        } else {
                            builder.withDisque(hostAndPort.getHostText());
                        }
                    }
                }
            }

        }

        checkArgument(builder != null, "Invalid URI, cannot get host part");
        return builder;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password.toCharArray();
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isVerifyPeer() {
        return verifyPeer;
    }

    public void setVerifyPeer(boolean verifyPeer) {
        this.verifyPeer = verifyPeer;
    }

    public boolean isStartTls() {
        return startTls;
    }

    public void setStartTls(boolean startTls) {
        this.startTls = startTls;
    }

    public List<ConnectionPoint> getConnectionPoints() {
        return connectionPoints;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [").append(connectionPoints);
        sb.append(']');
        return sb.toString();
    }

    /**
     * Builder for Disque URI.
     */
    public static class Builder {

        private final DisqueURI disqueURI = new DisqueURI();

        /**
         * Set Disque host. Creates a new builder.
         * 
         * @param host the host name
         * @return New builder with Disque host/port.
         */
        public static Builder disque(String host) {
            return disque(host, DEFAULT_DISQUE_PORT);
        }

        /**
         * Set Disque host and port. Creates a new builder
         * 
         * @param host the host name
         * @param port the port
         * @return New builder with Disque host/port.
         */
        public static Builder disque(String host, int port) {
            checkNotNull(host, "Host must not be null");
            Builder builder = new Builder();

            builder.withDisque(host, port);

            return builder;
        }

        /**
         * Set Disque socket. Creates a new builder .
         * 
         * @param socket the socket name
         * @return New builder with Disque socket.
         */
        public static Builder disqueSocket(String socket) {
            checkNotNull(socket, "Socket must not be null");
            Builder builder = new Builder();

            builder.withSocket(socket);

            return builder;
        }

        public Builder withSocket(String socket) {
            checkNotNull(socket, "Socket must not be null");

            this.disqueURI.connectionPoints.add(new DisqueSocket(socket));
            return this;
        }

        public Builder withDisque(String host) {
            checkNotNull(host, "Host must not be null");

            return withDisque(host, DEFAULT_DISQUE_PORT);
        }

        public Builder withDisque(String host, int port) {
            checkNotNull(host, "Host must not be null");

            DisqueHostAndPort hap = new DisqueHostAndPort(host, port);
            this.disqueURI.connectionPoints.add(hap);
            return this;
        }

        /**
         * Adds ssl information to the builder.
         *
         * @param ssl {@literal true} if use SSL
         * @return the builder
         */
        public Builder withSsl(boolean ssl) {
            disqueURI.setSsl(ssl);
            return this;
        }

        /**
         * Enables/disables StartTLS when using SSL.
         *
         * @param startTls {@literal true} if use StartTLS
         * @return the builder
         */
        public Builder withStartTls(boolean startTls) {
            disqueURI.setStartTls(startTls);
            return this;
        }

        /**
         * Enables/disables peer verification.
         *
         * @param verifyPeer {@literal true} to verify hosts when using SSL
         * @return the builder
         */
        public Builder withVerifyPeer(boolean verifyPeer) {
            disqueURI.setVerifyPeer(verifyPeer);
            return this;
        }

        /**
         * Adds database selection.
         * 
         * @param database the database number
         * @return the builder
         */
        public Builder withDatabase(int database) {
            disqueURI.setDatabase(database);
            return this;
        }

        /**
         * Adds authentication.
         * 
         * @param password the password
         * @return the builder
         */
        public Builder withPassword(String password) {
            checkNotNull(password, "Password must not be null");
            disqueURI.setPassword(password);
            return this;
        }

        /**
         * Adds timeout.
         * 
         * @param timeout must be greater or equal 0"
         * @param unit the timeout time unit.
         * @return the builder
         */
        public Builder withTimeout(long timeout, TimeUnit unit) {
            checkNotNull(unit, "TimeUnit must not be null");
            checkArgument(timeout >= 0, "Timeout must be greater or equal 0");
            disqueURI.setTimeout(timeout);
            disqueURI.setUnit(unit);
            return this;
        }

        /**
         * 
         * @return the DisqueURI.
         */
        public DisqueURI build() {
            return disqueURI;
        }

    }

    public static class DisqueSocket implements Serializable, ConnectionPoint {
        private String socket;
        private transient SocketAddress resolvedAddress;

        public DisqueSocket() {
        }

        public DisqueSocket(String socket) {
            this.socket = socket;
        }

        @Override
        public String getHost() {
            return null;
        }

        @Override
        public int getPort() {
            return -1;
        }

        @Override
        public String getSocket() {
            return socket;
        }

        public void setSocket(String socket) {
            this.socket = socket;
        }

        public SocketAddress getResolvedAddress() {
            if (resolvedAddress == null) {
                resolvedAddress = new DomainSocketAddress(getSocket());
            }
            return resolvedAddress;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer();
            sb.append("[socket=").append(socket);
            sb.append(']');
            return sb.toString();
        }

    }

    public static class DisqueHostAndPort implements Serializable, ConnectionPoint {

        private String host;
        private int port;
        private transient SocketAddress resolvedAddress;

        public DisqueHostAndPort() {
        }

        public DisqueHostAndPort(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String getSocket() {
            return null;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public SocketAddress getResolvedAddress() {
            if (resolvedAddress == null) {
                resolvedAddress = new InetSocketAddress(host, port);
            }
            return resolvedAddress;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer();
            sb.append("[port=").append(port);
            sb.append(", host='").append(host).append('\'');
            sb.append(']');
            return sb.toString();
        }
    }

}