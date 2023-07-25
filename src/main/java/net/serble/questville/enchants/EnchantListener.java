package net.serble.questville.enchants;

import net.serble.questville.Questville;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public abstract class EnchantListener implements Listener {
    public EnchantListener() {
        Bukkit.getPluginManager().registerEvents(this, Questville.getInstance());
    }

    public abstract CustomEnchantment getEnchant();
}
