package com.swag.swagmenus.requirement.requirements;

import com.swag.swagmenus.requirement.Requirement;
import com.swag.swagmenus.util.PlaceholderUtil;
import org.bukkit.entity.Player;

/**
 * Passes if the first placeholder/value equals the second (case-insensitive).
 */
public class StringEqualsRequirement implements Requirement {

    private final String input;
    private final String expected;
    private final boolean caseSensitive;

    public StringEqualsRequirement(String input, String expected, boolean caseSensitive) {
        this.input = input;
        this.expected = expected;
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean isMet(Player player) {
        String resolved = PlaceholderUtil.apply(input, player);
        String exp = PlaceholderUtil.apply(expected, player);
        return caseSensitive ? resolved.equals(exp) : resolved.equalsIgnoreCase(exp);
    }

    @Override
    public String getType() {
        return "string equals";
    }
}
