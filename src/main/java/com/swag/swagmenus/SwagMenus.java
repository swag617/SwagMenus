package com.swag.swagmenus;

import com.swag.swagmenus.action.ActionHandler;
import com.swag.swagmenus.command.SwagMenusCommand;
import com.swag.swagmenus.listener.MenuListener;
import com.swag.swagmenus.manager.MenuFileWatcher;
import com.swag.swagmenus.manager.MenuManager;
import com.swag.swagmenus.util.ColorUtil;
import com.swag.swagmenus.web.WebEditorServer;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * SwagMenus — A user-friendly custom GUI/menu plugin for Paper 1.21.x.
 *
 * <p>Plugin load order:
 * <ol>
 *   <li>Create data folder and copy default config/examples</li>
 *   <li>Initialize ActionHandler</li>
 *   <li>Initialize MenuManager and load all menus</li>
 *   <li>Start file watcher for auto-reload</li>
 *   <li>Register commands and listeners</li>
 * </ol>
 */
public class SwagMenus extends JavaPlugin {

    private static SwagMenus instance;

    private MenuManager menuManager;
    private ActionHandler actionHandler;
    private MenuFileWatcher fileWatcher;
    private WebEditorServer webEditorServer;

    @Override
    public void onEnable() {
        instance = this;
        Logger log = getLogger();

        log.info("==============================================");
        log.info("  SwagMenus v" + getPluginMeta().getVersion());
        log.info("  Loading...");
        log.info("==============================================");

        // 1. Setup data folder and default config
        saveDefaultConfig();
        generateExampleMenus();

        // 2. Initialize core systems
        actionHandler = new ActionHandler(this);
        menuManager = new MenuManager(this);

        // 3. Load all menus
        menuManager.loadAllMenus();

        // 4. Start file watcher (if enabled in config)
        if (getConfig().getBoolean("auto_reload_on_change", true)) {
            fileWatcher = new MenuFileWatcher(this, menuManager);
            fileWatcher.start();
        }

        // 5. Start web editor HTTP server
        webEditorServer = new WebEditorServer(this);
        webEditorServer.start();

        // 6. Register listener
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // 7. Register /sm command
        PluginCommand smCommand = getCommand("sm");
        if (smCommand != null) {
            SwagMenusCommand commandExecutor = new SwagMenusCommand(this);
            smCommand.setExecutor(commandExecutor);
            smCommand.setTabCompleter(commandExecutor);
        } else {
            log.severe("Could not find 'sm' command in plugin.yml! Commands will not work.");
        }

        // Log integration status
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
            webEditorServer.stop();
        }
        if (fileWatcher != null) {
            fileWatcher.stop();
        }
        if (menuManager != null) {
            menuManager.shutdown();
        }
        getLogger().info("Disabled.");
    }

    // -------------------------------------------------------------------------
    // Example Menu Generation
    // -------------------------------------------------------------------------

    /**
     * Copies example menu YAML files from the plugin JAR to the menus folder
     * if they don't already exist. This only runs once on first install.
     */
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

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public static SwagMenus getInstance() { return instance; }
    public MenuManager getMenuManager() { return menuManager; }
    public ActionHandler getActionHandler() { return actionHandler; }
    public WebEditorServer getWebEditorServer() { return webEditorServer; }
}
