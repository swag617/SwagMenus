package com.swag.swagmenus.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps PlaceholderAPI integration with graceful fallback when PAPI is not installed.
 */
public final class PlaceholderUtil {

    private PlaceholderUtil() {}

    /**
     * Returns true if PlaceholderAPI is installed and enabled.
     */
    public static boolean isEnabled() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
                && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /**
     * Applies PlaceholderAPI replacements to a string for the given player.
     * If PAPI is not available, returns the input unchanged.
     * Also replaces the built-in %player_name% and %player_displayname% without PAPI.
     */
    public static String apply(String input, Player player) {
        if (input == null) return "";
        String result = applyBuiltIn(input, player);
        if (isEnabled() && player != null) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }
        return result;
    }

    /**
     * Applies placeholders to an OfflinePlayer (for head textures, etc).
     */
    public static String apply(String input, OfflinePlayer player) {
        if (input == null) return "";
        if (player instanceof Player online) {
            return apply(input, online);
        }
        String result = input;
        if (player != null && player.getName() != null) {
            result = result.replace("%player_name%", player.getName());
        }
        if (isEnabled() && player != null) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }
        return result;
    }

    /**
     * Applies placeholders to a list of strings.
     */
    public static List<String> apply(List<String> input, Player player) {
        if (input == null) return new ArrayList<>();
        List<String> result = new ArrayList<>(input.size());
        for (String s : input) {
            result.add(apply(s, player));
        }
        return result;
    }

    /**
     * Built-in placeholder replacements that work without PAPI.
     */
    private static String applyBuiltIn(String input, Player player) {
        if (player == null) return input;
        // Use net.kyori.adventure.text.serializer.legacy to get display name as string
        // player.displayName() returns a Component; we serialize it to a legacy string.
        String displayName = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacySection().serialize(player.displayName());
        return input
                .replace("%player_name%", player.getName())
                .replace("%player_displayname%", displayName)
                .replace("%player_uuid%", player.getUniqueId().toString())
                .replace("%player_health%", String.format("%.1f", player.getHealth()))
                .replace("%player_food_level%", String.valueOf(player.getFoodLevel()))
                .replace("%player_level%", String.valueOf(player.getLevel()))
                .replace("%player_world%", player.getWorld().getName());
    }
}
