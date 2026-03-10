package com.swag.swagmenus.requirement;

import com.swag.swagmenus.requirement.requirements.*;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class RequirementFactory {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private RequirementFactory() {}

    /**
     * Parses a {@link RequirementSet} from a config section. The section may contain
     * a {@code requirements} sub-section and an optional {@code deny_commands} list.
     *
     * @param context a dotted path string used only for warning messages
     */
    public static RequirementSet parseRequirementSet(ConfigurationSection section, String context) {
        List<Requirement> requirements = new ArrayList<>();
        List<String> denyCommands = new ArrayList<>();

        if (section == null) {
            return new RequirementSet(requirements, denyCommands);
        }

        denyCommands.addAll(section.getStringList("deny_commands"));

        ConfigurationSection reqSection = section.getConfigurationSection("requirements");
        if (reqSection != null) {
            for (String key : reqSection.getKeys(false)) {
                ConfigurationSection reqEntry = reqSection.getConfigurationSection(key);
                if (reqEntry == null) continue;

                Requirement req = parseRequirement(reqEntry, context + ".requirements." + key);
                if (req != null) {
                    requirements.add(req);
                }
            }
        }

        return new RequirementSet(requirements, denyCommands);
    }

    public static Requirement parseRequirement(ConfigurationSection section, String context) {
        String type = section.getString("type", "").trim().toLowerCase();

        return switch (type) {
            case "has_permission" -> {
                String perm = section.getString("permission");
                if (perm == null || perm.isEmpty()) {
                    LOG.warning("Missing 'permission' in has_permission requirement at " + context);
                    yield null;
                }
                yield new PermissionRequirement(perm);
            }

            case "has_money" -> {
                double amount = section.getDouble("amount", 0);
                yield new MoneyRequirement(amount);
            }

            case "javascript", "expression" -> {
                String expr = section.getString("expression");
                if (expr == null || expr.isEmpty()) {
                    LOG.warning("Missing 'expression' in javascript/expression requirement at " + context);
                    yield null;
                }
                yield new JavascriptRequirement(expr);
            }

            case "string equals" -> {
                String input = section.getString("input", "");
                String value = section.getString("value", "");
                boolean cs = section.getBoolean("case_sensitive", false);
                yield new StringEqualsRequirement(input, value, cs);
            }

            case "string contains" -> {
                String input = section.getString("input", "");
                String value = section.getString("value", "");
                boolean cs = section.getBoolean("case_sensitive", false);
                yield new StringContainsRequirement(input, value, cs);
            }

            case "regex matches" -> {
                String input = section.getString("input", "");
                String regex = section.getString("regex", section.getString("value", ".*"));
                yield new RegexRequirement(input, regex);
            }

            case ">=", ">", "<=", "<", "==" -> {
                String input = section.getString("input", "");
                String value = section.getString("value", "0");
                try {
                    NumericRequirement.Operator op = NumericRequirement.Operator.fromString(type);
                    yield new NumericRequirement(input, op, value);
                } catch (IllegalArgumentException e) {
                    LOG.warning("Invalid numeric operator '" + type + "' at " + context);
                    yield null;
                }
            }

            default -> {
                LOG.warning("Unknown requirement type '" + type + "' at " + context);
                yield null;
            }
        };
    }
}
