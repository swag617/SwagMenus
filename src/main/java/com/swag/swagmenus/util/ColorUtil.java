package com.swag.swagmenus.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Color code translation for legacy {@code &} codes and {@code &#RRGGBB} hex codes.
 * Does not use deprecated {@code ChatColor} utilities.
 */
public final class ColorUtil {

    // Matches &#RRGGBB hex color codes
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private static final String COLOR_CHARS = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacySection();

    private ColorUtil() {}

    /**
     * Translates {@code &} color codes and {@code &#RRGGBB} hex codes into a legacy
     * §-based string. Suitable for inventory titles and other legacy-string contexts.
     */
    public static String colorize(String input) {
        if (input == null) return "";
        String hex = translateHex(input);
        return translateLegacyCodes(hex);
    }

    public static List<String> colorize(List<String> input) {
        if (input == null) return new ArrayList<>();
        List<String> result = new ArrayList<>(input.size());
        for (String s : input) {
            result.add(colorize(s));
        }
        return result;
    }

    public static Component toComponent(String input) {
        if (input == null) return Component.empty();
        return LEGACY_SERIALIZER.deserialize(colorize(input));
    }

    public static List<Component> toComponents(List<String> input) {
        if (input == null) return new ArrayList<>();
        List<Component> result = new ArrayList<>(input.size());
        for (String s : input) {
            result.add(toComponent(s));
        }
        return result;
    }

    /**
     * Strips all color/format codes from a string without using deprecated
     * {@code ChatColor.stripColor}.
     */
    public static String strip(String input) {
        if (input == null) return "";
        String colorized = colorize(input);
        return colorized.replaceAll("§[0-9A-Fa-fK-Ok-oRrXx]", "");
    }

    /**
     * Converts {@code &#RRGGBB} sequences into the legacy {@code §x§R§R§G§G§B§B} format
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
                i++;
            } else {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }
}
