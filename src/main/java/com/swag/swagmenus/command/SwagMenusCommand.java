package com.swag.swagmenus.command;

import com.swag.swagmenus.SwagMenus;
import com.swag.swagmenus.menu.Menu;
import com.swag.swagmenus.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class SwagMenusCommand implements CommandExecutor, TabCompleter {

    private static final String PREFIX = "&8[&6SwagMenus&8] ";

    private final SwagMenus plugin;

    public SwagMenusCommand(SwagMenus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open" -> handleOpen(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender, args);
            case "execute" -> handleExecute(sender, args);
            case "info" -> handleInfo(sender, args);
            case "editor" -> handleEditor(sender);
            case "port" -> handlePort(sender, args);
            case "help" -> sendHelp(sender);
            default -> sender.sendMessage(msg("&cUnknown sub-command. Use &e/sm help&c for help."));
        }
        return true;
    }

    private void handleOpen(CommandSender sender, String[] args) {
        if (!sender.hasPermission("swagmenus.open")) {
            sender.sendMessage(msg("&cYou don't have permission to open menus."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(msg("&cUsage: &e/sm open <menu> [player]"));
            return;
        }

        String menuName = args[1].toLowerCase();
        if (!plugin.getMenuManager().menuExists(menuName)) {
            sender.sendMessage(msg("&cMenu '&e" + menuName + "&c' does not exist."));
            return;
        }

        Player target;
        if (args.length >= 3) {
            if (!sender.hasPermission("swagmenus.open.others")) {
                sender.sendMessage(msg("&cYou don't have permission to open menus for others."));
                return;
            }
            target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(msg("&cPlayer '&e" + args[2] + "&c' not found."));
                return;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(msg("&cConsole must specify a target player: &e/sm open <menu> <player>"));
                return;
            }
            target = player;
        }

        boolean success = plugin.getMenuManager().openMenu(target, menuName);
        if (success && sender != target) {
            sender.sendMessage(msg("&aOpened menu &e" + menuName + " &afor &e" + target.getName() + "&a."));
        }
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("swagmenus.list")) {
            sender.sendMessage(msg("&cYou don't have permission."));
            return;
        }

        Map<String, Menu> menus = plugin.getMenuManager().getMenus();
        if (menus.isEmpty()) {
            sender.sendMessage(msg("&eNo menus are currently loaded."));
            return;
        }

        sender.sendMessage(msg("&aLoaded menus (" + menus.size() + "):"));
        List<String> sorted = new ArrayList<>(menus.keySet());
        Collections.sort(sorted);
        for (String name : sorted) {
            Menu m = menus.get(name);
            String cmds = m.getOpenCommands().isEmpty()
                    ? "&7(no command)"
                    : "&7/" + String.join(", /", m.getOpenCommands());
            sender.sendMessage(ColorUtil.toComponent(
                    "  &e" + name + " &8— &f" + m.getItems().size()
                    + " items, " + m.getSize() + " slots &8| " + cmds));
        }
    }

    private void handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("swagmenus.reload")) {
            sender.sendMessage(msg("&cYou don't have permission."));
            return;
        }

        if (args.length >= 2) {
            String menuName = args[1].toLowerCase();
            boolean success = plugin.getMenuManager().reloadMenu(menuName);
            if (success) {
                sender.sendMessage(msg("&aReloaded menu &e" + menuName + "&a."));
            } else {
                sender.sendMessage(msg("&cFailed to reload menu '&e" + menuName
                        + "&c'. File may not exist or has errors."));
            }
        } else {
            plugin.getMenuManager().reloadAllMenus();
            int count = plugin.getMenuManager().getMenus().size();
            sender.sendMessage(msg("&aReloaded all menus. &e" + count + " &amenus loaded."));
        }
    }

    private void handleExecute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("swagmenus.execute")) {
            sender.sendMessage(msg("&cYou don't have permission."));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(msg("&cUsage: &e/sm execute <player> <action>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(msg("&cPlayer '&e" + args[1] + "&c' not found."));
            return;
        }

        String action = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        plugin.getActionHandler().executeActions(target, List.of(action), null);
        sender.sendMessage(msg("&aExecuted action on &e" + target.getName() + "&a."));
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("swagmenus.list")) {
            sender.sendMessage(msg("&cYou don't have permission."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(msg("&cUsage: &e/sm info <menu>"));
            return;
        }

        String menuName = args[1].toLowerCase();
        Menu menu = plugin.getMenuManager().getMenu(menuName);
        if (menu == null) {
            sender.sendMessage(msg("&cMenu '&e" + menuName + "&c' does not exist."));
            return;
        }

        sender.sendMessage(ColorUtil.toComponent(PREFIX + "&6Menu Info: &e" + menuName));
        sender.sendMessage(ColorUtil.toComponent("  &7Title: &f" + menu.getTitle()));
        sender.sendMessage(ColorUtil.toComponent("  &7Size: &f" + menu.getSize()
                + " slots (" + (menu.getSize() / 9) + " rows)"));
        sender.sendMessage(ColorUtil.toComponent("  &7Items: &f" + menu.getItems().size()));
        sender.sendMessage(ColorUtil.toComponent("  &7Open commands: &f"
                + (menu.getOpenCommands().isEmpty()
                        ? "none"
                        : "/" + String.join(", /", menu.getOpenCommands()))));
        sender.sendMessage(ColorUtil.toComponent("  &7Auto-refresh: &f"
                + (menu.hasAutoRefresh() ? menu.getUpdateInterval() + " ticks" : "disabled")));
        sender.sendMessage(ColorUtil.toComponent("  &7Fill item: &f" + (menu.hasFillItem() ? "yes" : "no")));
    }

    private void handleEditor(CommandSender sender) {
        if (!sender.hasPermission("swagmenus.admin")) {
            sender.sendMessage(msg("&cYou don't have permission to use the web editor."));
            return;
        }

        var webServer = plugin.getWebEditorServer();
        if (webServer == null || !webServer.isRunning()) {
            sender.sendMessage(msg("&cThe web editor server is not running. Check console for errors."));
            return;
        }

        int port = webServer.getPort();

        // Prefer server-ip from server.properties, fall back to local host address
        String serverIp = plugin.getServer().getIp();
        if (serverIp == null || serverIp.isBlank()) {
            try {
                serverIp = java.net.InetAddress.getLocalHost().getHostAddress();
            } catch (java.net.UnknownHostException e) {
                serverIp = "localhost";
            }
        }

        String url = "http://" + serverIp + ":" + port + "/editor";

        sender.sendMessage(msg("&aWeb editor is running! Click the link below to open it:"));

        Component link = Component.text("  " + url)
                .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW)
                .decorate(net.kyori.adventure.text.format.TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(url));
        sender.sendMessage(link);

        sender.sendMessage(msg("&7Password is set in &econfig.yml &7under &eweb_editor.password&7."));
    }

    private void handlePort(CommandSender sender, String[] args) {
        if (!sender.hasPermission("swagmenus.admin")) {
            sender.sendMessage(msg("&cYou don't have permission to change the web editor port."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(msg("&cUsage: &e/sm port <1024-65535>"));
            return;
        }

        int newPort;
        try {
            newPort = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(msg("&cInvalid port number: &e" + args[1]));
            return;
        }

        if (newPort < 1024 || newPort > 65535) {
            sender.sendMessage(msg("&cPort must be between &e1024 &cand &e65535&c."));
            return;
        }

        var webServer = plugin.getWebEditorServer();
        if (webServer == null) {
            sender.sendMessage(msg("&cWeb editor server is not initialized."));
            return;
        }

        try {
            webServer.restart(newPort);
            sender.sendMessage(msg("&aWeb editor restarted on port &e" + newPort + "&a."));
        } catch (Exception e) {
            sender.sendMessage(msg("&cFailed to restart web editor: &f" + e.getMessage()));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.toComponent("&8&m                              "));
        sender.sendMessage(ColorUtil.toComponent(PREFIX + "&6Commands"));
        sender.sendMessage(ColorUtil.toComponent("  &e/sm open <menu> [player] &7— Open a menu"));
        sender.sendMessage(ColorUtil.toComponent("  &e/sm list &7— List all loaded menus"));
        sender.sendMessage(ColorUtil.toComponent("  &e/sm reload [menu] &7— Reload all or one menu"));
        sender.sendMessage(ColorUtil.toComponent("  &e/sm info <menu> &7— Show menu details"));
        sender.sendMessage(ColorUtil.toComponent("  &e/sm execute <player> <action> &7— Execute an action"));
        sender.sendMessage(ColorUtil.toComponent("  &e/sm editor &7— Open the web editor (admin only)"));
        sender.sendMessage(ColorUtil.toComponent("  &e/sm port <number> &7— Change the web editor port"));
        sender.sendMessage(ColorUtil.toComponent("&8&m                              "));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of(
                    "open", "list", "reload", "execute", "info", "editor", "port", "help"));
            return filterStarting(subs, args[0]);
        }

        String sub = args[0].toLowerCase();

        if (args.length == 2) {
            return switch (sub) {
                case "open", "reload", "info" ->
                        filterStarting(new ArrayList<>(plugin.getMenuManager().getMenus().keySet()), args[1]);
                case "execute" ->
                        Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                                .collect(Collectors.toList());
                default -> new ArrayList<>();
            };
        }

        if (args.length == 3) {
            return switch (sub) {
                case "open" ->
                        Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                                .collect(Collectors.toList());
                case "execute" ->
                        filterStarting(List.of("[player]", "[console]", "[message]", "[close]",
                                "[open]", "[back]", "[sound]", "[title]", "[actionbar]",
                                "[nextpage]", "[prevpage]"), args[2]);
                default -> new ArrayList<>();
            };
        }

        return new ArrayList<>();
    }

    private Component msg(String message) {
        return ColorUtil.toComponent(PREFIX + message);
    }

    private List<String> filterStarting(List<String> options, String partial) {
        String lowerPartial = partial.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lowerPartial))
                .collect(Collectors.toList());
    }
}
