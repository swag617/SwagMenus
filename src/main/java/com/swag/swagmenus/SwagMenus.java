package com.swag.swagmenus;

import com.swag.swagmenus.action.ActionHandler;
import com.swag.swagmenus.command.SwagMenusCommand;
import com.swag.swagmenus.listener.MenuListener;
import com.swag.swagmenus.manager.ChatInputManager;
import com.swag.swagmenus.manager.MenuFileWatcher;
import com.swag.swagmenus.manager.MenuManager;
import com.swag.swagmenus.util.ColorUtil;
import com.swag.swagmenus.util.DebugLog;
import com.swag.swagmenus.web.WebEditorServer;
import com.SwagDev.SwagAPI.api.IPrefixService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

public class SwagMenus extends JavaPlugin {

    private static SwagMenus instance;
    private static Economy economy = null;

    private MenuManager menuManager;
    private ActionHandler actionHandler;
    private ChatInputManager chatInputManager;
    private MenuFileWatcher fileWatcher;
    private WebEditorServer webEditorServer;
    private IPrefixService prefixService;

    @Override
    public void onEnable() {
        instance = this;
        Logger log = getLogger();

        log.info("==============================================");
        log.info("  SwagMenus v" + getPluginMeta().getVersion());
        log.info("  Loading...");
        log.info("==============================================");

        saveDefaultConfig();
        DebugLog.init(this);
        generateExampleMenus();

        ServicesManager sm = getServer().getServicesManager();
        RegisteredServiceProvider<IPrefixService> prefixProvider = sm.getRegistration(IPrefixService.class);
        prefixService = (prefixProvider != null) ? prefixProvider.getProvider() : null;

        setupEconomy(log);

        actionHandler = new ActionHandler(this);
        menuManager = new MenuManager(this);
        menuManager.loadAllMenus();

        chatInputManager = new ChatInputManager(this);
        getServer().getPluginManager().registerEvents(chatInputManager, this);

        if (getConfig().getBoolean("auto_reload_on_change", true)) {
            fileWatcher = new MenuFileWatcher(this, menuManager);
            fileWatcher.start();
        }

        webEditorServer = new WebEditorServer(this);
        webEditorServer.register();

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        PluginCommand smCommand = getCommand("sm");
        if (smCommand != null) {
            SwagMenusCommand commandExecutor = new SwagMenusCommand(this);
            smCommand.setExecutor(commandExecutor);
            smCommand.setTabCompleter(commandExecutor);
        } else {
            log.severe("Could not find 'sm' command in plugin.yml! Commands will not work.");
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            log.info("PlaceholderAPI found — placeholder support enabled.");
        } else {
            log.info("PlaceholderAPI not found — only built-in placeholders will work.");
        }

        log.info("Enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (webEditorServer != null) {
            webEditorServer.unregister();
        }
        if (fileWatcher != null) {
            fileWatcher.stop();
        }
        if (menuManager != null) {
            menuManager.shutdown();
        }
        getLogger().info("Disabled.");
    }

    private void setupEconomy(Logger log) {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            log.info("Vault not found — economy actions will be unavailable.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            log.warning("Vault found but no Economy provider is registered.");
            return;
        }
        economy = rsp.getProvider();
        log.info("Vault Economy hooked: " + economy.getName());
    }

    private void generateExampleMenus() {
        File menusFolder = new File(getDataFolder(), "menus");
        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }

