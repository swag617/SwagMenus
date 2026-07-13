package com.swag.swagmenus.web;

import com.SwagDev.SwagAPI.api.IWebService;
import com.swag.swagmenus.SwagMenus;
import com.sun.net.httpserver.HttpExchange;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Registers the SwagMenus web editor with SwagAPI's shared {@link IWebService}.
 *
 * SwagMenus does not run its own {@code HttpServer} — SwagAPI's shared server does.
 * The module is mounted at {@code /swagapi/swagmenus/} and every request is already
 * authenticated by SwagAPI's own session-cookie login system before
 * {@link WebEditorHttpHandler} ever runs, so the web editor implements no password/token
 * auth of its own.
 *
 * This is a soft dependency: if SwagAPI's {@link IWebService} isn't registered (SwagAPI
 * missing/disabled), {@link #register()} logs a warning and skips — it does not prevent
 * SwagMenus from enabling.
 */
public class WebEditorServer {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private final SwagMenus plugin;
    private boolean registered = false;

    public WebEditorServer(SwagMenus plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers the web editor module with SwagAPI's {@link IWebService}, if available.
     */
    public void register() {
        if (!plugin.getConfig().getBoolean("web_editor.enabled", true)) {
            LOG.info("Web editor is disabled in config.yml.");
            return;
        }

        IWebService webService = resolveWebService();
        if (webService == null) {
            LOG.warning("SwagAPI IWebService not found — web editor will not be available. "
                    + "Install/enable SwagAPI to use the web editor.");
            return;
        }

        webService.registerModule(plugin, new WebEditorHttpHandler(plugin));
        registered = true;
        LOG.info("Web editor registered at " + webService.getPluginUrl(plugin.getName().toLowerCase()));
    }

    /**
     * Unregisters the web editor module from SwagAPI's {@link IWebService}, if it was registered.
     */
    public void unregister() {
        if (!registered) return;
        IWebService webService = resolveWebService();
        if (webService != null) {
            webService.unregisterModule(plugin);
        }
        registered = false;
        LOG.info("Web editor unregistered.");
    }

    /**
     * @return true if the web editor module is currently registered with SwagAPI
     */
    public boolean isRegistered() {
        return registered;
    }

    /**
     * @return the full browser URL to the web editor, or null if not registered
     *         or SwagAPI's web service is unavailable
     */
    public String getUrl() {
        if (!registered) return null;
        IWebService webService = resolveWebService();
        if (webService == null) return null;
        return webService.getPluginUrl(plugin.getName().toLowerCase());
    }

    private IWebService resolveWebService() {
        RegisteredServiceProvider<IWebService> rsp =
                plugin.getServer().getServicesManager().getRegistration(IWebService.class);
        return rsp != null ? rsp.getProvider() : null;
    }

    static void sendResponse(HttpExchange exchange, int status, String contentType, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
