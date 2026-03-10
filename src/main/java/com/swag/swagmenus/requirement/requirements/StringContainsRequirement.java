package com.swag.swagmenus.requirement.requirements;

import com.swag.swagmenus.requirement.Requirement;
import com.swag.swagmenus.util.PlaceholderUtil;
import org.bukkit.entity.Player;

public class StringContainsRequirement implements Requirement {

    private final String input;
    private final String value;
    private final boolean caseSensitive;

    public StringContainsRequirement(String input, String value, boolean caseSensitive) {
        this.input = input;
        this.value = value;
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean isMet(Player player) {
        String resolved = PlaceholderUtil.apply(input, player);
        String val = PlaceholderUtil.apply(value, player);
        if (caseSensitive) {
            return resolved.contains(val);
        }
        return resolved.toLowerCase().contains(val.toLowerCase());
    }

    @Override
    public String getType() {
        return "string contains";
    }
}
