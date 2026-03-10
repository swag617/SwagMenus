package com.swag.swagmenus.menu;

import com.swag.swagmenus.requirement.RequirementSet;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuItem {

    private final String key;
    private final Material material;
    private final List<Integer> slots;
    private final String displayName;
    private final List<String> lore;
    private final int amount;
    private final boolean glow;
    private final int customModelData;
    private final boolean hideFlags;
    private final String skullOwner;   // supports %placeholders%
    private final String skullTexture; // base64

    private final List<String> leftClickCommands;
    private final List<String> rightClickCommands;
    private final List<String> shiftLeftClickCommands;
    private final List<String> shiftRightClickCommands;
    private final List<String> middleClickCommands;

    private final RequirementSet viewRequirement;
    private final RequirementSet leftClickRequirement;
    private final RequirementSet rightClickRequirement;

    private final MenuItem denyItem;

    // page 0 = show on all pages (e.g. navigation/border items)
    private final int page;

    private MenuItem(Builder builder) {
        this.key = builder.key;
        this.material = builder.material;
        this.slots = Collections.unmodifiableList(builder.slots);
        this.displayName = builder.displayName;
        this.lore = Collections.unmodifiableList(builder.lore);
        this.amount = builder.amount;
        this.glow = builder.glow;
        this.customModelData = builder.customModelData;
        this.hideFlags = builder.hideFlags;
        this.skullOwner = builder.skullOwner;
        this.skullTexture = builder.skullTexture;
        this.leftClickCommands = Collections.unmodifiableList(builder.leftClickCommands);
        this.rightClickCommands = Collections.unmodifiableList(builder.rightClickCommands);
        this.shiftLeftClickCommands = Collections.unmodifiableList(builder.shiftLeftClickCommands);
        this.shiftRightClickCommands = Collections.unmodifiableList(builder.shiftRightClickCommands);
        this.middleClickCommands = Collections.unmodifiableList(builder.middleClickCommands);
        this.viewRequirement = builder.viewRequirement;
        this.leftClickRequirement = builder.leftClickRequirement;
        this.rightClickRequirement = builder.rightClickRequirement;
        this.denyItem = builder.denyItem;
        this.page = builder.page;
    }

    public String getKey() { return key; }
    public Material getMaterial() { return material; }
    public List<Integer> getSlots() { return slots; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public int getAmount() { return amount; }
    public boolean isGlow() { return glow; }
    public int getCustomModelData() { return customModelData; }
    public boolean isHideFlags() { return hideFlags; }
    public String getSkullOwner() { return skullOwner; }
    public String getSkullTexture() { return skullTexture; }
    public List<String> getLeftClickCommands() { return leftClickCommands; }
    public List<String> getRightClickCommands() { return rightClickCommands; }
    public List<String> getShiftLeftClickCommands() { return shiftLeftClickCommands; }
    public List<String> getShiftRightClickCommands() { return shiftRightClickCommands; }
    public List<String> getMiddleClickCommands() { return middleClickCommands; }
    public RequirementSet getViewRequirement() { return viewRequirement; }
    public RequirementSet getLeftClickRequirement() { return leftClickRequirement; }
    public RequirementSet getRightClickRequirement() { return rightClickRequirement; }
    public MenuItem getDenyItem() { return denyItem; }
    public int getPage() { return page; }

    public boolean hasViewRequirement() {
        return viewRequirement != null && !viewRequirement.isEmpty();
    }

    public boolean hasLeftClickRequirement() {
        return leftClickRequirement != null && !leftClickRequirement.isEmpty();
    }

    public boolean hasRightClickRequirement() {
        return rightClickRequirement != null && !rightClickRequirement.isEmpty();
    }

    public static Builder builder(String key, Material material) {
        return new Builder(key, material);
    }

    public static class Builder {
        private final String key;
        private final Material material;
        private List<Integer> slots = new ArrayList<>();
        private String displayName = "";
        private List<String> lore = new ArrayList<>();
        private int amount = 1;
        private boolean glow = false;
        private int customModelData = 0;
        private boolean hideFlags = false;
        private String skullOwner = null;
        private String skullTexture = null;
        private List<String> leftClickCommands = new ArrayList<>();
        private List<String> rightClickCommands = new ArrayList<>();
        private List<String> shiftLeftClickCommands = new ArrayList<>();
        private List<String> shiftRightClickCommands = new ArrayList<>();
        private List<String> middleClickCommands = new ArrayList<>();
        private RequirementSet viewRequirement = null;
        private RequirementSet leftClickRequirement = null;
        private RequirementSet rightClickRequirement = null;
        private MenuItem denyItem = null;
        private int page = 0;

        private Builder(String key, Material material) {
            this.key = key;
            this.material = material;
        }

        public Builder slots(List<Integer> slots) { this.slots = slots; return this; }
        public Builder displayName(String name) { this.displayName = name; return this; }
        public Builder lore(List<String> lore) { this.lore = lore; return this; }
        public Builder amount(int amount) { this.amount = amount; return this; }
        public Builder glow(boolean glow) { this.glow = glow; return this; }
        public Builder customModelData(int data) { this.customModelData = data; return this; }
        public Builder hideFlags(boolean hide) { this.hideFlags = hide; return this; }
        public Builder skullOwner(String owner) { this.skullOwner = owner; return this; }
        public Builder skullTexture(String texture) { this.skullTexture = texture; return this; }
        public Builder leftClickCommands(List<String> cmds) { this.leftClickCommands = cmds; return this; }
        public Builder rightClickCommands(List<String> cmds) { this.rightClickCommands = cmds; return this; }
        public Builder shiftLeftClickCommands(List<String> cmds) { this.shiftLeftClickCommands = cmds; return this; }
        public Builder shiftRightClickCommands(List<String> cmds) { this.shiftRightClickCommands = cmds; return this; }
        public Builder middleClickCommands(List<String> cmds) { this.middleClickCommands = cmds; return this; }
        public Builder viewRequirement(RequirementSet req) { this.viewRequirement = req; return this; }
        public Builder leftClickRequirement(RequirementSet req) { this.leftClickRequirement = req; return this; }
        public Builder rightClickRequirement(RequirementSet req) { this.rightClickRequirement = req; return this; }
        public Builder denyItem(MenuItem item) { this.denyItem = item; return this; }
        public Builder page(int page) { this.page = page; return this; }

        public MenuItem build() {
            return new MenuItem(this);
        }
    }
}
