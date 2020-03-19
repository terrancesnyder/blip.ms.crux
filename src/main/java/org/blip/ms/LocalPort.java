package org.blip.ms;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Provides a method and manner in which to discover and use a random port. Used for ZMQ port bindings
 * as well as test scripts to find an open HTTP or other port when running localized tests.
 *
 * @author Terrance A. Snyder
 *
 */
public class LocalPort {
    private final int port;
    private static final Object lock = new Object();

    private LocalPort(final int port) {
        this.port = port;
    }

    /**
     * Get the port associated to this random port.
     * @return The port number ready and able to be used.
     */
    public int getPort() {
        return port;
    }

    /**
     * Find a random port.
     * @return Returns a random port that can be used to bind against.
     */
    public static LocalPort random() {
        try {
            int port = findRandomOpenPortOnAllLocalInterfaces();
            return new LocalPort(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("squid:S4818")
    private static int findRandomOpenPortOnAllLocalInterfaces() throws IOException {
        synchronized (lock) {
            try (ServerSocket socket = new ServerSocket(0);) {
                int port = socket.getLocalPort();
                IOUtils.closeQuietly(socket);
                return port;
            }
        }
    }
}
