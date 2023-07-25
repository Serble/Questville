package net.serble.questville.enchants;

import net.serble.questville.NbtHandler;
import net.serble.questville.Questville;
import net.serble.questville.enchants.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CustomEnchants implements Listener {

    public CustomEnchants() {
        // register enchants
        new FireAspect();
        new LightningAspect();

        Bukkit.getPluginManager().registerEvents(this, Questville.getInstance());
    }

    public void enchantItem(ItemStack item, CustomEnchantment enchantment, int level) {
        if (item.getItemMeta() == null) {
            return;
        }

        if (hasEnchantment(item, enchantment)) {
            removeEnchantment(item, enchantment);
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String enchantments = NbtHandler.getTag(container, "enchantments", PersistentDataType.STRING);
        if (enchantments == null) {
            enchantments = enchantment.toString() + ":" + level;
        } else {
            enchantments += "," + enchantment.toString() + ":" + level;
        }
        NbtHandler.setTag(container, "enchantments", PersistentDataType.STRING, enchantments);
        item.setItemMeta(meta);
        Questville.getInstance().getItemManager().updateLore(item);
        Bukkit.getLogger().info("Enchanted item: " + item);
    }

    public int getEnchantmentLevel(ItemStack item, CustomEnchantment enchantment) {
        HashMap<CustomEnchantment, Integer> enchantments = getEnchantments(item);
        if (enchantments == null) {
            return 0;
        }
        return enchantments.getOrDefault(enchantment, 0);
    }

    private boolean hasEnchantment(ItemStack item, CustomEnchantment enchantment) {
        HashMap<CustomEnchantment, Integer> enchantments = getEnchantments(item);
        if (enchantments == null) {
            return false;
        }
        return enchantments.containsKey(enchantment);
    }

    public void removeEnchantment(ItemStack item, CustomEnchantment enchantment) {
        if (item.getItemMeta() == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String enchantments = NbtHandler.getTag(container, "enchantments", PersistentDataType.STRING);
        if (enchantments == null) {
            return;
        }
        List<String> enchantmentList = Arrays.stream(enchantments.split(","))
                .filter(s -> !s.startsWith(enchantment.toString()))
                .collect(Collectors.toList());
        enchantments = String.join(",", enchantmentList);
        NbtHandler.setTag(container, "enchantments", PersistentDataType.STRING, enchantments);
        item.setItemMeta(meta);
        Questville.getInstance().getItemManager().updateLore(item);
    }

    public HashMap<CustomEnchantment, Integer> getEnchantments(ItemStack item) {
        if (item.getItemMeta() == null) {
            return new HashMap<>();
        }
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (!NbtHandler.hasTag(container, "enchantments", PersistentDataType.STRING)) {
            return new HashMap<>();
        }

        String enchantments = NbtHandler.getTag(container, "enchantments", PersistentDataType.STRING);
        return Arrays.stream(enchantments.split(","))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(
                        s -> CustomEnchantment.valueOf(s[0]),
                        s -> Integer.parseInt(s[1]),
                        (a, b) -> b,
                        HashMap::new
                ));
    }

}
