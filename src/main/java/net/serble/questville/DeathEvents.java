package net.serble.questville;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvents implements Listener {

    public DeathEvents() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Questville.getInstance());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        if (!Questville.enabledIn(p)) {
            return;
        }
        e.setKeepInventory(true);
        e.setKeepLevel(true);
        e.setDroppedExp(0);

        int moneyLoss = Questville.getInstance().getYmlConfig().getConfiguration().getInt("death-actions.money-loss");
        int moneyPercentLoss = Questville.getInstance().getYmlConfig().getConfiguration().getInt("death-actions.money-percent-loss");
        int resourcePercentLoss = Questville.getInstance().getYmlConfig().getConfiguration().getInt("death-actions.resource-percent-loss");

        int playerWorth = Questville.getInstance().getItemManager().getInventoryBalance(p.getInventory());

        int moneyLossAmount = (int) (playerWorth * (moneyPercentLoss / 100.0)) + moneyLoss;
        if (moneyLossAmount > playerWorth) {
            moneyLossAmount = playerWorth;
        }
        if (moneyLossAmount != 0) {
            Questville.getInstance().getItemManager().chargePlayerAmount(p, moneyLossAmount);
            p.sendMessage(Utils.t("&cYou died and lost &6$" + moneyLossAmount + "&c."));
        }

        // TODO: Resource loss
    }

}
