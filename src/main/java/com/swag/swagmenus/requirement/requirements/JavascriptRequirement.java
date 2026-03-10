package com.swag.swagmenus.requirement.requirements;

import com.swag.swagmenus.requirement.Requirement;
import com.swag.swagmenus.util.PlaceholderUtil;
import org.bukkit.entity.Player;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates a simple expression after placeholder substitution and returns a boolean result.
 *
 * Replaces the previous Nashorn-based implementation, which was removed in Java 15.
 * Supported forms:
 *   - Comparison: {@code <value> <op> <value>} — operators: >=, <=, >, <, ==, !=
 *   - Plain boolean literal: {@code true} or {@code false}
 *
 * If both sides parse as numbers, numeric comparison is used; otherwise string comparison
 * (case-insensitive for == and !=).
 *
 * Named JavascriptRequirement for backwards config compatibility — use {@code type: javascript}
 * or {@code type: expression} in YAML.
 */
public class JavascriptRequirement implements Requirement {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    private static final Pattern COMPARISON_PATTERN =
            Pattern.compile("^(.+?)\\s*(>=|<=|!=|==|>|<)\\s*(.+)$");

    private final String expression;

    public JavascriptRequirement(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean isMet(Player player) {
        String resolved = PlaceholderUtil.apply(expression, player).trim();

        Matcher m = COMPARISON_PATTERN.matcher(resolved);
        if (m.matches()) {
            String left = m.group(1).trim();
            String op   = m.group(2).trim();
            String right = m.group(3).trim();
            return evaluateComparison(left, op, right);
        }

        if (resolved.equalsIgnoreCase("true"))  return true;
        if (resolved.equalsIgnoreCase("false")) return false;

        LOG.warning("Expression requirement could not be evaluated: '" + resolved
                + "' (original: '" + expression + "'). "
                + "Supported forms: '<value> <op> <value>' or 'true'/'false'.");
        return false;
    }

    private boolean evaluateComparison(String left, String op, String right) {
        try {
            double l = Double.parseDouble(left);
            double r = Double.parseDouble(right);
            return switch (op) {
                case ">="  -> l >= r;
                case "<="  -> l <= r;
                case ">"   -> l > r;
                case "<"   -> l < r;
                case "=="  -> l == r;
                case "!="  -> l != r;
                default    -> false;
            };
        } catch (NumberFormatException ignored) {
            // Not numeric — fall through to string comparison
        }

        // Ordering operators are not meaningful for non-numeric strings
        return switch (op) {
            case "==" -> left.equalsIgnoreCase(right);
            case "!=" -> !left.equalsIgnoreCase(right);
            default   -> {
                LOG.warning("Expression requirement: operator '" + op
                        + "' is not supported for non-numeric values (left='" + left
                        + "', right='" + right + "').");
                yield false;
            }
        };
    }

    @Override
    public String getType() {
        return "javascript";
    }

    public String getExpression() {
        return expression;
    }
}
