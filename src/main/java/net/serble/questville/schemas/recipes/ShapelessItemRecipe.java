package net.serble.questville.schemas.recipes;

import net.serble.questville.NbtHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ShapelessItemRecipe implements IItemRecipe {
    private final String[] requiredItems;
    private final String result;
    private final int amount;
    private final String id;

    public ShapelessItemRecipe(String i, String r, int a) {
        result = r;
        requiredItems = new String[9];
        amount = a;
        id = i;
    }

    @Override
    public boolean isMatching(ItemStack[] items) {
        List<String> remainingItems = new ArrayList<>(Arrays.asList(requiredItems));
        remainingItems.removeIf(Objects::isNull);
        for (ItemStack item : items) {
            if (item == null) {
                continue;
            }
            if (item.getItemMeta() == null) {
                return false;
            }
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            boolean isResource = NbtHandler.hasTag(container, "resource", PersistentDataType.STRING);
            boolean isItem = NbtHandler.hasTag(container, "item", PersistentDataType.STRING);
            String key = isResource ? "resource" : isItem ? "item" : null;
            if (key == null) {
                return false;
            }
            String itemId = NbtHandler.getTag(container, key, PersistentDataType.STRING);
            if (!remainingItems.contains(key + ":" + itemId)) {
                return false;  // Not same thing
            }
            remainingItems.remove(key + ":" + itemId);
        }
        return (long) remainingItems.size() == 0;
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public String[] getIngredients() {
        return requiredItems;
    }

    @Override
    public void setIngredient(int slot, String item) {
        requiredItems[slot] = item;
    }

    @Override
    public String getId() {
        return id;
    }
}
