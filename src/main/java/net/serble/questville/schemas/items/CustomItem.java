package net.serble.questville.schemas.items;

import net.serble.questville.Questville;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CustomItem {
    private final String id;
    private final ItemAttributes attributes;
    private final int damage;
    private final boolean hasEditions;
    private final String rarity;

    public CustomItem(String id, ItemAttributes attributes, int damage, boolean hasEditions, String rarity) {
        this.id = id;
        this.attributes = attributes;
        this.damage = damage;
        this.hasEditions = hasEditions;
        this.rarity = rarity;
    }

    public CustomItem(ConfigurationSection config) {
        this.id = config.getName();
        this.attributes = new ItemAttributes(Objects.requireNonNull(config.getConfigurationSection("attributes")));
        this.damage = config.getInt("damage");
        this.hasEditions = config.getBoolean("hasEditions");
        this.rarity = config.getString("rarity");
    }

    public ItemStack create(Player p) {
        return Questville.getInstance().getItemManager().createItem(p, id);
    }

    public ItemStack createDummy(Player p) {
        return Questville.getInstance().getItemManager().createItem(p, id);
    }

    public String getId() {
        return id;
    }

    public ItemAttributes getAttributes() {
        return attributes;
    }

    public int getDamage() {
        return damage;
    }

    public boolean hasEditions() {
        return hasEditions;
    }

    public ItemRarity getRarity() {
        return Questville.getInstance().getItemManager().getRarity(rarity);
    }
}
