package com.swag.swagmenus.command;

import com.swag.swagmenus.SwagMenus;
import com.swag.swagmenus.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Executor for a menu's custom open command (e.g. /mymenu or /server-selector).
 * Each instance is bound to a specific menu name.
 */
public class MenuOpenCommand implements CommandExecutor, TabCompleter {

    private final SwagMenus plugin;
    private final String menuName;

    public MenuOpenCommand(SwagMenus plugin, String menuName) {
        this.plugin = plugin;
        this.menuName = menuName;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ColorUtil.toComponent("&cThis command can only be used by players."));
                return true;
            }
            if (!player.hasPermission("swagmenus.open")) {
                player.sendMessage(ColorUtil.toComponent("&cYou don't have permission to open menus."));
                return true;
            }
            plugin.getMenuManager().openMenu(player, menuName);
        } else {
            if (!sender.hasPermission("swagmenus.open.others")) {
                sender.sendMessage(ColorUtil.toComponent(
                        "&cYou don't have permission to open menus for other players."));
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ColorUtil.toComponent("&cPlayer '&e" + args[0] + "&c' not found."));
                return true;
            }
            plugin.getMenuManager().openMenu(target, menuName);
            sender.sendMessage(ColorUtil.toComponent(
                    "&aOpened menu &e" + menuName + " &afor &e" + target.getName() + "&a."));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("swagmenus.open.others")) {
            String partial = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
