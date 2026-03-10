package com.swag.swagmenus.requirement.requirements;

import com.swag.swagmenus.requirement.Requirement;
import com.swag.swagmenus.util.PlaceholderUtil;
import org.bukkit.entity.Player;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates a simple expression (after placeholder substitution) and returns a boolean result.
 *
 * <p>Replaces the previous Nashorn-based implementation, which stopped working on Java 15+.
 * This evaluator handles the most common use cases without any script engine dependency.
 *
 * <p>Supported expression forms:
 * <ul>
 *   <li>Comparison: {@code <value> <op> <value>} — operators: {@code >=}, {@code <=}, {@code >},
 *       {@code <}, {@code ==}, {@code !=}</li>
 *   <li>Plain boolean: {@code true} or {@code false}</li>
 * </ul>
 *
 * <p>If both sides of a comparison parse as numbers, numeric comparison is used.
 * Otherwise string comparison is used (case-insensitive for {@code ==} and {@code !=}).
 *
 * <p>The class is named {@code JavascriptRequirement} for backwards config compatibility.
 * Use {@code type: javascript} or {@code type: expression} in YAML.
 */
public class JavascriptRequirement implements Requirement {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    // Matches: <left> <op> <right>  — where op is one of >= <= > < == !=
    private static final Pattern COMPARISON_PATTERN =
            Pattern.compile("^(.+?)\\s*(>=|<=|!=|==|>|<)\\s*(.+)$");

    private final String expression;

    public JavascriptRequirement(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean isMet(Player player) {
        // Step 1: replace placeholders in the raw expression
        String resolved = PlaceholderUtil.apply(expression, player).trim();

        // Step 2: try to match a comparison expression
        Matcher m = COMPARISON_PATTERN.matcher(resolved);
        if (m.matches()) {
            String left = m.group(1).trim();
            String op   = m.group(2).trim();
            String right = m.group(3).trim();
            return evaluateComparison(left, op, right);
        }

        // Step 3: plain boolean literal
        if (resolved.equalsIgnoreCase("true"))  return true;
        if (resolved.equalsIgnoreCase("false")) return false;

        // Step 4: unknown form — log and return false
        LOG.warning("Expression requirement could not be evaluated: '" + resolved
                + "' (original: '" + expression + "'). "
                + "Supported forms: '<value> <op> <value>' or 'true'/'false'.");
        return false;
    }

    /**
     * Evaluates a comparison between two string values.
     * Attempts numeric comparison first; falls back to string comparison.
     */
    private boolean evaluateComparison(String left, String op, String right) {
        // Try numeric comparison
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

        // String comparison (== and != only; ordering operators not meaningful for strings)
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
