package net.serble.questville;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class PlayerManagerItem implements Listener {
    private ItemStack item;
    private final HashMap<UUID, ActiveMenu> activeMenus = new HashMap<>();
    private static final int SLOT = 8;

    public PlayerManagerItem() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Questville.getInstance());
    }

    @EventHandler
    public void onWorldJoin(PlayerChangedWorldEvent e) {
        if (!Questville.enabledIn(e.getPlayer())) {
            return;
        }
        e.getPlayer().getInventory().setItem(SLOT, getItem());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!Questville.enabledIn(e.getPlayer())) {
            return;
        }
        e.getPlayer().getInventory().setItem(SLOT, getItem());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!Questville.enabledIn(e.getPlayer())) {
            return;
        }

        if (!isItemStackTheItem(e.getItemDrop().getItemStack())) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (!Questville.enabledIn(e.getPlayer())) {
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!isItemStackTheItem(e.getItem())) {
            return;
        }

        e.setCancelled(true);
        run(e.getPlayer());
    }

    private boolean isItemStackTheItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return NbtHandler.hasTag(container, "player_manager", PersistentDataType.INTEGER);
    }

    private void run(Player p) {
        p.sendMessage(Utils.t("&6You clicked it!"));
        openMainMenu(p);
    }

    private void openMainMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 9, Utils.t("&6Questville"));

        inv.setItem(0, makeButton(Material.CRAFTING_TABLE, "Crafting Table", "craft"));
        inv.setItem(1, makeButton(Material.BOOK, "Recipe Book", "recipes"));

        p.openInventory(inv);
        activeMenus.put(p.getUniqueId(), ActiveMenu.MAIN);
    }

    private ItemStack makeButton(Material item, String name, String id, String... lore) {
        ItemStack button = new ItemStack(item);
        ItemMeta meta = button.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Utils.t("&r" + name));
        meta.setLore(Arrays.asList(lore));
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NbtHandler.setTag(container, "player_manager_item", PersistentDataType.STRING, id);
        button.setItemMeta(meta);
        Utils.hideFlags(button);
        return button;
    }

    private String getItemId(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return NbtHandler.getTag(container, "player_manager_item", PersistentDataType.STRING);
    }

    @EventHandler
    public void invClick(InventoryClickEvent e) {
        String id = getItemId(e.getCurrentItem());
        if (activeMenus.get(e.getWhoClicked().getUniqueId()) == ActiveMenu.MAIN) {
            e.setCancelled(true);
            if (id == null) {
                return;
            }
            switch (id) {
                case "craft": {
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().openWorkbench(e.getWhoClicked().getLocation(), true);
                    break;
                }

                case "recipes": {
                    e.getWhoClicked().closeInventory();
                    Questville.getInstance().getCraftingManager().openRecipeBook((Player) e.getWhoClicked());
                    break;
                }

            }
            return;
        }
    }

    @EventHandler
    public void invClose(InventoryCloseEvent e) {
        activeMenus.remove(e.getPlayer().getUniqueId());
    }

    private ItemStack getItem() {
        if (item != null) {
            return item;
        }
        item = new ItemStack(Material.NETHER_STAR);
        item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 0);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Utils.t("§6Questville"));
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NbtHandler.setTag(container, "player_manager", PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
        Utils.hideFlags(item);
        return item;
    }

}

enum ActiveMenu {
    MAIN
}