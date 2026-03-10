package com.swag.swagmenus.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Fluent builder for ItemStacks. Uses {@link com.destroystokyo.paper.profile.PlayerProfile}
 * for skull textures (non-deprecated in Paper 1.21).
 */
public final class ItemBuilder {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack base) {
        this.item = base.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    public ItemBuilder name(Component name) {
        if (meta != null) meta.displayName(name);
        return this;
    }

    public ItemBuilder name(String legacyName) {
        return name(ColorUtil.toComponent(legacyName));
    }

    public ItemBuilder lore(List<Component> lore) {
        if (meta != null) meta.lore(lore);
        return this;
    }

    public ItemBuilder loreStrings(List<String> lore) {
        return lore(ColorUtil.toComponents(lore));
    }

    /**
     * Adds an invisible enchantment to produce a glowing effect, then hides the enchantment
     * tag via {@link ItemFlag#HIDE_ENCHANTS}. LUCK_OF_THE_SEA applies to all material types.
     */
    public ItemBuilder glow(boolean glow) {
        if (!glow || meta == null) return this;
        meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder customModelData(int data) {
        if (meta != null && data != 0) meta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder hideFlags(boolean hide) {
        if (!hide || meta == null) return this;
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    /**
     * Sets the skull owner by player name. The deprecation is suppressed — skull heads by
     * name are a deliberate menu-author choice, not an oversight.
     */
    @SuppressWarnings("deprecation")
    public ItemBuilder skullOwner(String playerName) {
        if (meta instanceof SkullMeta skullMeta && playerName != null && !playerName.isEmpty()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            skullMeta.setOwningPlayer(offlinePlayer);
        }
        return this;
    }

    /**
     * Sets a skull texture. Accepts:
     * - A raw Base64 string decoding to {@code {"textures":{"SKIN":{"url":"..."}}}}
     * - A direct {@code https://textures.minecraft.net/...} URL (wrapped automatically)
     */
    public ItemBuilder skullTexture(String base64OrUrl) {
        if (!(meta instanceof SkullMeta skullMeta) || base64OrUrl == null || base64OrUrl.isEmpty()) {
            return this;
        }

        try {
            String textureValue = ensureBase64Texture(base64OrUrl);
            if (textureValue == null) {
                LOG.warning("Could not process skull texture: "
                        + base64OrUrl.substring(0, Math.min(40, base64OrUrl.length())));
                return this;
            }

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "SwagMenusSkull");
            profile.setProperty(new ProfileProperty("textures", textureValue));
            skullMeta.setPlayerProfile(profile);
        } catch (Exception e) {
            LOG.warning("Failed to set skull texture: " + e.getMessage());
        }
        return this;
    }

    private String ensureBase64Texture(String input) {
        if (input.startsWith("http://") || input.startsWith("https://")) {
            String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + input + "\"}}}";
            return Base64.getEncoder().encodeToString(json.getBytes());
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(input);
            String decodedStr = new String(decoded);
            if (decodedStr.contains("textures") || decodedStr.contains("SKIN")) {
                return input;
            }
            if (decodedStr.startsWith("http")) {
                String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + decodedStr.trim() + "\"}}}";
                return Base64.getEncoder().encodeToString(json.getBytes());
            }
        } catch (IllegalArgumentException ignored) {
            // Not valid Base64 — fall through
        }

        return null;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        if (meta != null) meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemStack build() {
        if (meta != null) item.setItemMeta(meta);
        return item;
    }
}
