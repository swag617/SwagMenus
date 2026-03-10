package com.swag.swagmenus.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MenuSession {

    private final Player player;
    private final Menu menu;
    private final Inventory inventory;
    private long lastRefreshTime;
    private int currentPage = 1;

    public MenuSession(Player player, Menu menu, Inventory inventory) {
        this.player = player;
        this.menu = menu;
        this.inventory = inventory;
        this.lastRefreshTime = System.currentTimeMillis();
    }

    public Player getPlayer() { return player; }
    public Menu getMenu() { return menu; }
    public Inventory getInventory() { return inventory; }
    public long getLastRefreshTime() { return lastRefreshTime; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int page) { this.currentPage = Math.max(1, page); }

    public void updateLastRefreshTime() {
        this.lastRefreshTime = System.currentTimeMillis();
    }
}
