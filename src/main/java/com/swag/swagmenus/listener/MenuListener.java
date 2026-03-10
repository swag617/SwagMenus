package com.swag.swagmenus.listener;

import com.swag.swagmenus.SwagMenus;
import com.swag.swagmenus.action.ActionHandler;
import com.swag.swagmenus.manager.MenuManager;
import com.swag.swagmenus.menu.Menu;
import com.swag.swagmenus.menu.MenuItem;
import com.swag.swagmenus.menu.MenuSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

/**
 * Handles inventory interaction events for SwagMenus.
 * Cancels all clicks in managed inventories and routes to action execution.
 */
public class MenuListener implements Listener {

    private final SwagMenus plugin;
    private final MenuManager menuManager;
    private final ActionHandler actionHandler;

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

        // Always cancel clicks in managed inventories
        event.setCancelled(true);

        // Only handle top inventory clicks (not player hotbar clicks on bottom)
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(session.getInventory())) return;

        int slot = event.getRawSlot();
        Menu menu = session.getMenu();

        if (slot < 0 || slot >= menu.getSize()) return;

        MenuItem menuItem = findItemAtSlot(menu, slot);
        if (menuItem == null) return;

        // Determine which action list to use based on click type
        ClickType clickType = event.getClick();
        List<String> commands = getCommandsForClick(menuItem, clickType);

        if (commands == null || commands.isEmpty()) return;

        // Check click requirement
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
                return; // Requirement not met; deny commands already executed
            }
        }

        actionHandler.executeActions(player, commands, menu.getName());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        MenuSession session = menuManager.getSession(player);
        if (session == null) return;

        // Cancel any drag that touches the menu inventory
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
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        menuManager.handleMenuClose(event.getPlayer());
    }

    /**
     * Finds the MenuItem defined for the given slot in the menu.
     * Returns null if no item is configured for that slot.
     */
    private MenuItem findItemAtSlot(Menu menu, int slot) {
        // Iterate in reverse to give priority to items defined later (override fill)
        List<MenuItem> items = menu.getItems();
        for (int i = items.size() - 1; i >= 0; i--) {
            MenuItem item = items.get(i);
            if (item.getSlots().contains(slot)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Returns the appropriate command list for the given click type.
     */
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
