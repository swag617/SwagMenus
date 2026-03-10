package com.swag.swagmenus.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles color code translation including legacy & codes and &#RRGGBB hex colors.
 * Uses the Adventure API for Paper 1.21.x.
 * Does NOT use deprecated {@code ChatColor} utilities.
 */
public final class ColorUtil {

    // Matches &#RRGGBB hex color codes
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    // All valid & color/format codes
    private static final String COLOR_CHARS = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    // Reusable legacy serializer (§-based)
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacySection();

    private ColorUtil() {}

    /**
     * Translates a string containing {@code &} color codes and {@code &#RRGGBB} hex codes into
     * a colored string using legacy {@code §} characters.
     * This is suitable for inventory titles and other legacy-string contexts.
     */
    public static String colorize(String input) {
        if (input == null) return "";
        String hex = translateHex(input);
        return translateLegacyCodes(hex);
    }

    /**
     * Translates a list of strings.
     */
    public static List<String> colorize(List<String> input) {
        if (input == null) return new ArrayList<>();
        List<String> result = new ArrayList<>(input.size());
        for (String s : input) {
            result.add(colorize(s));
        }
        return result;
    }

    /**
     * Converts a colorized string to an Adventure {@link Component} using the legacy section
     * serializer. Use this when setting display names and lore via the Adventure API.
     */
    public static Component toComponent(String input) {
        if (input == null) return Component.empty();
        return LEGACY_SERIALIZER.deserialize(colorize(input));
    }

    /**
     * Converts a list of strings to a list of Adventure {@link Component}s.
     */
    public static List<Component> toComponents(List<String> input) {
        if (input == null) return new ArrayList<>();
        List<Component> result = new ArrayList<>(input.size());
        for (String s : input) {
            result.add(toComponent(s));
        }
        return result;
    }

    /**
     * Strips all color/format codes (both {@code &x} and {@code §x}) from a string.
     * Does not use deprecated {@code ChatColor.stripColor}.
     */
    public static String strip(String input) {
        if (input == null) return "";
        // Strip §x codes (including hex §x§R§R§G§G§B§B sequences)
        String colorized = colorize(input);
        // Pattern: § followed by any single character
        return colorized.replaceAll("§[0-9A-Fa-fK-Ok-oRrXx]", "");
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Converts {@code &#RRGGBB} hex sequences into the legacy {@code §x§R§R§G§G§B§B} format
     * that Minecraft's legacy color system recognizes.
     */
    private static String translateHex(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Manually translates {@code &X} codes to {@code §X} codes, replacing the deprecated
     * {@code ChatColor.translateAlternateColorCodes('&', input)} call.
     */
    private static String translateLegacyCodes(String input) {
        char[] chars = input.toCharArray();
        StringBuilder sb = new StringBuilder(chars.length);
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 1 < chars.length
                    && COLOR_CHARS.indexOf(chars[i + 1]) != -1) {
                sb.append('§');
                sb.append(chars[i + 1]);
                i++; // skip the code character
            } else {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }
}
