package com.swag.swagmenus.listener;

import com.swag.swagmenus.SwagMenus;
import com.swag.swagmenus.action.ActionHandler;
import com.swag.swagmenus.manager.MenuManager;
import com.swag.swagmenus.menu.Menu;
import com.swag.swagmenus.menu.MenuItem;
import com.swag.swagmenus.menu.MenuSession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuListener implements Listener {

    private final SwagMenus plugin;
    private final MenuManager menuManager;
    private final ActionHandler actionHandler;

    // Debounces rapid double/triple-clicks on the same slot so a single spam-click can't fire
    // an item's commands twice before the first click's effects (e.g. [money_take], [console]
    // give) are visible — closes the economy dupe exploit noted in the audit. Keyed per
    // player+slot rather than globally so clicking a different item immediately after isn't
    // delayed.
    private final Map<UUID, Map<Integer, Long>> lastClickTimes = new ConcurrentHashMap<>();
    private static final long CLICK_COOLDOWN_MS = 300L;

    public MenuListener(SwagMenus plugin) {
        this.plugin = plugin;
        this.menuManager = plugin.getMenuManager();
        this.actionHandler = plugin.getActionHandler();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        MenuSession session = menuManager.getSession(player);
        if (session == null) return;

        event.setCancelled(true);

        if (session.isAnimating()) return;
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(session.getInventory())) return;

        int slot = event.getRawSlot();
        Menu menu = session.getMenu();

        if (slot < 0 || slot >= menu.getSize()) return;

        MenuItem menuItem = findItemAtSlot(menu, slot);
        if (menuItem == null) return;

        if (isOnCooldown(player, slot)) return;

        ClickType clickType = event.getClick();
        List<String> commands;

        if (menuItem.isPlayerList()) {
            // Resolve which online player was clicked
            List<? extends Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
            int playerIndex = slot - menuItem.getSlotStart();
            if (playerIndex < 0 || playerIndex >= online.size()) return;
            String targetName = online.get(playerIndex).getName();

            List<String> raw = getCommandsForClick(menuItem, clickType);
            if (raw == null || raw.isEmpty()) return;
            commands = raw.stream()
                    .map(c -> c.replace("{player_name}", targetName))
                    .toList();
        } else {
            commands = getCommandsForClick(menuItem, clickType);
            if (commands == null || commands.isEmpty()) return;

            boolean hasRequirement = switch (clickType) {
                case RIGHT, SHIFT_RIGHT -> menuItem.hasRightClickRequirement();
                default -> menuItem.hasLeftClickRequirement();
            };

            if (hasRequirement) {
                var requirement = switch (clickType) {
                    case RIGHT, SHIFT_RIGHT -> menuItem.getRightClickRequirement();
                    default -> menuItem.getLeftClickRequirement();
                };
                if (!requirement.checkAndDeny(player, actionHandler)) {
                    return;
                }
            }
        }

        actionHandler.executeActions(player, commands, menu.getName(), menuItem);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        MenuSession session = menuManager.getSession(player);
        if (session == null) return;

        for (int slot : event.getRawSlots()) {
            if (slot < session.getMenu().getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        menuManager.handleMenuClose(player);
        lastClickTimes.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        menuManager.handleMenuClose(event.getPlayer());
        lastClickTimes.remove(event.getPlayer().getUniqueId());
    }

    /**
     * True if this player clicked this exact slot within the last {@link #CLICK_COOLDOWN_MS}.
     * Records the click time as a side effect (so the next click starts a fresh window).
     */
    private boolean isOnCooldown(Player player, int slot) {
        long now = System.currentTimeMillis();
        Map<Integer, Long> perSlot = lastClickTimes.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        Long last = perSlot.put(slot, now);
        return last != null && (now - last) < CLICK_COOLDOWN_MS;
    }

    private MenuItem findItemAtSlot(Menu menu, int slot) {
        // Iterate in reverse to give later-defined items priority over fill items
        List<MenuItem> items = menu.getItems();
        for (int i = items.size() - 1; i >= 0; i--) {
            MenuItem item = items.get(i);
            if (item.isPlayerList()) {
                int count = Bukkit.getOnlinePlayers().size();
                int start = item.getSlotStart();
                if (slot >= start && slot < start + count && slot < menu.getSize()) {
                    return item;
                }
            } else if (item.getSlots().contains(slot)) {
                return item;
            }
        }
        return null;
    }

    private List<String> getCommandsForClick(MenuItem item, ClickType clickType) {
        return switch (clickType) {
            case LEFT -> item.getLeftClickCommands();
            case RIGHT -> item.getRightClickCommands();
            case SHIFT_LEFT -> {
                List<String> cmds = item.getShiftLeftClickCommands();
                yield cmds.isEmpty() ? item.getLeftClickCommands() : cmds;
            }
            case SHIFT_RIGHT -> {
                List<String> cmds = item.getShiftRightClickCommands();
                yield cmds.isEmpty() ? item.getRightClickCommands() : cmds;
            }
            case MIDDLE -> item.getMiddleClickCommands();
            default -> item.getLeftClickCommands();
        };
    }
}
