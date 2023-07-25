package net.serble.questville;

import net.serble.questville.enchants.CustomEnchantment;
import net.serble.questville.enchants.EnchantDisplayNames;
import net.serble.questville.schemas.items.CustomItem;
import net.serble.questville.schemas.items.ItemAttributes;
import net.serble.questville.schemas.items.ItemRarity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.*;

public class ItemManager implements Listener {
    private ItemRarity[] itemRarities;
    private HashMap<String, ConfigurationSection> resourceConfigs;
    private HashMap<String, ItemStack> resources;
    private HashMap<String, ConfigurationSection> itemConfigs;
    private HashMap<String, ItemStack> items;
    private YmlConfig editionTracker;
    private HashMap<String, ItemStack> currencyItems;
    private HashMap<String, Integer> currencyValues;

    public ItemManager() {
        Questville.getInstance().saveResource("items.yml", false);

        YmlConfig itemsConfig;
        try {
            itemsConfig = new YmlConfig("items.yml");
        } catch (Exception e) {
            Questville.getInstance().disableDueToError(e);
            return;
        }

        // Load rarity
        {
            ConfigurationSection raritySection = itemsConfig.getConfiguration().getConfigurationSection("rarity");
            assert raritySection != null;
            Set<String> rarities = raritySection.getKeys(false);

            List<ItemRarity> rarityList = new ArrayList<>();
            for (String rarity : rarities) {
                ConfigurationSection rarityConfig = raritySection.getConfigurationSection(rarity);
                assert rarityConfig != null;
                String name = rarityConfig.getName();
                String display = rarityConfig.getString("name");
                ItemRarity rarityObj = new ItemRarity(name, Utils.t(display));
                rarityList.add(rarityObj);
            }
            itemRarities = rarityList.toArray(new ItemRarity[0]);
        }

        // Load resources
        {
            resources = new HashMap<>();
            resourceConfigs = new HashMap<>();
            ConfigurationSection resourcesSection = itemsConfig.getConfiguration().getConfigurationSection("resources");
            assert resourcesSection != null;
            Set<String> resourcesSectionKeys = resourcesSection.getKeys(false);

            for (String resourceKey : resourcesSectionKeys) {
                ConfigurationSection dropConfig = resourcesSection.getConfigurationSection(resourceKey);
                assert dropConfig != null;
                String type = dropConfig.getString("type");
                String rarity = dropConfig.getString("rarity");
                if (type == null) {
                    Questville.getInstance().getLogger().warning("Invalid material type 'null' for drop " + resourceKey);
                    continue;
                }
                Material material = Material.getMaterial(type);
                if (material == null) {
                    Questville.getInstance().getLogger().warning("Invalid material type " + type + " for drop " + resourceKey);
                    continue;
                }

                ItemStack drop = new ItemStack(material);
                ItemMeta meta = drop.getItemMeta();
                assert meta != null;
                meta.setDisplayName(Utils.t(dropConfig.getString("name", material.name())));
                PersistentDataContainer data = meta.getPersistentDataContainer();
                NbtHandler.setTag(data, "resource", PersistentDataType.STRING, resourceKey);
                drop.setItemMeta(meta);
                Utils.hideFlags(drop);

                resourceConfigs.put(resourceKey, dropConfig);
                drop = updateResourceLore(drop);
                resources.put(resourceKey, drop);

            }
        }

        // Load items
        {
            items = new HashMap<>();
            itemConfigs = new HashMap<>();
            ConfigurationSection itemsSection = itemsConfig.getConfiguration().getConfigurationSection("items");
            assert itemsSection != null;
            Set<String> itemsSectionKeys = itemsSection.getKeys(false);

            for (String itemKey : itemsSectionKeys) {
                ConfigurationSection itemConfig = itemsSection.getConfigurationSection(itemKey);
                assert itemConfig != null;
                String type = itemConfig.getString("type");
                if (type == null) {
                    Questville.getInstance().getLogger().warning("Invalid material type 'null' for drop " + itemKey);
                    continue;
                }
                Material material = Material.getMaterial(type);
                if (material == null) {
                    Questville.getInstance().getLogger().warning("Invalid material type " + type + " for drop " + itemKey);
                    continue;
                }

                ItemStack drop = new ItemStack(material);
                ItemMeta meta = drop.getItemMeta();
                assert meta != null;
                meta.setDisplayName(Utils.t(itemConfig.getString("name", material.name())));
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.setUnbreakable(true);
                drop.setItemMeta(meta);
                Utils.hideFlags(drop);

                items.put(itemKey, drop);
                itemConfigs.put(itemKey, itemConfig);
            }
        }

        // Load currency
        {
            currencyItems = new HashMap<>();
            currencyValues = new HashMap<>();
            ConfigurationSection currencySection = itemsConfig.getConfiguration().getConfigurationSection("currency");
            assert currencySection != null;
            Set<String> currencySectionKeys = currencySection.getKeys(false);

            for (String currencyKey : currencySectionKeys) {
                ConfigurationSection dropConfig = currencySection.getConfigurationSection(currencyKey);
                assert dropConfig != null;
                String type = dropConfig.getString("type");
                if (type == null) {
                    Questville.getInstance().getLogger().warning("Invalid material type 'null' for currency " + currencyKey);
                    continue;
                }
                Material material = Material.getMaterial(type);
                if (material == null) {
                    Questville.getInstance().getLogger().warning("Invalid material type " + type + " for currency " + currencyKey);
                    continue;
                }

                ItemStack drop = new ItemStack(material);
                ItemMeta meta = drop.getItemMeta();
                assert meta != null;
                meta.setDisplayName(Utils.t(dropConfig.getString("name", material.name())));
                List<String> lore = dropConfig.getStringList("lore");
                List<String> loreTranslated = new ArrayList<>();
                for (String line : lore) {
                    loreTranslated.add(Utils.t(line));
                }
                meta.setLore(loreTranslated);

                PersistentDataContainer data = meta.getPersistentDataContainer();
                NbtHandler.setTag(data, "currency", PersistentDataType.STRING, currencyKey);
                NbtHandler.setTag(data, "currency-value", PersistentDataType.INTEGER, dropConfig.getInt("value"));
                drop.setItemMeta(meta);
                Utils.hideFlags(drop);
                currencyItems.put(currencyKey, drop);
                currencyValues.put(currencyKey, dropConfig.getInt("value"));
            }
        }

        // Load edition tracker
        {
            try {
                editionTracker = new YmlConfig("edition_tracker.yml");
            } catch (Exception e) {
                Questville.getInstance().disableDueToError(e);
                return;
            }
        }

        Bukkit.getPluginManager().registerEvents(this, Questville.getInstance());
        Bukkit.getLogger().info("All items information loaded");
    }

