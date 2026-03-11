package com.swag.swagmenus.manager;

import com.swag.swagmenus.SwagMenus;
import com.swag.swagmenus.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PendingInput input = pending.remove(uuid);
        if (input == null) return;

        event.setCancelled(true);
        String text = event.getMessage();
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
