package com.swag.swagmenus.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Serves static files embedded in the plugin JAR under {@code /webeditor/}.
 *
 * <p>Routing:
 * <ul>
 *   <li>{@code GET /} → {@code /webeditor/index.html}</li>
 *   <li>{@code GET /editor} → {@code /webeditor/index.html}</li>
 *   <li>{@code GET /assets/<file>} → {@code /webeditor/assets/<file>}</li>
 * </ul>
 */
public class StaticFileHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private static final Map<String, String> MIME_TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "css",  "text/css; charset=utf-8",
            "js",   "application/javascript; charset=utf-8",
            "json", "application/json; charset=utf-8",
            "png",  "image/png",
            "ico",  "image/x-icon",
            "svg",  "image/svg+xml"
    );

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            WebEditorServer.sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();

        // Route / and /editor to index.html
        if (path.equals("/") || path.equals("/editor") || path.equals("/editor/")) {
            serveResource(exchange, "/webeditor/index.html");
            return;
        }

        // Route /assets/* to webeditor/assets/*
        if (path.startsWith("/assets/")) {
            serveResource(exchange, "/webeditor" + path);
            return;
        }

        WebEditorServer.sendResponse(exchange, 404, "text/plain", "Not Found");
    }

    private void serveResource(HttpExchange exchange, String resourcePath) throws IOException {
        InputStream in = getClass().getResourceAsStream(resourcePath);
        if (in == null) {
            WebEditorServer.sendResponse(exchange, 404, "text/plain", "Resource not found: " + resourcePath);
            return;
        }

        String ext = "";
        int dot = resourcePath.lastIndexOf('.');
        if (dot != -1) {
            ext = resourcePath.substring(dot + 1).toLowerCase();
        }
        String mime = MIME_TYPES.getOrDefault(ext, "application/octet-stream");

        byte[] bytes;
        try {
            bytes = in.readAllBytes();
        } finally {
            in.close();
        }

        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