    public ItemStack getResource(String resource) {
        return resources.get(resource);
    }

    public ItemStack createItem(Player p, String itemId) {
        ItemStack item = items.get(itemId);
        if (item == null) {
            return null;
        }
        return updateItemLore(tagItem(item, itemId, p, false));
    }

    public ItemStack createFakeItem(Player p, String itemId) {
        ItemStack item = items.get(itemId);
        if (item == null) {
            return null;
        }
        return updateItemLore(tagItem(item, itemId, p, true));
    }

    public ItemStack[] getCurrencyItems() {
        ItemStack[] items = new ItemStack[currencyItems.size()];
        int i = 0;
        for (ItemStack item : currencyItems.values()) {
            items[i] = item;
            i++;
        }
        return items;
    }

    public ItemStack updateLore(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemKey = NbtHandler.getTag(data, "item", PersistentDataType.STRING);
        if (itemKey != null) {
            return updateItemLore(item);
        }
        String resourceKey = NbtHandler.getTag(data, "resource", PersistentDataType.STRING);
        if (resourceKey != null) {
            return updateResourceLore(item);
        }
        return item;
    }

    public ItemStack updateResourceLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String resourceKey = NbtHandler.getTag(data, "resource", PersistentDataType.STRING);
        ConfigurationSection resourceConfig = resourceConfigs.get(resourceKey);
        String rarity = resourceConfig.getString("rarity");

