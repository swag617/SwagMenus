package com.swag.swagmenus.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.swag.swagmenus.SwagMenus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Manages the lifecycle of the embedded HTTP server for the web editor.
 *
 * <p>The server uses {@link HttpServer} from {@code com.sun.net.httpserver} — no extra
 * dependencies. It binds to {@code 0.0.0.0} so it is reachable from the network.
 *
 * <p>Routing overview:
 * <ul>
 *   <li>{@code /}, {@code /editor} → {@link StaticFileHandler}</li>
 *   <li>{@code /assets/*}          → {@link StaticFileHandler}</li>
 *   <li>{@code /api/*}             → {@link ApiHandler}</li>
 * </ul>
 */
public class WebEditorServer {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private final SwagMenus plugin;
    private final AuthManager authManager;

    private HttpServer server;
    private int currentPort;

    public WebEditorServer(SwagMenus plugin) {
        this.plugin = plugin;
        long expiryMinutes = plugin.getConfig().getLong("web_editor.token_expiry_minutes", 30);
        this.authManager = new AuthManager(expiryMinutes);
        this.currentPort = plugin.getConfig().getInt("web_editor.port", 8080);
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Starts the HTTP server on the configured port.
     * Safe to call multiple times (will skip if already running).
     */
    public void start() {
        if (!plugin.getConfig().getBoolean("web_editor.enabled", true)) {
            LOG.info("Web editor is disabled in config.yml.");
            return;
        }

        if (server != null) {
            LOG.warning("WebEditorServer.start() called but server is already running.");
            return;
        }

        String bindAddress = plugin.getConfig().getString("web_editor.bind-address", "0.0.0.0");
        try {
            server = HttpServer.create(new InetSocketAddress(bindAddress, currentPort), 0);
        } catch (IOException e) {
            LOG.severe("Failed to start web editor on " + bindAddress + ":" + currentPort + ": " + e.getMessage());
            server = null;
            return;
        }

        StaticFileHandler staticHandler = new StaticFileHandler();
        ApiHandler apiHandler = new ApiHandler(plugin, authManager);

        // Context mapping — order matters; more specific paths first
        server.createContext("/api", apiHandler);
        server.createContext("/assets", staticHandler);
        server.createContext("/editor", staticHandler);
        server.createContext("/", staticHandler);

        // Use a virtual-thread executor — Java 21, lightweight, no thread pool config needed
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();

        LOG.info("Web editor started on port " + currentPort
                + " — visit http://localhost:" + currentPort + "/editor");
    }

    /**
     * Stops the HTTP server, waiting up to 2 seconds for in-flight requests to finish.
     */
    public void stop() {
        if (server == null) return;
        server.stop(2);
        server = null;
        LOG.info("Web editor stopped.");
    }

    /**
     * Restarts the server on a new port. Updates {@code config.yml} with the new port.
     *
     * @param newPort the port to restart on (1024–65535)
     * @throws IllegalArgumentException if the port is out of range
     */
    public void restart(int newPort) {
        if (newPort < 1024 || newPort > 65535) {
            throw new IllegalArgumentException("Port must be between 1024 and 65535.");
        }
        stop();
        currentPort = newPort;
        plugin.getConfig().set("web_editor.port", newPort);
        plugin.saveConfig();
        start();
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public int getPort() {
        return currentPort;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public boolean isRunning() {
        return server != null;
    }

    // -------------------------------------------------------------------------
    // Static response utility (used by StaticFileHandler and ApiHandler)
    // -------------------------------------------------------------------------

    /**
     * Sends a plain-text response. Used for error conditions where JSON is overkill.
     */
    static void sendResponse(HttpExchange exchange, int status, String contentType, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
