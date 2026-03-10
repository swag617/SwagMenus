package com.swag.swagmenus.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Menu {

    private final String name;
    private final String title;
    private final int size;
    private final List<String> openCommands;
    private final int updateInterval; // ticks; 0 = no auto-refresh
    private final List<MenuItem> items;
    private final MenuItem fillItem;

    private Menu(Builder builder) {
        this.name = builder.name;
        this.title = builder.title;
        this.size = builder.size;
        this.openCommands = Collections.unmodifiableList(builder.openCommands);
        this.updateInterval = builder.updateInterval;
        this.items = Collections.unmodifiableList(builder.items);
        this.fillItem = builder.fillItem;
    }

    public String getName() { return name; }
    public String getTitle() { return title; }
    public int getSize() { return size; }
    public List<String> getOpenCommands() { return openCommands; }
    public int getUpdateInterval() { return updateInterval; }
    public List<MenuItem> getItems() { return items; }
    public MenuItem getFillItem() { return fillItem; }

    public boolean hasFillItem() { return fillItem != null; }
    public boolean hasAutoRefresh() { return updateInterval > 0; }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private String title = "&8Menu";
        private int size = 54;
        private List<String> openCommands = new ArrayList<>();
        private int updateInterval = 0;
        private List<MenuItem> items = new ArrayList<>();
        private MenuItem fillItem = null;

        private Builder(String name) {
            this.name = name;
        }

        public Builder title(String title) { this.title = title; return this; }
        public Builder size(int size) { this.size = size; return this; }
        public Builder openCommands(List<String> cmds) { this.openCommands = cmds; return this; }
        public Builder updateInterval(int ticks) { this.updateInterval = ticks; return this; }
        public Builder items(List<MenuItem> items) { this.items = items; return this; }
        public Builder fillItem(MenuItem item) { this.fillItem = item; return this; }

        public Menu build() {
            if (size < 9 || size > 54 || size % 9 != 0) {
                throw new IllegalArgumentException(
                        "Invalid menu size " + size + " for menu '" + name
                        + "'. Must be a multiple of 9 between 9 and 54.");
            }
            return new Menu(this);
        }
    }
}
