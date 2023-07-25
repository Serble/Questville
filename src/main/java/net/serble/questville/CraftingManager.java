package net.serble.questville;

import net.serble.questville.schemas.recipes.IItemRecipe;
import net.serble.questville.schemas.recipes.ShapedItemRecipe;
import net.serble.questville.schemas.recipes.ShapelessItemRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CraftingManager implements Listener {
    private final List<IItemRecipe> recipes;
    private final HashMap<UUID, CraftActiveMenu> activeMenus = new HashMap<>();

    public CraftingManager() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Questville.getInstance());

        recipes = new ArrayList<>();
        Questville.getInstance().saveResource("crafting.yml", false);
        YmlConfig craftingConfig;
        try {
            craftingConfig = new YmlConfig("crafting.yml");
        } catch (Exception e) {
            Questville.getInstance().disableDueToError(e);
            return;
        }

        ConfigurationSection recipesSection = craftingConfig.getConfiguration().getConfigurationSection("recipes");
        assert recipesSection != null;
        for (String recipeKey : recipesSection.getKeys(false)) {
            ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeKey);
            assert recipeSection != null;
            String type = recipeSection.getString("type");
            String result = recipeSection.getString("result");
            int amount = recipeSection.getInt("amount");

            IItemRecipe recipe;
            switch (Objects.requireNonNull(type)) {
                case "shapeless": {
                    recipe = new ShapelessItemRecipe(recipeKey, result, amount);
                    break;
                }

                default:
                case "shaped": {
                    recipe = new ShapedItemRecipe(recipeKey, result, amount);
                    break;
                }
            }

            ConfigurationSection ingredientsSection = recipeSection.getConfigurationSection("ingredients");
            assert ingredientsSection != null;
            for (String ingredientKey : ingredientsSection.getKeys(false)) {
                String ingredient = ingredientsSection.getString(ingredientKey);
                assert ingredient != null;
                recipe.setIngredient(Integer.parseInt(ingredientKey), ingredient);
            }

            recipes.add(recipe);
            Bukkit.getLogger().info("Loaded recipe " + recipeKey + " with result " + result);
        }
    }

    @EventHandler
    public void onAttemptCraft(PrepareItemCraftEvent e) {
        Player p = (Player) e.getView().getPlayer();
        if (!Questville.enabledIn(p)) {
            return;
        }

        ItemStack[] matrix = e.getInventory().getMatrix();

        if (e.getInventory().getType() == InventoryType.CRAFTING) {
            ItemStack[] newMatrix = new ItemStack[9];
            newMatrix[0] = matrix[0];
            newMatrix[1] = matrix[1];
            newMatrix[3] = matrix[2];
            newMatrix[4] = matrix[3];
            matrix = newMatrix;
        }

        for (IItemRecipe recipe : recipes) {
            if (!recipe.isMatching(matrix)) {
                continue;
            }
            ItemStack item = getCraftResult(recipe.getResult(), p, true);
            item.setAmount(recipe.getAmount());
            e.getInventory().setResult(item);
            return;
        }
        e.getInventory().setResult(null);
    }

    private ItemStack getCraftResult(String itemString, Player p, boolean dummy) {
        boolean isItem = itemString.startsWith("item:");
        if (isItem) {
            if (dummy) {
                return Questville.getInstance().getItemManager().createFakeItem(p, itemString.replace("item:", ""));
            }
            return Questville.getInstance().getItemManager().createItem(p, itemString.replace("item:", ""));
        }
        return Questville.getInstance().getItemManager().getResource(itemString.replace("resource:", ""));
    }

    // When they actually craft it override the item with a non-dummy one
    @EventHandler
    public void onCraft(CraftItemEvent e) {
        Player p = (Player) e.getView().getPlayer();
        if (!Questville.enabledIn(p)) {
            return;
        }

        ItemStack[] matrix = e.getInventory().getMatrix();

        if (e.getInventory().getType() == InventoryType.CRAFTING) {
            ItemStack[] newMatrix = new ItemStack[9];
            newMatrix[0] = matrix[0];
            newMatrix[1] = matrix[1];
            newMatrix[3] = matrix[2];
            newMatrix[4] = matrix[3];
            matrix = newMatrix;
        }

        for (IItemRecipe recipe : recipes) {
            if (recipe.isMatching(matrix)) {
                ItemStack item = getCraftResult(recipe.getResult(), p, false);
                item.setAmount(recipe.getAmount());
                e.getInventory().setResult(item);
                return;
            }
        }
    }

    public void openRecipeBook(Player p) {
        Inventory inv = Bukkit.createInventory(null, 5*9, "Recipe Book");

        int i = 0;
        for (IItemRecipe recipe : recipes) {
            inv.setItem(i, getRecipeBookButton(recipe, p));
            i++;
        }

        // Fill the rest will filler item
        ItemStack filler = getFillerItem();
        for (; i < 5*9; i++) {
            inv.setItem(i, filler);
        }

        p.openInventory(inv);
        activeMenus.put(p.getUniqueId(), CraftActiveMenu.RECIPE_BOOK);
    }

    private ItemStack getFillerItem() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        assert fillerMeta != null;
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        return filler;
    }

    @EventHandler
    public void invClick(InventoryClickEvent e) {
        CraftActiveMenu menu = activeMenus.get(e.getWhoClicked().getUniqueId());
        if (menu == null) {
            return;
        }

        if (menu == CraftActiveMenu.RECIPE_BOOK) {
            e.setCancelled(true);
            String recipeId = getRecipeId(e.getCurrentItem());
            if (recipeId == null) {
                return;
            }

            IItemRecipe recipe = recipes.stream().filter(r -> r.getId().equals(recipeId)).findFirst().orElse(null);
            if (recipe == null) {
                return;
            }

            openRecipeViewer((Player) e.getWhoClicked(), recipe);
            activeMenus.put(e.getWhoClicked().getUniqueId(), CraftActiveMenu.RECIPE_VIEW);
        }

        if (menu == CraftActiveMenu.RECIPE_VIEW) {
            e.setCancelled(true);
        }

    }

    private ItemStack getRecipeBookButton(IItemRecipe recipe, Player p) {
        ItemStack button = new ItemStack(getCraftResult(recipe.getResult(), p, true));
        ItemMeta meta = button.getItemMeta();
        assert meta != null;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NbtHandler.setTag(container, "recipe_book_recipe", PersistentDataType.STRING, recipe.getId());
        button.setItemMeta(meta);
        Utils.hideFlags(button);
        return button;
    }

    public void openRecipeViewer(Player p, IItemRecipe recipe) {
        Inventory inv = Bukkit.createInventory(null, 5*9, "Recipe Viewer");

        // Fill all slots with grey glass
        for (int i = 0; i < 5*9; i++) {
            inv.setItem(i, getFillerItem());
        }

        // Fill the middle 3x3 area the recipe
        // Start with the first row
        int ingredientIndex = 0;
        int invIndex = 10;
        for (int i = 0; i < 3; i++) {
            String ingredient = recipe.getIngredients()[ingredientIndex];
            ItemStack item = getIngredientItem(ingredient, p);
            inv.setItem(invIndex, item);
            ingredientIndex++;
            invIndex++;
        }

        // Second row
        invIndex = 19;
        for (int i = 0; i < 3; i++) {
            String ingredient = recipe.getIngredients()[ingredientIndex];
            ItemStack item = getIngredientItem(ingredient, p);
            inv.setItem(invIndex, item);
            ingredientIndex++;
            invIndex++;
        }

        // Third row
        invIndex = 28;
        for (int i = 0; i < 3; i++) {
            String ingredient = recipe.getIngredients()[ingredientIndex];
            ItemStack item = getIngredientItem(ingredient, p);
            inv.setItem(invIndex, item);
            ingredientIndex++;
            invIndex++;
        }

        // 23 is result
        inv.setItem(23, getCraftResult(recipe.getResult(), p, true));

        p.openInventory(inv);
        activeMenus.put(p.getUniqueId(), CraftActiveMenu.RECIPE_VIEW);
    }

    private ItemStack getIngredientItem(String ingredient, Player p) {
        if (ingredient == null) {
            return null;
        }
        boolean isItem = ingredient.startsWith("item:");
        if (isItem) {
            return Questville.getInstance().getItemManager().createFakeItem(p, ingredient.replace("item:", ""));
        }
        return Questville.getInstance().getItemManager().getResource(ingredient.replace("resource:", ""));
    }

    private String getRecipeId(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return NbtHandler.getTag(container, "recipe_book_recipe", PersistentDataType.STRING);
    }

    @EventHandler
    public void invClose(InventoryCloseEvent e) {
        activeMenus.remove(e.getPlayer().getUniqueId());
    }

}

enum CraftActiveMenu {
    RECIPE_BOOK,
    RECIPE_VIEW
}