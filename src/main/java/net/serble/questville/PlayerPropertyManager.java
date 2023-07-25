package net.serble.questville;

import net.serble.questville.schemas.items.ItemAttributes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class PlayerPropertyManager implements Listener {
    private final HashMap<UUID, Integer> playerMana = new HashMap<>();

    public PlayerPropertyManager() {
        Bukkit.getPluginManager().registerEvents(this, Questville.getInstance());
    }

    public int getPlayerMaxHealth(Player p) {
        return sumAttributesOfAllPlayerItems(p).getHealthModifier() + 20;
    }

    public int getPlayerMaxMana(Player p) {
        return sumAttributesOfAllPlayerItems(p).getManaModifier() + 20;
    }

    public int getPlayerDefence(Player p) {
        return sumAttributesOfAllPlayerItems(p).getDefenceModifier();
    }

    public int getPlayerBaseDamage(Player p) {
        return sumAttributesOfAllPlayerItems(p).getDamageModifier() + 1;
    }

    public int getPlayerSpeed(Player p) {
        return sumAttributesOfAllPlayerItems(p).getSpeedModifier() + 100;
    }

    public int getPlayerMana(Player p) {
        return playerMana.getOrDefault(p.getUniqueId(), getPlayerMaxMana(p));
    }


    public void setPlayerMana(Player p, int mana) {
        playerMana.put(p.getUniqueId(), mana);
    }

    private ItemAttributes sumAttributesOfAllPlayerItems(Player p) {
        ItemAttributes attributes = new ItemAttributes();
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null) {
                continue;
            }
            attributes = attributes.add(Questville.getInstance().getItemManager().getItemAttributes(item));
        }

        for (ItemStack item : p.getInventory().getArmorContents()) {
            if (item == null) {
                continue;
            }
            attributes = attributes.add(Questville.getInstance().getItemManager().getItemAttributes(item));
        }

        attributes = attributes.add(Questville.getInstance().getItemManager().getItemAttributes(p.getInventory().getItemInOffHand()));
        return attributes;
    }

}
