package com.swag.swagmenus.util;

import com.swag.swagmenus.SwagMenus;

import java.util.logging.Logger;

/**
 * Thin wrapper around the {@code debug: true} config key. Previously this key was read nowhere
 * in the codebase despite its own config comment promising "detailed debug information" — this
 * class is what actually makes it do something.
 *
 * Kept intentionally lightweight: a handful of call sites (menu load timing, action execution,
 * requirement evaluation) log through here so admins troubleshooting a menu can flip one config
 * key instead of reading source code.
 */
public final class DebugLog {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private static volatile SwagMenus plugin;

    private DebugLog() {}

    public static void init(SwagMenus pluginInstance) {
        plugin = pluginInstance;
    }

    public static boolean isEnabled() {
        return plugin != null && plugin.getConfig().getBoolean("debug", false);
    }

    public static void log(String message) {
        if (isEnabled()) {
            LOG.info("[debug] " + message);
        }
    }
}