        String[] examples = {"example_main.yml", "example_server_selector.yml"};
        for (String example : examples) {
            File dest = new File(menusFolder, example);
            if (!dest.exists()) {
                try (InputStream in = getResource("menus/" + example)) {
                    if (in != null) {
                        try (OutputStream out = Files.newOutputStream(dest.toPath())) {
                            in.transferTo(out);
                        }
                        getLogger().info("Generated example menu: " + example);
                    }
                } catch (IOException e) {
                    getLogger().warning("Could not write example menu " + example + ": " + e.getMessage());
                }
            }
        }
    }

    public static SwagMenus getInstance() { return instance; }
    public static Economy getEconomy() { return economy; }
    public static boolean isEconomyEnabled() { return economy != null; }
    public MenuManager getMenuManager() { return menuManager; }
    public ActionHandler getActionHandler() { return actionHandler; }
    public ChatInputManager getChatInputManager() { return chatInputManager; }
    public WebEditorServer getWebEditorServer() { return webEditorServer; }

    /**
     * Resolves the "[SwagMenus]" chat-message tag, honoring a central admin override from
     * SwagAPI's {@link IPrefixService} (per-plugin, else global, else the fallback) if the
     * service is available. Only the bracketed tag text itself is swappable — callers keep
     * their own surrounding color codes and message wording exactly as-is.
     *
     * @param fallbackTag this call site's own default tag text, e.g. {@code "[SwagMenus]"}
     */
    private static final java.util.regex.Pattern LEGACY_HEX =
            java.util.regex.Pattern.compile("(?i)[&§]#([0-9a-f]{6})");
    private static final java.util.regex.Pattern LEGACY_CODE =
            java.util.regex.Pattern.compile("(?i)[&§]([0-9a-fk-or])");
    private static final java.util.Map<Character, String> LEGACY_TAGS = java.util.Map.ofEntries(
            java.util.Map.entry('0', "<black>"), java.util.Map.entry('1', "<dark_blue>"), java.util.Map.entry('2', "<dark_green>"),
            java.util.Map.entry('3', "<dark_aqua>"), java.util.Map.entry('4', "<dark_red>"), java.util.Map.entry('5', "<dark_purple>"),
            java.util.Map.entry('6', "<gold>"), java.util.Map.entry('7', "<gray>"), java.util.Map.entry('8', "<dark_gray>"),
            java.util.Map.entry('9', "<blue>"), java.util.Map.entry('a', "<green>"), java.util.Map.entry('b', "<aqua>"),
            java.util.Map.entry('c', "<red>"), java.util.Map.entry('d', "<light_purple>"), java.util.Map.entry('e', "<yellow>"),
            java.util.Map.entry('f', "<white>"), java.util.Map.entry('k', "<obfuscated>"), java.util.Map.entry('l', "<bold>"),
            java.util.Map.entry('m', "<strikethrough>"), java.util.Map.entry('n', "<underlined>"), java.util.Map.entry('o', "<italic>"),
            java.util.Map.entry('r', "<reset>"));

    /**
     * Renders a stored prefix value from SwagAPI's IPrefixService — which may be MiniMessage
     * tags (admin-typed via the web panel), legacy {@code &}/{@code §} codes (this plugin's own
     * fallback constants), or a mix — into a legacy §-coded string safe for this plugin's
     * ChatColor/sendMessage(String) pipeline. Without this, a MiniMessage-tag override (e.g.
     * {@code <gold>[FleaMC] </gold>}) would show its literal tag text instead of rendering.
     */
    private static String toLegacyPrefix(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        var hex = LEGACY_HEX.matcher(raw);
        StringBuilder sb = new StringBuilder();
        while (hex.find()) hex.appendReplacement(sb, "<#" + hex.group(1) + ">");
        hex.appendTail(sb);
        raw = sb.toString();

        var code = LEGACY_CODE.matcher(raw);
        StringBuilder sb2 = new StringBuilder();
        while (code.find()) {
            String tag = LEGACY_TAGS.get(Character.toLowerCase(code.group(1).charAt(0)));
            code.appendReplacement(sb2, tag != null ? java.util.regex.Matcher.quoteReplacement(tag) : "");
        }
        code.appendTail(sb2);

        var component = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(sb2.toString());
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(component);
    }

    public String getPrefixTag(String fallbackTag) {
        return toLegacyPrefix((prefixService != null) ? prefixService.getPrefix("SwagMenus", fallbackTag) : fallbackTag);
    }
}
