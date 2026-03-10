package com.swag.swagmenus.requirement;

import com.swag.swagmenus.action.ActionHandler;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A collection of requirements evaluated with AND logic.
 * If all requirements are met, the set passes.
 * If any requirement fails, the deny_commands are executed.
 */
public class RequirementSet {

    private final List<Requirement> requirements;
    private final List<String> denyCommands;

    public RequirementSet(List<Requirement> requirements, List<String> denyCommands) {
        this.requirements = Collections.unmodifiableList(new ArrayList<>(requirements));
        this.denyCommands = Collections.unmodifiableList(new ArrayList<>(denyCommands));
    }

    /**
     * Tests whether all requirements in this set are met for the given player.
     */
    public boolean isMet(Player player) {
        for (Requirement requirement : requirements) {
            if (!requirement.isMet(player)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Executes the deny commands if requirements are not met.
     * Returns true if requirements were met, false otherwise.
     */
    public boolean checkAndDeny(Player player, ActionHandler actionHandler) {
        if (isMet(player)) {
            return true;
        }
        actionHandler.executeActions(player, denyCommands, null);
        return false;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }

    public List<String> getDenyCommands() {
        return denyCommands;
    }

    public boolean isEmpty() {
        return requirements.isEmpty();
    }
}
