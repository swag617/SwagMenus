package com.swag.swagmenus.requirement.requirements;

import com.swag.swagmenus.SwagMenus;
import com.swag.swagmenus.requirement.Requirement;
import org.bukkit.entity.Player;

/**
 * Passes if the player's Vault balance is >= the required amount.
 *
 * Reads the balance directly from {@link SwagMenus#getEconomy()} — the same Vault hook used by
 * the {@code [money_give]}/{@code [money_take]} actions — rather than round-tripping through the
 * PlaceholderAPI {@code %vault_balance%} placeholder. The old PAPI-based approach silently
 * evaluated to "false" on any server that has Vault + an economy plugin but not PlaceholderAPI
 * installed (PAPI is a soft dependency, not required), even though Vault itself was available.
 */
public class MoneyRequirement implements Requirement {

    private final double amount;

    public MoneyRequirement(double amount) {
        this.amount = amount;
    }

    @Override
    public boolean isMet(Player player) {
        if (player == null) return false;
        if (!SwagMenus.isEconomyEnabled()) return false;
        double balance = SwagMenus.getEconomy().getBalance(player);
        return balance >= amount;
    }

    @Override
    public String getType() {
        return "has_money";
    }

    public double getAmount() {
        return amount;
    }
}
