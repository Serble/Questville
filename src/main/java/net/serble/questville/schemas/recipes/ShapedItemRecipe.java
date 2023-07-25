package net.serble.questville.schemas.recipes;

import net.serble.questville.NbtHandler;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Objects;

public class ShapedItemRecipe implements IItemRecipe {
    private final String[] ingredients;
    private final String result;
    private final int amount;
    private final String id;

    public ShapedItemRecipe(String i, String r, int a) {
        result = r;
        ingredients = new String[9];
        amount = a;
        id = i;
    }

    public String[] getIngredients() {
        return ingredients;
    }

    public String getResult() {
        return result;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    public void setIngredient(int slot, String item) {
        ingredients[slot] = item;
    }

    @Override
    public String getId() {
        return id;
    }

    private void loopMaxedDump(ItemStack[] inputItems) {
        Bukkit.getLogger().severe("Maxed out loop count, dumping matrix:");
        for (int i = 0; i < inputItems.length; i++) {
            Bukkit.getLogger().severe(i + ". " + (inputItems[i] == null ? "null" : inputItems[i].getType()));
        }
    }

    public boolean isMatching(ItemStack[] inputItems) {
        String[] ingredients = getIngredients();

        if (Arrays.stream(inputItems).allMatch(Objects::isNull)) {
            return false;  // Otherwise they infinite loop
        }

        int maxLoopsCountdown = 100;

        // While the left column is empty, shift the matrix to the left
        while (inputItems[0] == null && inputItems[3] == null && inputItems[6] == null) {
            inputItems[0] = inputItems[1];
            inputItems[1] = inputItems[2];
            inputItems[2] = null;
            inputItems[3] = inputItems[4];
            inputItems[4] = inputItems[5];
            inputItems[5] = null;
            inputItems[6] = inputItems[7];
            inputItems[7] = inputItems[8];
            inputItems[8] = null;
            maxLoopsCountdown--;
            if (maxLoopsCountdown <= 0) {
                loopMaxedDump(inputItems);
                break;
            }
        }

        // While the top row is empty, shift the matrix up
        while (inputItems[0] == null && inputItems[1] == null && inputItems[2] == null) {
            inputItems[0] = inputItems[3];
            inputItems[1] = inputItems[4];
            inputItems[2] = inputItems[5];
            inputItems[3] = inputItems[6];
            inputItems[4] = inputItems[7];
            inputItems[5] = inputItems[8];
            inputItems[6] = null;
            inputItems[7] = null;
            inputItems[8] = null;
            maxLoopsCountdown--;
            if (maxLoopsCountdown <= 0) {
                loopMaxedDump(inputItems);
                break;
            }
        }

        // SAME FOR INGRIDIENTS
        while (ingredients[0] == null && ingredients[3] == null && ingredients[6] == null) {
            ingredients[0] = ingredients[1];
            ingredients[1] = ingredients[2];
            ingredients[2] = null;
            ingredients[3] = ingredients[4];
            ingredients[4] = ingredients[5];
            ingredients[5] = null;
            ingredients[6] = ingredients[7];
            ingredients[7] = ingredients[8];
            ingredients[8] = null;
            maxLoopsCountdown--;
            if (maxLoopsCountdown <= 0) {
                loopMaxedDump(inputItems);
                break;
            }
        }

        // SAME FOR INGRIDIENTS
        while (ingredients[0] == null && ingredients[1] == null && ingredients[2] == null) {
            ingredients[0] = ingredients[3];
            ingredients[1] = ingredients[4];
            ingredients[2] = ingredients[5];
            ingredients[3] = ingredients[6];
            ingredients[4] = ingredients[7];
            ingredients[5] = ingredients[8];
            ingredients[6] = null;
            ingredients[7] = null;
            ingredients[8] = null;
            maxLoopsCountdown--;
            if (maxLoopsCountdown <= 0) {
                loopMaxedDump(inputItems);
                break;
            }
        }

        for (int i = 0; i < ingredients.length; i++) {
            if (ingredients[i] == null && inputItems[i] == null) {
                continue;
            }
            if (ingredients[i] == null || inputItems[i] == null) {
                return false;
            }
            PersistentDataContainer container2 = Objects.requireNonNull(Objects.requireNonNull(inputItems[i]).getItemMeta()).getPersistentDataContainer();
            boolean isResource = ingredients[i].startsWith("resource:");
            boolean isItem = ingredients[i].startsWith("item:");

            String type = isResource ? "resource" : isItem ? "item" : null;
            if (type == null) {
                return false;
            }
            String itemId = NbtHandler.getTag(container2, type, PersistentDataType.STRING);
            if (!ingredients[i].equals(type + ":" + itemId)) {
                return false;  // Not same resource
            }
        }
        return true;
    }
}
