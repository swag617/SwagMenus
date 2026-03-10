package com.swag.swagmenus.action;

import com.swag.swagmenus.SwagMenus;
import com.swag.swagmenus.menu.MenuSession;
import com.swag.swagmenus.util.ColorUtil;
import com.swag.swagmenus.util.PlaceholderUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ActionHandler {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private final SwagMenus plugin;

    public ActionHandler(SwagMenus plugin) {
        this.plugin = plugin;
    }

    public void executeActions(Player player, List<String> actions, String currentMenuName) {
        if (actions == null || actions.isEmpty()) return;
        executeFromIndex(player, actions, 0, currentMenuName);
    }

    private void executeFromIndex(Player player, List<String> actions, int startIndex, String currentMenuName) {
        for (int i = startIndex; i < actions.size(); i++) {
            String action = actions.get(i).trim();
            if (action.isEmpty()) continue;

            if (!action.startsWith("[")) {
                // Legacy: treat as a player command
                executeSingle(player, "[player] " + action, currentMenuName);
                continue;
            }

            int closeBracket = action.indexOf(']');
            if (closeBracket == -1) {
                LOG.warning("Malformed action (missing ']'): " + action);
                continue;
            }

            String type = action.substring(1, closeBracket).toLowerCase().trim();
            String args = action.substring(closeBracket + 1).trim();

            if (type.equals("delay")) {
                long ticks;
                try {
                    ticks = Long.parseLong(args.trim());
                } catch (NumberFormatException e) {
                    LOG.warning("Invalid delay ticks: " + args);
                    ticks = 1;
                }
                final int nextIndex = i + 1;
                final List<String> remaining = new ArrayList<>(actions.subList(nextIndex, actions.size()));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        executeFromIndex(player, remaining, 0, currentMenuName);
                    }
                }, ticks);
                return;
            }

            executeSingle(player, action, currentMenuName);
        }
    }

    /**
     * Suppresses deprecation for {@code player.chat()} — there is no non-deprecated API
     * to send a raw chat message as a player in Paper 1.21 without triggering async chat events.
     */
    @SuppressWarnings("deprecation")
    private void executeSingle(Player player, String action, String currentMenuName) {
        if (!action.startsWith("[")) {
            runPlayerCommand(player, PlaceholderUtil.apply(action, player));
            return;
        }

        int closeBracket = action.indexOf(']');
        if (closeBracket == -1) return;

        String type = action.substring(1, closeBracket).toLowerCase().trim();
        String args = PlaceholderUtil.apply(action.substring(closeBracket + 1).trim(), player);

        switch (type) {
            case "player" -> runPlayerCommand(player, args);
            case "console" -> runConsoleCommand(args);
            case "op" -> runAsOp(player, args);
            case "chat" -> {
                final String chatMsg = args;
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (chatMsg.startsWith("/")) {
                        Bukkit.dispatchCommand(player, chatMsg.substring(1));
                    } else {
                        player.chat(chatMsg);
                    }
                });
            }
            case "message" -> player.sendMessage(ColorUtil.toComponent(args));
            case "broadcast" -> {
                Component msg = ColorUtil.toComponent(args);
                Bukkit.broadcast(msg);
            }
            case "close" -> Bukkit.getScheduler().runTask(plugin, (Runnable) player::closeInventory);
            case "open" -> {
                // Delay by 2 ticks so any preceding [close] action completes first.
                // [close] schedules on the next tick; [open] on the tick after that,
                // preventing the InventoryCloseEvent from firing after the new menu opens.
                String menuName = args.trim();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getMenuManager().openMenu(player, menuName);
                    }
                }, 2L);
            }
            case "nextpage" -> {
                MenuSession sess = plugin.getMenuManager().getSession(player);
                if (sess != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        sess.setCurrentPage(sess.getCurrentPage() + 1);
                        plugin.getMenuManager().populateInventory(player, sess.getMenu(), sess.getInventory());
                    });
                }
            }
            case "prevpage" -> {
                MenuSession sess = plugin.getMenuManager().getSession(player);
                if (sess != null && sess.getCurrentPage() > 1) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        sess.setCurrentPage(sess.getCurrentPage() - 1);
                        plugin.getMenuManager().populateInventory(player, sess.getMenu(), sess.getInventory());
                    });
                }
            }
            case "back" -> Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    plugin.getMenuManager().navigateBack(player);
                }
            }, 2L);
            case "openforplayer" -> {
                String[] parts = args.trim().split("\\s+", 2);
                if (parts.length < 2) {
                    LOG.warning("[openforplayer] requires <menu> <player>: " + action);
                    break;
                }
                String menuName = parts[0];
                Player target = Bukkit.getPlayer(parts[1]);
                if (target != null) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            plugin.getMenuManager().openMenu(target, menuName));
                }
            }
            case "sound" -> playSound(player, args);
            case "title" -> showTitle(player, args);
            case "actionbar" -> player.sendActionBar(ColorUtil.toComponent(args));
            case "refresh" -> {
                if (currentMenuName != null) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            plugin.getMenuManager().refreshMenu(player));
                }
            }
            case "json" -> {
                try {
                    Component component = GsonComponentSerializer.gson().deserialize(args);
                    player.sendMessage(component);
                } catch (Exception e) {
                    LOG.warning("Failed to parse JSON action: " + args + " — " + e.getMessage());
                }
            }
            default -> LOG.warning("Unknown action type '" + type + "' in action: " + action);
        }
    }

    private void runPlayerCommand(Player player, String command) {
        String cmd = command.startsWith("/") ? command.substring(1) : command;
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.dispatchCommand(player, cmd));
    }

    private void runConsoleCommand(String command) {
        String cmd = command.startsWith("/") ? command.substring(1) : command;
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
    }

    private void runAsOp(Player player, String command) {
        if (player.isOp()) {
            runPlayerCommand(player, command);
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                player.setOp(true);
                String cmd = command.startsWith("/") ? command.substring(1) : command;
                Bukkit.dispatchCommand(player, cmd);
            } finally {
                player.setOp(false);
            }
        });
    }

    private void playSound(Player player, String args) {
        String[] parts = args.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return;

        // Minecraft sound keys use dots (entity.player.levelup) but configs often use
        // underscores (ENTITY_PLAYER_LEVELUP). We try both forms.
        String rawInput = parts[0];
        Sound sound = resolveSoundByKey(rawInput);
        if (sound == null) {
            LOG.warning("Unknown sound: " + rawInput
                    + ". Use Minecraft sound IDs like 'entity.player.levelup'.");
            return;
        }

        float volume = 1.0f;
        float pitch = 1.0f;

        if (parts.length >= 2) {
            try { volume = Float.parseFloat(parts[1]); } catch (NumberFormatException ignored) {}
        }
        if (parts.length >= 3) {
            try { pitch = Float.parseFloat(parts[2]); } catch (NumberFormatException ignored) {}
        }

        final float finalVolume = volume;
        final float finalPitch = pitch;
        Bukkit.getScheduler().runTask(plugin, () ->
                player.playSound(player.getLocation(), sound, finalVolume, finalPitch));
    }

    /**
     * Resolves a Sound from a string key. Accepts three formats:
     * {@code entity.player.levelup} (Minecraft dot notation),
     * {@code ENTITY_PLAYER_LEVELUP} (legacy Bukkit enum name — converted to dots),
     * and {@code minecraft:entity.player.levelup} (namespaced).
     */
    private Sound resolveSoundByKey(String input) {
        String lower = input.toLowerCase();

        Sound sound = org.bukkit.Registry.SOUNDS.get(org.bukkit.NamespacedKey.minecraft(lower));
        if (sound != null) return sound;

        // Convert ENUM_NAME_FORMAT → dot.separated.format
        String dotted = lower.replace('_', '.');
        sound = org.bukkit.Registry.SOUNDS.get(org.bukkit.NamespacedKey.minecraft(dotted));
        if (sound != null) return sound;

        if (lower.startsWith("minecraft:")) {
            String stripped = lower.substring("minecraft:".length());
            sound = org.bukkit.Registry.SOUNDS.get(org.bukkit.NamespacedKey.minecraft(stripped));
            if (sound != null) return sound;
        }

        return null;
    }

    private void showTitle(Player player, String args) {
        // Format: "Title text;Subtitle text"
        String[] parts = args.split(";", 2);
        Component title = ColorUtil.toComponent(parts[0].trim());
        Component subtitle = parts.length > 1 ? ColorUtil.toComponent(parts[1].trim()) : Component.empty();

        Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(3000),
                Duration.ofMillis(500)
        );

        Bukkit.getScheduler().runTask(plugin, () ->
                player.showTitle(Title.title(title, subtitle, times)));
    }
}