        List<String> lore = resourceConfig.getStringList("lore");
        List<String> loreTranslated = new ArrayList<>();
        for (String line : lore) {
            loreTranslated.add(Utils.t(line));
        }
        loreTranslated.add("");
        loreTranslated.add(getRarityDisplay(rarity));
        meta.setLore(loreTranslated);
        item.setItemMeta(meta);
        Utils.hideFlags(item);
        return item;
    }

    public void privateDrop(Player p, ItemStack stack, Location loc) {
        Item itemEntity = Objects.requireNonNull(loc.getWorld()).dropItemNaturally(loc, stack);
        itemEntity.addScoreboardTag("questville:drop");
        itemEntity.addScoreboardTag(p.getUniqueId().toString());
    }

    public void privateDrop(Player p, ItemStack[] stacks, Location loc) {
        for (ItemStack stack : stacks) {
            privateDrop(p, stack, loc);
        }
    }

    public ItemStack updateItemLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String itemKey = NbtHandler.getTag(data, "item", PersistentDataType.STRING);

        ConfigurationSection itemConfig = itemConfigs.get(itemKey);
        assert itemConfig != null;
        String rarity = itemConfig.getString("rarity");
        String creatorUuid = NbtHandler.getTag(data, "creator", PersistentDataType.STRING);
        boolean hasEditions = itemConfig.getBoolean("has-editions", false);
        List<String> lore = itemConfig.getStringList("lore");

        List<String> finalLore = new ArrayList<>();
        for (String line : lore) {
            finalLore.add(Utils.t(line));
        }

        // enchants
        HashMap<CustomEnchantment, Integer> enchants = Questville.getInstance().getCustomEnchantsManager().getEnchantments(item);
        if (enchants.size() > 0) {
            finalLore.add("");
            for (CustomEnchantment enchant : enchants.keySet()) {
                finalLore.add(Utils.t("&9" + EnchantDisplayNames.getDisplayName(enchant) + " " + Utils.toRomanNumeral(enchants.get(enchant))));
            }
        }

        if (hasEditions || creatorUuid != null) {
            finalLore.add("");
            if (creatorUuid != null) {
                finalLore.add(Utils.t("&8Created by: " + Bukkit.getOfflinePlayer(UUID.fromString(creatorUuid)).getName()));
            }
            if (hasEditions) {
                finalLore.add(Utils.t("&8Edition: " + NbtHandler.getTag(data, "edition", PersistentDataType.INTEGER)));
            }
            finalLore.add("");
        }

        finalLore.add(getRarityDisplay(rarity));

        meta.setLore(finalLore);
        item.setItemMeta(meta);
        Utils.hideFlags(item);
        return item;
    }

    private String getRarityDisplay(String rarity) {
        ItemRarity rarity1 = getRarity(rarity);
        if (rarity1 == null) {
            return "&8Unknown rarity";
        }
        return Utils.t(rarity1.getDisplay());
    }

    public ItemRarity getRarity(String rarity) {
        return Arrays.stream(itemRarities).filter(r -> r.getName().equals(rarity)).findFirst().orElse(null);
    }

    public ItemStack tagItem(ItemStack item, String itemKey, Player p, boolean dummy) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        ConfigurationSection itemConfig = itemConfigs.get(itemKey);
        PersistentDataContainer data = meta.getPersistentDataContainer();

        if (p != null) {
            NbtHandler.setTag(data, "creator", PersistentDataType.STRING, p.getUniqueId().toString());
        }
        if (itemConfig.getBoolean("has-editions", false)) {
            int currentEdition = editionTracker.getConfiguration().getInt("edition." + itemKey, 0);
            NbtHandler.setTag(data, "edition", PersistentDataType.INTEGER, currentEdition+1);
            if (!dummy) {
                editionTracker.getConfiguration().set("edition." + itemKey, currentEdition+1);
            }
        }
        NbtHandler.setTag(data, "item", PersistentDataType.STRING, itemKey);

        item.setItemMeta(meta);
        return item;
    }

    public void saveEditions() {
        try {
            editionTracker.save();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save edition tracker data");
            e.printStackTrace();
        }
    }

    public boolean hasAmount(Player p, int amount) {
        return getInventoryBalance(p.getInventory()) >= amount;
    }

    public int getInventoryBalance(Inventory inv) {
        int total = 0;
        for (ItemStack item : inv.getContents()) {
            if (item == null) {
                continue;
            }
            PersistentDataContainer data = Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer();
            Integer currency = NbtHandler.getTag(data, "currency-value", PersistentDataType.INTEGER);
            if (currency == null) {
                continue;
            }
            total += currency * item.getAmount();
        }
        return total;
    }

    public void chargePlayerAmount(Player p, int amount) {
        int remaining = amount;
        for (ItemStack item : p.getInventory().getContents()) {
            if (item == null) {
                continue;
            }
            PersistentDataContainer data = Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer();
            Integer currency = NbtHandler.getTag(data, "currency-value", PersistentDataType.INTEGER);
            if (currency == null) {
                continue;
            }
            if (currency > remaining) {
                // We can't use this item to pay the full amount
                // Take it and give change
                if (item.getAmount() == 1) {
                    p.getInventory().setItem(p.getInventory().first(item), null);
                } else {
                    item.setAmount(item.getAmount() - 1);
                    p.getInventory().setItem(p.getInventory().first(item), item);
                }

                // Give change
                int change = currency - remaining;
                ItemStack[] changeItems = getCurrencyMatchingAmount(change);
                for (ItemStack changeItem : changeItems) {
                    p.getInventory().addItem(changeItem);
                }
            }
            int itemAmount = item.getAmount();
            int itemValue = currency * itemAmount;
            if (itemValue > remaining) {
                item.setAmount((int) Math.ceil((double) remaining / currency));
                p.getInventory().setItem(p.getInventory().first(item), item);
                break;
            } else {
                remaining -= itemValue;
                p.getInventory().setItem(p.getInventory().first(item), null);
            }
        }
    }

    public ItemStack[] getCurrencyMatchingAmount(int amount) {
        // Use the least amount of items possible
        // Get the biggest currencyValues value that is smaller than amount
        int remaining = amount;
        List<ItemStack> items = new ArrayList<>();

        int maxLoop = 100;
        while (remaining > 0) {
            maxLoop--;
            if (maxLoop <= 0) {
                Bukkit.getLogger().severe("Failed to get currency matching amount. Loop limit reached. Remaining: " + remaining + " Amount: " + amount);
                return null;
            }

            int biggestValue = 0;
            String biggestKey = null;
            for (String key : currencyValues.keySet()) {
                int value = currencyValues.get(key);
                if (value > biggestValue && value <= remaining) {
                    biggestValue = value;
                    biggestKey = key;
                }
            }

            if (biggestKey == null) {
                // No currency found
                return null;
            }

            ItemStack item = currencyItems.get(biggestKey);
            int itemAmount = (int) Math.floor((double) remaining / biggestValue);
            remaining -= itemAmount * biggestValue;
            item.setAmount(itemAmount);
            items.add(item);
        }

        return items.toArray(new ItemStack[0]);
    }

    public void removeItemFromPlayer(Player p, String item, int amount) {
        for (ItemStack itemStack : p.getInventory().getContents()) {
            if (amount == 0) {
                return;
            }
            if (itemStack == null) {
                continue;
            }
            PersistentDataContainer data = Objects.requireNonNull(itemStack.getItemMeta()).getPersistentDataContainer();
            String itemKey = NbtHandler.getTag(data, "item", PersistentDataType.STRING);
            if (itemKey == null) {
                continue;
            }
            if (!itemKey.equals(item)) {
                continue;
            }
            amount -= itemStack.getAmount();
            p.getInventory().setItem(p.getInventory().first(itemStack), null);
        }
    }

    public boolean hasItem(Player p, String item, int amount) {
        int total = 0;
        for (ItemStack itemStack : p.getInventory().getContents()) {
            if (itemStack == null) {
                continue;
            }
            PersistentDataContainer data = Objects.requireNonNull(itemStack.getItemMeta()).getPersistentDataContainer();
            String itemKey = NbtHandler.getTag(data, "item", PersistentDataType.STRING);
            if (itemKey == null) {
                continue;
            }
            if (!itemKey.equals(item)) {
                continue;
            }
            total += itemStack.getAmount();
            if (total >= amount) {
                return true;
            }
        }
        return total >= amount;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        if (!Questville.enabledIn(e.getEntity().getWorld())) {
            return;
        }

        if (!e.getItem().getScoreboardTags().contains("questville:drop")) {
            return;
        }

        if (!e.getItem().getScoreboardTags().contains(e.getEntity().getUniqueId().toString())) {
            e.setCancelled(true);
            e.getEntity().sendMessage(Utils.t("&cYou cannot pickup items mined by other players!"));
        }
    }

    public ItemAttributes getItemAttributes(String itemKey) {
        ConfigurationSection config = itemConfigs.get(itemKey);
        if (config == null) {
            return null;
        }
        return new ItemAttributes(config);
    }

    public ItemAttributes getItemAttributes(ItemStack item) {
        PersistentDataContainer data = Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer();
        String itemKey = NbtHandler.getTag(data, "item", PersistentDataType.STRING);
        if (itemKey == null) {
            return null;
        }
        return getItemAttributes(itemKey);
    }

    public CustomItem getItem(String itemKey) {
        ConfigurationSection config = itemConfigs.get(itemKey);
        if (config == null) {
            return null;
        }
        return new CustomItem(config);
    }

    public CustomItem getItem(ItemStack stack) {
        PersistentDataContainer data = Objects.requireNonNull(stack.getItemMeta()).getPersistentDataContainer();
        String itemKey = NbtHandler.getTag(data, "item", PersistentDataType.STRING);
        if (itemKey == null) {
            return null;
        }
        return getItem(itemKey);
    }

}
