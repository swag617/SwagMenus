package com.swag.swagmenus.web;

import com.swag.swagmenus.SwagMenus;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * Single entry point registered with SwagAPI's {@code IWebService} (one handler per plugin —
 * see {@code IWebService#registerModule(Plugin, HttpHandler)}).
 *
 * Dispatches {@code /api/...} to {@link ApiHandler} and everything else to
 * {@link StaticFileHandler}. Authentication is handled entirely by SwagAPI's shared
 * session-cookie login before this handler ever runs, so no auth logic lives here.
 */
public class WebEditorHttpHandler implements HttpHandler {

    private final ApiHandler apiHandler;
    private final StaticFileHandler staticHandler;

    public WebEditorHttpHandler(SwagMenus plugin) {
        this.apiHandler = new ApiHandler(plugin);
        this.staticHandler = new StaticFileHandler();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path.startsWith("/api/")) {
            apiHandler.handle(exchange);
        } else {
            staticHandler.handle(exchange);
        }
    }
}
