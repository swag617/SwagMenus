package com.swag.swagmenus.requirement.requirements;

import com.swag.swagmenus.requirement.Requirement;
import com.swag.swagmenus.util.PlaceholderUtil;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class NumericRequirement implements Requirement {

    private static final Logger LOG = Logger.getLogger("SwagMenus");

    public enum Operator {
        GREATER_EQUAL(">="),
        GREATER(">"),
        LESS_EQUAL("<="),
        LESS("<"),
        EQUAL("=="),
        NOT_EQUAL("!=");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public static Operator fromString(String s) {
            for (Operator op : values()) {
                if (op.symbol.equals(s.trim())) return op;
            }
            throw new IllegalArgumentException("Unknown numeric operator: " + s);
        }

        public String getSymbol() {
            return symbol;
        }
    }

    private final String input;
    private final Operator operator;
    private final String expected;

    public NumericRequirement(String input, Operator operator, String expected) {
        this.input = input;
        this.operator = operator;
        this.expected = expected;
    }

    @Override
    public boolean isMet(Player player) {
        String resolvedInput = PlaceholderUtil.apply(input, player).trim().replace(",", "");
        String resolvedExpected = PlaceholderUtil.apply(expected, player).trim().replace(",", "");

        double inputVal;
        double expectedVal;
        try {
            inputVal = Double.parseDouble(resolvedInput);
            expectedVal = Double.parseDouble(resolvedExpected);
        } catch (NumberFormatException e) {
            LOG.warning("Numeric requirement could not parse values: '"
                    + resolvedInput + "' " + operator.getSymbol() + " '" + resolvedExpected + "'");
            return false;
        }

        return switch (operator) {
            case GREATER_EQUAL -> inputVal >= expectedVal;
            case GREATER       -> inputVal > expectedVal;
            case LESS_EQUAL    -> inputVal <= expectedVal;
            case LESS          -> inputVal < expectedVal;
            case EQUAL         -> inputVal == expectedVal;
            case NOT_EQUAL     -> inputVal != expectedVal;
        };
    }

    @Override
    public String getType() {
        return operator.getSymbol();
    }
}
