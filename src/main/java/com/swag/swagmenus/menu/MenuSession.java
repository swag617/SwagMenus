package com.swag.swagmenus.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class MenuSession {

    private final Player player;
    private final Menu menu;
    private final Inventory inventory;
    private long lastRefreshTime;
    private int currentPage = 1;
    private boolean animating = false;

    // Lore animation: maps item key -> current frame index
    private final Map<String, Integer> loreFrameIndices = new HashMap<>();
    private int loreAnimTaskId = -1;

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
    public boolean isAnimating() { return animating; }
    public void setAnimating(boolean animating) { this.animating = animating; }
    public Map<String, Integer> getLoreFrameIndices() { return loreFrameIndices; }
    public int getLoreAnimTaskId() { return loreAnimTaskId; }
    public void setLoreAnimTaskId(int id) { this.loreAnimTaskId = id; }

    public void updateLastRefreshTime() {
        this.lastRefreshTime = System.currentTimeMillis();
    }
}
