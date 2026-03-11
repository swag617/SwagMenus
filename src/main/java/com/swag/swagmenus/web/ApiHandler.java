package com.swag.swagmenus.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.swag.swagmenus.SwagMenus;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles all {@code /api/*} REST endpoints for the web editor.
 *
 * Auth: every request must carry a valid {@code X-Auth-Token} header.
 * All responses are JSON. CORS headers are added for dev convenience.
 *
 * Endpoints:
 *   GET    /api/menus                 — list menu names
 *   GET    /api/menus/{name}          — get menu YAML as JSON
 *   POST   /api/menus/{name}          — create / overwrite menu
 *   PUT    /api/menus/{name}          — partial update (merge)
 *   DELETE /api/menus/{name}          — delete menu file
 *   POST   /api/menus/{name}/reload   — reload menu in-game
 *   GET    /api/materials             — all valid Material names
 *   GET    /api/sounds                — all valid Sound names
 */
public class ApiHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final SwagMenus plugin;
    private final AuthManager auth;

    public ApiHandler(SwagMenus plugin, AuthManager auth) {
        this.plugin = plugin;
        this.auth = auth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-Auth-Token");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        if (path.equals("/api/login") && "POST".equals(method)) {
            handleLogin(exchange);
            return;
        }

        String token = exchange.getRequestHeaders().getFirst("X-Auth-Token");
        if (!auth.validate(token)) {
            sendJson(exchange, 401, mapOf("error", "Unauthorized — please log in"));
            return;
        }

        try {
            route(exchange, method, path);
        } catch (Exception e) {
            LOG.warning("WebEditor unhandled error in API handler: " + e.getMessage());
            sendJson(exchange, 500, mapOf("error", "Internal server error: " + e.getMessage()));
        }
    }

    private void route(HttpExchange exchange, String method, String path) throws IOException {
        String sub = path.startsWith("/api") ? path.substring(4) : path;

        if (sub.equals("/materials") || sub.equals("/materials/")) {
            if ("GET".equals(method)) { handleMaterials(exchange); return; }
        }

        if (sub.equals("/sounds") || sub.equals("/sounds/")) {
            if ("GET".equals(method)) { handleSounds(exchange); return; }
        }

        if (sub.equals("/menus") || sub.equals("/menus/")) {
            if ("GET".equals(method)) { handleListMenus(exchange); return; }
        }

        if (sub.startsWith("/menus/")) {
            String rest = sub.substring("/menus/".length());

            if (rest.endsWith("/reload")) {
                String menuName = rest.substring(0, rest.length() - "/reload".length()).toLowerCase();
                if ("POST".equals(method)) { handleReloadMenu(exchange, menuName); return; }
            } else {
                String menuName = rest.toLowerCase();
                switch (method) {
                    case "GET"    -> handleGetMenu(exchange, menuName);
                    case "POST"   -> handleCreateMenu(exchange, menuName);
                    case "PUT"    -> handleUpdateMenu(exchange, menuName);
                    case "DELETE" -> handleDeleteMenu(exchange, menuName);
                    default -> sendJson(exchange, 405, mapOf("error", "Method not allowed"));
                }
                return;
            }
        }

        sendJson(exchange, 404, mapOf("error", "API endpoint not found: " + path));
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        JsonObject json;
        try {
            json = JsonParser.parseString(body).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            sendJson(exchange, 400, mapOf("error", "Invalid JSON"));
            return;
        }

        String submitted = json.has("password") ? json.get("password").getAsString() : "";
        String correct = plugin.getConfig().getString("web_editor.password", "admin");

        if (!submitted.equals(correct)) {
            sendJson(exchange, 401, mapOf("error", "Incorrect password"));
            return;
        }

        String token = auth.createSession();
        sendJson(exchange, 200, mapOf("token", token));
    }

    private void handleListMenus(HttpExchange exchange) throws IOException {
        List<String> names = new ArrayList<>(plugin.getMenuManager().getMenus().keySet());
        names.sort(String::compareTo);
        sendJson(exchange, 200, names);
    }

    private void handleGetMenu(HttpExchange exchange, String menuName) throws IOException {
        File file = menuFile(menuName);
        if (!file.exists()) {
            sendJson(exchange, 404, mapOf("error", "Menu not found: " + menuName));
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> map = yamlToMap(yaml);
        sendJson(exchange, 200, map);
    }

    private void handleCreateMenu(HttpExchange exchange, String menuName) throws IOException {
        String body = readBody(exchange);
        JsonObject json;
        try {
            json = JsonParser.parseString(body).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            sendJson(exchange, 400, mapOf("error", "Invalid JSON body: " + e.getMessage()));
            return;
        }

        YamlConfiguration yaml = new YamlConfiguration();
        applyJsonToYaml(json, yaml, "");

        File file = menuFile(menuName);
        try {
            yaml.save(file);
        } catch (IOException e) {
            sendJson(exchange, 500, mapOf("error", "Failed to save menu: " + e.getMessage()));
            return;
        }

        scheduleReload(menuName);
        sendJson(exchange, 200, mapOf("status", "created", "menu", menuName));
    }

    private void handleUpdateMenu(HttpExchange exchange, String menuName) throws IOException {
        File file = menuFile(menuName);
        YamlConfiguration yaml = file.exists()
                ? YamlConfiguration.loadConfiguration(file)
                : new YamlConfiguration();

        String body = readBody(exchange);
        JsonObject json;
        try {
            json = JsonParser.parseString(body).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            sendJson(exchange, 400, mapOf("error", "Invalid JSON body: " + e.getMessage()));
            return;
        }

        applyJsonToYaml(json, yaml, "");

        try {
            yaml.save(file);
        } catch (IOException e) {
            sendJson(exchange, 500, mapOf("error", "Failed to save menu: " + e.getMessage()));
            return;
        }

        scheduleReload(menuName);
        sendJson(exchange, 200, mapOf("status", "updated", "menu", menuName));
    }

    private void handleDeleteMenu(HttpExchange exchange, String menuName) throws IOException {
        File file = menuFile(menuName);
        if (!file.exists()) {
            sendJson(exchange, 404, mapOf("error", "Menu not found: " + menuName));
            return;
        }
        boolean deleted = file.delete();
        if (!deleted) {
            sendJson(exchange, 500, mapOf("error", "Failed to delete menu file"));
            return;
        }
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () ->
                plugin.getMenuManager().removeMenu(menuName));
        sendJson(exchange, 200, mapOf("status", "deleted", "menu", menuName));
    }

    private void handleReloadMenu(HttpExchange exchange, String menuName) throws IOException {
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () ->
                plugin.getMenuManager().reloadMenu(menuName));
        sendJson(exchange, 200, mapOf("status", "reloading", "menu", menuName));
    }

    private void handleMaterials(HttpExchange exchange) throws IOException {
        List<String> names = Arrays.stream(Material.values())
                .filter(m -> !m.isLegacy())
                .map(Enum::name)
                .sorted()
                .toList();
        sendJson(exchange, 200, names);
    }

    private void handleSounds(HttpExchange exchange) throws IOException {
        List<String> names = new ArrayList<>();
        Registry.SOUNDS.forEach(sound ->
                names.add(sound.getKey().getKey().toUpperCase().replace('.', '_'))
        );
        names.sort(String::compareTo);
        sendJson(exchange, 200, names);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> yamlToMap(ConfigurationSection section) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection sub) {
                map.put(key, yamlToMap(sub));
            } else if (value instanceof List<?> list) {
                map.put(key, convertList(list));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private List<Object> convertList(List<?> list) {
        List<Object> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> m) {
                Map<String, Object> converted = new LinkedHashMap<>();
                for (Map.Entry<?, ?> e : m.entrySet()) {
                    converted.put(String.valueOf(e.getKey()), e.getValue());
                }
                result.add(converted);
            } else {
                result.add(item);
            }
        }
        return result;
    }

    private void applyJsonToYaml(JsonObject json, YamlConfiguration yaml, String prefix) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            JsonElement el = entry.getValue();
            setYamlValue(yaml, key, el);
        }
    }

    private void setYamlValue(YamlConfiguration yaml, String key, JsonElement el) {
        if (el.isJsonNull()) {
            yaml.set(key, null);
        } else if (el.isJsonPrimitive()) {
            var prim = el.getAsJsonPrimitive();
            if (prim.isBoolean()) {
                yaml.set(key, prim.getAsBoolean());
            } else if (prim.isNumber()) {
                // Preserve integers where possible to avoid writing "1.0" instead of "1"
                double d = prim.getAsDouble();
                if (d == Math.floor(d) && !Double.isInfinite(d) && Math.abs(d) < Integer.MAX_VALUE) {
                    yaml.set(key, (int) d);
                } else {
                    yaml.set(key, d);
                }
            } else {
                yaml.set(key, prim.getAsString());
            }
        } else if (el.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement item : el.getAsJsonArray()) {
                list.add(jsonElementToObject(item));
            }
            yaml.set(key, list);
        } else if (el.isJsonObject()) {
            for (Map.Entry<String, JsonElement> child : el.getAsJsonObject().entrySet()) {
                setYamlValue(yaml, key + "." + child.getKey(), child.getValue());
            }
        }
    }

    private Object jsonElementToObject(JsonElement el) {
        if (el.isJsonNull()) return null;
        if (el.isJsonPrimitive()) {
            var prim = el.getAsJsonPrimitive();
            if (prim.isBoolean()) return prim.getAsBoolean();
            if (prim.isNumber()) {
                double d = prim.getAsDouble();
                if (d == Math.floor(d) && !Double.isInfinite(d) && Math.abs(d) < Integer.MAX_VALUE) {
                    return (int) d;
                }
                return d;
            }
            return prim.getAsString();
        }
        if (el.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement item : el.getAsJsonArray()) {
                list.add(jsonElementToObject(item));
            }
            return list;
        }
        if (el.isJsonObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> e : el.getAsJsonObject().entrySet()) {
                map.put(e.getKey(), jsonElementToObject(e.getValue()));
            }
            return map;
        }
        return null;
    }

    private File menuFile(String menuName) {
        return new File(plugin.getMenuManager().getMenusFolder(), menuName + ".yml");
    }

    private void scheduleReload(String menuName) {
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () ->
                plugin.getMenuManager().reloadMenu(menuName));
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static void sendJson(HttpExchange exchange, int status, Object payload) throws IOException {
        byte[] bytes = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, Object> mapOf(String k, Object v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k, v);
        return m;
    }

    private static Map<String, Object> mapOf(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }
}
