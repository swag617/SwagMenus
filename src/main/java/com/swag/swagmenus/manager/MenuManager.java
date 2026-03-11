package com.swag.swagmenus.manager;

import com.swag.swagmenus.SwagMenus;
import com.swag.swagmenus.command.MenuOpenCommand;
import com.swag.swagmenus.menu.Menu;
import com.swag.swagmenus.menu.MenuAnimationType;
import com.swag.swagmenus.menu.MenuItem;
import com.swag.swagmenus.menu.MenuSession;
import com.swag.swagmenus.requirement.RequirementFactory;
import com.swag.swagmenus.requirement.RequirementSet;
import com.swag.swagmenus.util.ColorUtil;
import com.swag.swagmenus.util.ItemBuilder;
import com.swag.swagmenus.util.PlaceholderUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

public class MenuManager {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private static final Map<String, Integer> SLOT_ALIASES = new HashMap<>();

    static {
        SLOT_ALIASES.put("top_left", 0);
        SLOT_ALIASES.put("top_middle", 4);
        SLOT_ALIASES.put("top_right", 8);
        SLOT_ALIASES.put("center", 22);
        SLOT_ALIASES.put("middle", 22);
        SLOT_ALIASES.put("bottom_left", 45);
        SLOT_ALIASES.put("bottom_middle", 49);
        SLOT_ALIASES.put("bottom_right", 53);
    }

    private final SwagMenus plugin;
    private final File menusFolder;

    private final Map<String, Menu> menus = new ConcurrentHashMap<>();
    private final Map<UUID, MenuSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, MenuOpenCommand> registeredCommands = new ConcurrentHashMap<>();

    private int refreshTaskId = -1;

    // Navigation stack per player — supports [back] action
    private final Map<UUID, Deque<String>> navStacks = new ConcurrentHashMap<>();

    public MenuManager(SwagMenus plugin) {
        this.plugin = plugin;
        this.menusFolder = new File(plugin.getDataFolder(), "menus");
    }

    public void loadAllMenus() {
        menus.clear();
        unregisterAllOpenCommands();

        if (!menusFolder.exists()) {
            menusFolder.mkdirs();
        }

        File[] files = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            LOG.info("No menu files found in " + menusFolder.getPath());
            return;
        }

        for (File file : files) {
            loadMenuFile(file);
        }

