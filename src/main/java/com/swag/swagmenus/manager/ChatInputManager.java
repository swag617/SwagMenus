package com.swag.swagmenus.manager;

import com.swag.swagmenus.SwagMenus;
import com.swag.swagmenus.util.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatInputManager implements Listener {

    private record PendingInput(List<String> actions) {}

    private final SwagMenus plugin;
    private final Map<UUID, PendingInput> pending = new ConcurrentHashMap<>();

    public ChatInputManager(SwagMenus plugin) {
        this.plugin = plugin;
    }

    public void await(Player player, String prompt, List<String> actions) {
        pending.put(player.getUniqueId(), new PendingInput(actions));
        // Close the inventory first, then send the prompt on the next tick so the
        // InventoryCloseEvent fires before we register interest (avoids session teardown
        // racing with the prompt message).
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.closeInventory();
            player.sendMessage(ColorUtil.toComponent(prompt));
        });
    }

    public boolean isAwaiting(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    /**
     * Uses Paper's native, Adventure-based chat event rather than the deprecated
     * {@code org.bukkit.event.player.AsyncPlayerChatEvent}. Paper still fires the legacy event
     * for backward compatibility today, but it's explicitly deprecated and not guaranteed to
     * keep firing (e.g. a chat plugin that only hooks AsyncChatEvent and cancels chat before the
     * legacy bridge runs would silently break every [chat_input] prompt).
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PendingInput input = pending.remove(uuid);
        if (input == null) return;

        event.setCancelled(true);
        String text = PlainTextComponentSerializer.plainText().serialize(event.message());
        Player player = event.getPlayer();

        List<String> resolved = input.actions().stream()
                .map(a -> a.replace("{input}", text))
                .toList();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                plugin.getActionHandler().executeActions(player, resolved, null);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }
}
