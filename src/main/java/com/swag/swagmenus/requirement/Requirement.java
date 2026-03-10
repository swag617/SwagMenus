package com.swag.swagmenus.requirement;

import org.bukkit.entity.Player;

public interface Requirement {

    boolean isMet(Player player);

    /** A human-readable name for this requirement type, used in error messages. */
    String getType();
}