        LOG.info("Loaded " + menus.size() + " menu(s).");
        startRefreshTask();
    }

    public boolean loadMenuFile(File file) {
        String menuName = file.getName().replace(".yml", "").toLowerCase();
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Menu menu = parseMenu(menuName, config, file.getPath());
            menus.put(menuName, menu);

            List<String> commands = menu.getOpenCommands();
            for (String cmd : commands) {
                registerOpenCommand(cmd.toLowerCase(), menu);
            }

            LOG.info("Loaded menu '" + menuName + "'");
            return true;
        } catch (Exception e) {
            LOG.warning("Failed to load menu '" + menuName + "': " + e.getMessage());
            notifyAdmins("&cError loading menu &e" + menuName + "&c: &f" + e.getMessage());
            return false;
        }
    }

    public void reloadAllMenus() {
        for (MenuSession session : sessions.values()) {
            session.getPlayer().closeInventory();
        }
        sessions.clear();
        navStacks.clear();
        loadAllMenus();
    }

    public boolean reloadMenu(String menuName) {
        File file = new File(menusFolder, menuName + ".yml");
        if (!file.exists()) {
            return false;
        }

        sessions.entrySet().removeIf(entry -> {
            if (entry.getValue().getMenu().getName().equals(menuName)) {
                entry.getValue().getPlayer().closeInventory();
                return true;
            }
            return false;
        });

        if (menus.containsKey(menuName)) {
            Menu old = menus.get(menuName);
            for (String cmd : old.getOpenCommands()) {
                unregisterOpenCommand(cmd.toLowerCase());
            }
        }

        return loadMenuFile(file);
    }

    /**
     * Opens a menu for a player. If the player already has a menu open, pushes the current
     * menu onto the navigation stack so that a [back] action can return to it.
     */
    public boolean openMenu(Player player, String menuName) {
        Menu menu = menus.get(menuName.toLowerCase());
        if (menu == null) {
            player.sendMessage(ColorUtil.toComponent("&cMenu '&e" + menuName + "&c' not found."));
            return false;
        }

        MenuSession existing = sessions.get(player.getUniqueId());
        if (existing != null) {
            Deque<String> stack = navStacks.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>());
            stack.push(existing.getMenu().getName());
        }

        openMenuAnimated(player, menu);
        return true;
    }

    /**
     * Navigates back to the previous menu on the player's navigation stack.
     * Closes the inventory if the stack is empty.
     */
    public void navigateBack(Player player) {
        Deque<String> stack = navStacks.get(player.getUniqueId());
        if (stack == null || stack.isEmpty()) {
            player.closeInventory();
            return;
        }

        String previousMenu = stack.pop();
        Menu menu = menus.get(previousMenu);
        if (menu == null) {
            // Menu was deleted since it was pushed — close and clear stack
            LOG.warning("Back navigation target '" + previousMenu + "' no longer exists for player "
                    + player.getName() + ". Closing inventory.");
            navStacks.remove(player.getUniqueId());
            player.closeInventory();
            return;
        }

        openMenuAnimated(player, menu);
    }

    /**
     * Opens an inventory for a player and runs the opening animation if configured.
     * For NONE, falls back to instant populate. For all other types, the inventory is
     * opened empty and items are filled in frame-by-frame via scheduled tasks.
     */
    private void openMenuAnimated(Player player, Menu menu) {
        Component title = ColorUtil.toComponent(PlaceholderUtil.apply(menu.getTitle(), player));
        Inventory inv = Bukkit.createInventory(null, menu.getSize(), title);
        player.openInventory(inv);

        MenuSession session = new MenuSession(player, menu, inv);
        sessions.put(player.getUniqueId(), session);

        startLoreAnimTask(session, menu, inv);

        if (menu.getAnimationType() == MenuAnimationType.NONE) {
            populateInventory(player, menu, inv);
            return;
        }

        session.setAnimating(true);

        // Snapshot the items to display now so that placeholder values are captured
        // at open-time rather than at each frame's scheduled tick.
        Map<Integer, ItemStack> slotMap = buildSlotMap(player, menu, session);

        List<List<Integer>> groups = menu.getAnimationType().getSlotGroups(menu.getSize());
        int speed = menu.getAnimationSpeed();
        UUID uuid = player.getUniqueId();

        for (int i = 0; i < groups.size(); i++) {
            final List<Integer> group = groups.get(i);
            long delay = (long) i * speed;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                MenuSession current = sessions.get(uuid);
                // Session gone (player closed/quit) or swapped to a different menu — abort
                if (current == null || current.getInventory() != inv) return;

                for (int slot : group) {
                    inv.setItem(slot, slotMap.get(slot));
                }
            }, delay);
        }

        // Clear animating flag after the last frame
        long totalDelay = (long) (groups.size() - 1) * speed + 1L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MenuSession current = sessions.get(uuid);
            if (current != null && current.getInventory() == inv) {
                current.setAnimating(false);
            }
        }, totalDelay);
    }

    private void startLoreAnimTask(MenuSession session, Menu menu, Inventory inv) {
        List<MenuItem> animItems = menu.getItems().stream()
                .filter(MenuItem::hasLoreFrames)
                .toList();
        if (animItems.isEmpty()) return;

        // Use the minimum frame speed among all animated items as the tick period.
        int period = animItems.stream()
                .mapToInt(MenuItem::getLoreFrameSpeed)
                .min()
                .orElse(20);

        Player player = session.getPlayer();
        UUID uuid = player.getUniqueId();

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            MenuSession current = sessions.get(uuid);
            if (current == null || current.getInventory() != inv) return;

            Map<String, Integer> indices = current.getLoreFrameIndices();
            for (MenuItem item : animItems) {
                List<List<String>> frames = item.getLoreFrames();
                int frameIndex = indices.getOrDefault(item.getKey(), 0);
                // Only update on the item's own speed boundary
                // Track elapsed ticks per item via the index map value cycling
                // Simplest: advance frame each time this task fires (using period = min speed)
                // For per-item speed: store a tick counter in the session map keyed by key+"_tick"
                String tickKey = item.getKey() + "\0tick";
                int tick = indices.getOrDefault(tickKey, 0) + period;
                if (tick >= item.getLoreFrameSpeed()) {
                    tick = tick - item.getLoreFrameSpeed();
                    frameIndex = (frameIndex + 1) % frames.size();
                    indices.put(item.getKey(), frameIndex);
                }
                indices.put(tickKey, tick);

                List<String> frame = frames.get(frameIndex);
                List<Component> loreComponents = ColorUtil.toComponents(frame);
                for (int slot : item.getSlots()) {
                    if (slot < 0 || slot >= menu.getSize()) continue;
                    ItemStack stack = inv.getItem(slot);
                    if (stack == null || stack.getType() == Material.AIR) continue;
                    ItemStack copy = stack.clone();
                    var meta = copy.getItemMeta();
                    if (meta != null) {
                        meta.lore(loreComponents);
                        copy.setItemMeta(meta);
                    }
                    inv.setItem(slot, copy);
                }
            }
        }, period, period).getTaskId();

        session.setLoreAnimTaskId(taskId);
    }

    /**
     * Builds a slot -> ItemStack map for all visible items in the menu for the given player,
     * used to snapshot placeholder values at open time for the animation frames.
     */
    private Map<Integer, ItemStack> buildSlotMap(Player player, Menu menu, MenuSession session) {
        int currentPage = session.getCurrentPage();
        Map<Integer, ItemStack> map = new HashMap<>();

        Set<Integer> occupiedSlots = new HashSet<>();

        for (MenuItem menuItem : menu.getItems()) {
            if (menuItem.getPage() != 0 && menuItem.getPage() != currentPage) continue;

            if (menuItem.hasViewRequirement()) {
                boolean visible = menuItem.getViewRequirement().isMet(player);
                if (!visible) {
                    if (menuItem.getDenyItem() != null) {
                        ItemStack denyStack = buildItemStack(player, menuItem.getDenyItem());
                        for (int slot : menuItem.getDenyItem().getSlots()) {
                            if (slot >= 0 && slot < menu.getSize()) {
                                map.put(slot, denyStack);
                                occupiedSlots.add(slot);
                            }
                        }
                    }
                    continue;
                }
            }

            ItemStack itemStack = buildItemStack(player, menuItem);
            for (int slot : menuItem.getSlots()) {
                if (slot >= 0 && slot < menu.getSize()) {
                    map.put(slot, itemStack);
                    occupiedSlots.add(slot);
                }
            }
        }

        if (menu.hasFillItem()) {
            ItemStack filler = buildItemStack(player, menu.getFillItem());
            for (int i = 0; i < menu.getSize(); i++) {
                if (!occupiedSlots.contains(i)) {
                    map.put(i, filler);
                }
            }
        }

        return map;
    }

    public void refreshMenu(Player player) {
        MenuSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        populateInventory(player, session.getMenu(), session.getInventory());
        session.updateLastRefreshTime();
    }

    public void handleMenuClose(Player player) {
        MenuSession session = sessions.remove(player.getUniqueId());
        if (session != null && session.getLoreAnimTaskId() != -1) {
            Bukkit.getScheduler().cancelTask(session.getLoreAnimTaskId());
        }
        navStacks.remove(player.getUniqueId());
    }

    public MenuSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public Inventory buildInventory(Player player, Menu menu) {
        Component title = ColorUtil.toComponent(PlaceholderUtil.apply(menu.getTitle(), player));
        Inventory inv = Bukkit.createInventory(null, menu.getSize(), title);
        populateInventory(player, menu, inv);
        return inv;
    }

    /**
     * Fills an inventory with items for the given player and current page.
     * Items with page == 0 appear on all pages; items with a specific page number only
     * appear when the session is on that page.
     */
    public void populateInventory(Player player, Menu menu, Inventory inv) {
        MenuSession session = sessions.get(player.getUniqueId());
        int currentPage = (session != null) ? session.getCurrentPage() : 1;

        inv.clear();

        Set<Integer> occupiedSlots = new HashSet<>();

        for (MenuItem menuItem : menu.getItems()) {
            if (menuItem.getPage() != 0 && menuItem.getPage() != currentPage) {
                continue;
            }

            if (menuItem.isPlayerList()) {
                populatePlayerList(player, menu, inv, menuItem, occupiedSlots);
                continue;
            }

            if (menuItem.hasViewRequirement()) {
                boolean visible = menuItem.getViewRequirement().isMet(player);
                if (!visible) {
                    if (menuItem.getDenyItem() != null) {
                        ItemStack denyStack = buildItemStack(player, menuItem.getDenyItem());
                        for (int slot : menuItem.getDenyItem().getSlots()) {
                            if (slot >= 0 && slot < menu.getSize()) {
                                inv.setItem(slot, denyStack);
                                occupiedSlots.add(slot);
                            }
                        }
                    }
                    continue;
                }
            }

            ItemStack itemStack = buildItemStack(player, menuItem);
            for (int slot : menuItem.getSlots()) {
                if (slot >= 0 && slot < menu.getSize()) {
                    inv.setItem(slot, itemStack);
                    occupiedSlots.add(slot);
                }
            }
        }

        if (menu.hasFillItem()) {
            ItemStack filler = buildItemStack(player, menu.getFillItem());
            for (int i = 0; i < menu.getSize(); i++) {
                if (!occupiedSlots.contains(i)) {
                    inv.setItem(i, filler);
                }
            }
        }
    }

    private void populatePlayerList(Player viewer, Menu menu, Inventory inv, MenuItem template,
                                    Set<Integer> occupiedSlots) {
        List<? extends Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (int i = 0; i < online.size(); i++) {
            int slot = template.getSlotStart() + i;
            if (slot < 0 || slot >= menu.getSize()) break;

            Player target = online.get(i);
            String name = target.getName();

            ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD);
            if (!template.getDisplayName().isEmpty()) {
                builder.name(template.getDisplayName().replace("{player_name}", name));
            }
            if (!template.getLore().isEmpty()) {
                List<String> lore = template.getLore().stream()
                        .map(l -> l.replace("{player_name}", name))
                        .collect(Collectors.toList());
                builder.loreStrings(lore);
            }
            builder.skullOwner(name)
                   .amount(template.getAmount())
                   .glow(template.isGlow())
                   .hideFlags(template.isHideFlags());

            inv.setItem(slot, builder.build());
            occupiedSlots.add(slot);
        }
    }

    public ItemStack buildItemStack(Player player, MenuItem menuItem) {
        ItemBuilder builder = new ItemBuilder(menuItem.getMaterial());

        if (menuItem.getDisplayName() != null && !menuItem.getDisplayName().isEmpty()) {
            String name = PlaceholderUtil.apply(menuItem.getDisplayName(), player);
            builder.name(name);
        }

        if (!menuItem.getLore().isEmpty()) {
            List<String> lore = PlaceholderUtil.apply(menuItem.getLore(), player);
            builder.loreStrings(lore);
        }

        builder.amount(menuItem.getAmount())
               .glow(menuItem.isGlow())
               .customModelData(menuItem.getCustomModelData())
               .hideFlags(menuItem.isHideFlags());

        if (menuItem.getMaterial() == Material.PLAYER_HEAD) {
            if (menuItem.getSkullTexture() != null && !menuItem.getSkullTexture().isEmpty()) {
                builder.skullTexture(menuItem.getSkullTexture());
            } else if (menuItem.getSkullOwner() != null && !menuItem.getSkullOwner().isEmpty()) {
                String owner = PlaceholderUtil.apply(menuItem.getSkullOwner(), player);
                builder.skullOwner(owner);
            }
        }

        return builder.build();
    }

    private Menu parseMenu(String menuName, YamlConfiguration config, String filePath) {
        Menu.Builder menuBuilder = Menu.builder(menuName);

        menuBuilder.title(config.getString("menu_title", "&8Menu"));

        int size = config.getInt("menu_size", 54);
        if (size < 9 || size > 54 || size % 9 != 0) {
            throw new IllegalArgumentException(
                    "Invalid menu_size " + size + " (must be 9/18/27/36/45/54)");
        }
        menuBuilder.size(size);

        List<String> openCmds = new ArrayList<>();
        String singleCmd = config.getString("open_command");
        if (singleCmd != null && !singleCmd.isEmpty()) {
            openCmds.add(singleCmd);
        }
        openCmds.addAll(config.getStringList("open_commands"));
        openCmds = new ArrayList<>(new LinkedHashSet<>(openCmds));
        menuBuilder.openCommands(openCmds);

        menuBuilder.updateInterval(config.getInt("update_interval", 0));

        menuBuilder.animationType(MenuAnimationType.fromString(config.getString("animation", "NONE")));
        menuBuilder.animationSpeed(config.getInt("animation_speed", 1));

        ConfigurationSection fillSection = config.getConfigurationSection("fill_item");
        if (fillSection != null) {
            MenuItem fillItem = parseMenuItem("fill_item", fillSection, size, filePath);
            if (fillItem != null) {
                menuBuilder.fillItem(fillItem);
            }
        }

        List<MenuItem> items = new ArrayList<>();
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection == null) continue;
                MenuItem item = parseMenuItem(key, itemSection, size, filePath);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        menuBuilder.items(items);

        return menuBuilder.build();
    }

    private MenuItem parseMenuItem(String key, ConfigurationSection section, int menuSize, String filePath) {
        String materialStr = section.getString("material", "STONE").toUpperCase();
        Material material;
        try {
            material = Material.valueOf(materialStr);
        } catch (IllegalArgumentException e) {
            LOG.warning("Unknown material '" + materialStr + "' for item '" + key
                    + "' in " + filePath + ". Using STONE.");
            material = Material.STONE;
        }

        // Supports both 'slot' (single int) and 'slots' (list)
        List<Integer> slots = new ArrayList<>();
        if (section.contains("slots")) {
            List<?> rawSlots = section.getList("slots");
            if (rawSlots != null) {
                for (Object raw : rawSlots) {
                    Integer resolved = resolveSlot(raw.toString().trim(), menuSize, key, filePath);
                    if (resolved != null) slots.add(resolved);
                }
            }
        } else if (section.contains("slot")) {
            Integer resolved = resolveSlot(section.getString("slot", "0"), menuSize, key, filePath);
            if (resolved != null) slots.add(resolved);
        }

        String itemType = section.getString("type", null);
        if (slots.isEmpty() && !key.equals("fill_item") && !"player_list".equals(itemType)) {
            LOG.warning("Item '" + key + "' in " + filePath + " has no valid slots defined.");
        }

        MenuItem.Builder builder = MenuItem.builder(key, material)
                .slots(slots)
                .displayName(section.getString("display_name", ""))
                .lore(section.getStringList("lore"))
                .amount(Math.max(1, Math.min(64, section.getInt("amount", 1))))
                .glow(section.getBoolean("glow", false))
                .customModelData(section.getInt("custom_model_data", 0))
                .hideFlags(section.getBoolean("hide_flags", false))
                .skullOwner(section.getString("skull_owner", null))
                .skullTexture(section.getString("skull_texture", null))
                .leftClickCommands(section.getStringList("left_click_commands"))
                .rightClickCommands(section.getStringList("right_click_commands"))
                .shiftLeftClickCommands(section.getStringList("shift_left_click_commands"))
                .shiftRightClickCommands(section.getStringList("shift_right_click_commands"))
                .middleClickCommands(section.getStringList("middle_click_commands"))
                .page(section.getInt("page", 0))
                .onChatInput(section.getStringList("on_chat_input"))
                .type(section.getString("type", null))
                .slotStart(section.getInt("slot_start", 0))
                .loreFrameSpeed(section.getInt("lore_frame_speed", 20));

        // Parse lore_frames: list of lists
        if (section.isList("lore_frames")) {
            List<?> raw = section.getList("lore_frames");
            if (raw != null) {
                List<List<String>> frames = new ArrayList<>();
                for (Object entry : raw) {
                    if (entry instanceof List<?> frameList) {
                        List<String> frame = new ArrayList<>();
                        for (Object line : frameList) {
                            frame.add(line.toString());
                        }
                        frames.add(frame);
                    } else {
                        frames.add(List.of(entry.toString()));
                    }
                }
                if (!frames.isEmpty()) {
                    builder.loreFrames(frames);
                }
            }
        }

        ConfigurationSection viewReq = section.getConfigurationSection("view_requirement");
        if (viewReq != null) {
            builder.viewRequirement(RequirementFactory.parseRequirementSet(viewReq,
                    filePath + ".items." + key + ".view_requirement"));
        }

        ConfigurationSection clickReq = section.getConfigurationSection("click_requirement");
        if (clickReq != null) {
            ConfigurationSection leftReq = clickReq.getConfigurationSection("left_click_requirements");
            if (leftReq != null) {
                builder.leftClickRequirement(RequirementFactory.parseRequirementSet(leftReq,
                        filePath + ".items." + key + ".click_requirement.left_click_requirements"));
            } else {
                // Fallback: treat click_requirement itself as the left click requirement
                builder.leftClickRequirement(RequirementFactory.parseRequirementSet(clickReq,
                        filePath + ".items." + key + ".click_requirement"));
            }
            ConfigurationSection rightReq = clickReq.getConfigurationSection("right_click_requirements");
            if (rightReq != null) {
                builder.rightClickRequirement(RequirementFactory.parseRequirementSet(rightReq,
                        filePath + ".items." + key + ".click_requirement.right_click_requirements"));
            }
        }

        return builder.build();
    }

    private Integer resolveSlot(String raw, int menuSize, String itemKey, String filePath) {
        String lower = raw.toLowerCase();
        if (SLOT_ALIASES.containsKey(lower)) {
            int slot = SLOT_ALIASES.get(lower);
            if (slot < menuSize) return slot;
            LOG.warning("Slot alias '" + raw + "' (" + slot + ") is out of range for size "
                    + menuSize + " in item '" + itemKey + "' in " + filePath);
            return null;
        }
        try {
            int slot = Integer.parseInt(raw);
            if (slot < 0 || slot >= menuSize) {
                LOG.warning("Slot " + slot + " out of range [0," + (menuSize - 1)
                        + "] in item '" + itemKey + "' in " + filePath);
                return null;
            }
            return slot;
        } catch (NumberFormatException e) {
            LOG.warning("Invalid slot '" + raw + "' for item '" + itemKey + "' in " + filePath);
            return null;
        }
    }

    /**
     * Registers a dynamic open command using reflection on PluginCommand's package-private
     * constructor, since Paper provides no public API for runtime command registration.
     */
    private void registerOpenCommand(String commandName, Menu menu) {
        MenuOpenCommand executor = new MenuOpenCommand(plugin, menu.getName());

        org.bukkit.command.CommandMap commandMap = Bukkit.getCommandMap();

        PluginCommand existing = plugin.getServer().getPluginCommand(commandName);
        if (existing != null) {
            LOG.warning("Command /" + commandName + " is already registered. Skipping.");
            return;
        }

        try {
            java.lang.reflect.Constructor<PluginCommand> constructor =
                    PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            constructor.setAccessible(true);
            PluginCommand cmd = constructor.newInstance(commandName, plugin);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
            cmd.setDescription("Open the " + menu.getName() + " menu");
            cmd.setPermission("swagmenus.open");
            commandMap.register(plugin.getName().toLowerCase(), cmd);
            registeredCommands.put(commandName, executor);
        } catch (Exception e) {
            LOG.warning("Failed to register command /" + commandName + ": " + e.getMessage());
        }
    }

    /**
     * Unregisters a dynamic command by removing it from CommandMap's knownCommands via reflection.
     */
    @SuppressWarnings("unchecked")
    private void unregisterOpenCommand(String commandName) {
        org.bukkit.command.CommandMap commandMap = Bukkit.getCommandMap();
        Map<String, Command> knownCommands;
        try {
            java.lang.reflect.Field field = commandMap.getClass().getDeclaredField("knownCommands");
            field.setAccessible(true);
            knownCommands = (Map<String, Command>) field.get(commandMap);
            knownCommands.remove(commandName);
            knownCommands.remove(plugin.getName().toLowerCase() + ":" + commandName);
        } catch (Exception e) {
            LOG.warning("Failed to unregister command /" + commandName + ": " + e.getMessage());
        }
        registeredCommands.remove(commandName);
    }

    private void unregisterAllOpenCommands() {
        new ArrayList<>(registeredCommands.keySet()).forEach(this::unregisterOpenCommand);
        registeredCommands.clear();
    }

    private void startRefreshTask() {
        if (refreshTaskId != -1) {
            Bukkit.getScheduler().cancelTask(refreshTaskId);
        }

        refreshTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (MenuSession session : sessions.values()) {
                Menu menu = session.getMenu();
                if (!menu.hasAutoRefresh()) continue;

                long intervalMs = (menu.getUpdateInterval() * 50L); // ticks to ms
                long elapsed = System.currentTimeMillis() - session.getLastRefreshTime();
                if (elapsed >= intervalMs) {
                    Player player = session.getPlayer();
                    if (player.isOnline()) {
                        populateInventory(player, menu, session.getInventory());
                        session.updateLastRefreshTime();
                    }
                }
            }
        }, 1L, 1L).getTaskId();
    }

    public void shutdown() {
        if (refreshTaskId != -1) {
            Bukkit.getScheduler().cancelTask(refreshTaskId);
            refreshTaskId = -1;
        }
        unregisterAllOpenCommands();
        sessions.clear();
        navStacks.clear();
    }

    public Map<String, Menu> getMenus() { return Collections.unmodifiableMap(menus); }

    public Menu getMenu(String name) { return menus.get(name.toLowerCase()); }

    public boolean menuExists(String name) { return menus.containsKey(name.toLowerCase()); }

    public void removeMenu(String name) { menus.remove(name.toLowerCase()); }

    public File getMenusFolder() { return menusFolder; }

    public void notifyAdmins(String message) {
        Component component = ColorUtil.toComponent(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("swagmenus.admin")) {
                player.sendMessage(component);
            }
        }
    }
}
