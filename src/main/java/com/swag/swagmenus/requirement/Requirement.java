package com.swag.swagmenus.requirement;

import org.bukkit.entity.Player;

/**
 * Represents a single requirement that a player must meet.
 */
public interface Requirement {

    /**
     * Evaluates whether the player meets this requirement.
     *
     * @param player the player to test
     * @return true if the requirement is met
     */
    boolean isMet(Player player);

    /**
     * A human-readable name for this requirement type, used in error messages.
     */
    String getType();
}
