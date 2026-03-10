package com.swag.swagmenus.requirement.requirements;

import com.swag.swagmenus.requirement.Requirement;
import com.swag.swagmenus.util.PlaceholderUtil;
import org.bukkit.entity.Player;

/**
 * Passes if the player's balance (via %vault_balance% placeholder) is >= the required amount.
 * Requires PlaceholderAPI + Vault to be installed.
 */
public class MoneyRequirement implements Requirement {

    private final double amount;

    public MoneyRequirement(double amount) {
        this.amount = amount;
    }

    @Override
    public boolean isMet(Player player) {
        if (player == null) return false;
        String balanceStr = PlaceholderUtil.apply("%vault_balance%", player);
        // If PAPI/Vault not available, the placeholder won't be replaced
        if (balanceStr.contains("%")) return false;
        try {
            double balance = Double.parseDouble(balanceStr.replace(",", ""));
            return balance >= amount;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String getType() {
        return "has_money";
    }

    public double getAmount() {
        return amount;
    }
}
