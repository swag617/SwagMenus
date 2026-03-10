package com.swag.swagmenus.requirement.requirements;

import com.swag.swagmenus.requirement.Requirement;
import org.bukkit.entity.Player;

public class PermissionRequirement implements Requirement {

    private final String permission;

    public PermissionRequirement(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean isMet(Player player) {
        return player != null && player.hasPermission(permission);
    }

    @Override
    public String getType() {
        return "has_permission";
    }

    public String getPermission() {
        return permission;
    }
}
