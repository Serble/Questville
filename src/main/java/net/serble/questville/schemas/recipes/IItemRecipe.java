package net.serble.questville.schemas.recipes;

import org.bukkit.inventory.ItemStack;

public interface IItemRecipe {
    boolean isMatching(ItemStack[] items);
    String getResult();
    int getAmount();
    String[] getIngredients();
    void setIngredient(int slot, String item);
    String getId();
}
