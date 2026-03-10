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

        saveDefaultConfig();
        generateExampleMenus();

        actionHandler = new ActionHandler(this);
        menuManager = new MenuManager(this);
        menuManager.loadAllMenus();

        if (getConfig().getBoolean("auto_reload_on_change", true)) {
            fileWatcher = new MenuFileWatcher(this, menuManager);
            fileWatcher.start();
        }

        webEditorServer = new WebEditorServer(this);
        webEditorServer.start();

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
    public MenuManager getMenuManager() { return menuManager; }
    public ActionHandler getActionHandler() { return actionHandler; }
    public WebEditorServer getWebEditorServer() { return webEditorServer; }
}
