package com.swag.swagmenus.requirement.requirements;

import com.swag.swagmenus.requirement.Requirement;
import com.swag.swagmenus.util.PlaceholderUtil;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.logging.Logger;

/**
 * Passes if the resolved input matches the given regular expression.
 */
public class RegexRequirement implements Requirement {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private final String input;
    private final Pattern pattern;

    public RegexRequirement(String input, String regex) {
        this.input = input;
        Pattern compiled;
        try {
            compiled = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            LOG.warning("Invalid regex in requirement: " + regex + " — " + e.getMessage());
            compiled = Pattern.compile("(?!)"); // never matches
        }
        this.pattern = compiled;
    }

    @Override
    public boolean isMet(Player player) {
        String resolved = PlaceholderUtil.apply(input, player);
        return pattern.matcher(resolved).matches();
    }

    @Override
    public String getType() {
        return "regex matches";
    }
}
