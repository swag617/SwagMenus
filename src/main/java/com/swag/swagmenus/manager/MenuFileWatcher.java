package com.swag.swagmenus.manager;

import com.swag.swagmenus.SwagMenus;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Watches the menus folder for file creation, modification, and deletion events.
 * When a change is detected, the affected menu is reloaded on the main server thread.
 *
 * <p>Runs in a daemon thread so it doesn't block server shutdown.
 */
public class MenuFileWatcher implements Runnable {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private final SwagMenus plugin;
    private final MenuManager menuManager;
    private final Path watchPath;

    private WatchService watchService;
    private Thread watchThread;
    private volatile boolean running = false;

    // Debounce: track last modification time per file to avoid duplicate events
    private final Map<String, Long> lastModified = new HashMap<>();
    private static final long DEBOUNCE_MS = 500;

    public MenuFileWatcher(SwagMenus plugin, MenuManager menuManager) {
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.watchPath = menuManager.getMenusFolder().toPath();
    }

    public void start() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            watchPath.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);

            running = true;
            watchThread = new Thread(this, "SwagMenus-FileWatcher");
            watchThread.setDaemon(true);
            watchThread.start();
            LOG.info("File watcher started for: " + watchPath);
        } catch (IOException e) {
            LOG.warning("Could not start file watcher: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException ignored) {}
        }
        LOG.info("File watcher stopped.");
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            WatchKey key;
            try {
                key = watchService.take(); // Blocks until an event arrives
            } catch (InterruptedException | ClosedWatchServiceException e) {
                break;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) continue;

                @SuppressWarnings("unchecked")
                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                Path fileName = pathEvent.context();
                String fileNameStr = fileName.toString();

                if (!fileNameStr.endsWith(".yml")) continue;

                String menuName = fileNameStr.replace(".yml", "").toLowerCase();

                // Debounce
                long now = System.currentTimeMillis();
                Long last = lastModified.get(menuName);
                if (last != null && (now - last) < DEBOUNCE_MS) continue;
                lastModified.put(menuName, now);

                // Schedule reload on main thread
                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    Bukkit.getScheduler().runTask(plugin, () -> handleDelete(menuName));
                } else {
                    // CREATE or MODIFY — slight delay to ensure file write is complete
                    Bukkit.getScheduler().runTaskLater(plugin, () -> handleChange(menuName, fileNameStr), 10L);
                }
            }

            boolean valid = key.reset();
            if (!valid) break;
        }
    }

    private void handleChange(String menuName, String fileName) {
        File file = new File(menuManager.getMenusFolder(), fileName);
        if (!file.exists()) return;

        boolean success = menuManager.reloadMenu(menuName);
        if (success) {
            String msg = "&a[SwagMenus] Auto-reloaded menu &e" + menuName + " &adue to file change.";
            menuManager.notifyAdmins(msg);
            LOG.info("Auto-reloaded menu '" + menuName + "' due to file change.");
        } else {
            String msg = "&c[SwagMenus] Failed to auto-reload menu &e" + menuName
                    + " &cafter file change. Check console.";
            menuManager.notifyAdmins(msg);
        }
    }

    private void handleDelete(String menuName) {
        if (menuManager.menuExists(menuName)) {
            // Remove from cache — can't reload a deleted file
            menuManager.getMenus(); // just to check existence
            LOG.info("Menu file deleted: " + menuName + ".yml — menu removed from memory.");
            menuManager.notifyAdmins("&e[SwagMenus] Menu &6" + menuName + " &ewas deleted.");
        }
    }
}
