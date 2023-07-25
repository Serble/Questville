package net.serble.questville;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BankManager implements Listener {
    private static final int BANK_SIZE = 3*9;
    private static final int DEPOSIT_SIZE = 3*9;
    private static final List<UUID> openBank = new ArrayList<>();
    private static final List<UUID> openDeposit = new ArrayList<>();
    private static final List<UUID> openWithdraw = new ArrayList<>();

    private static final HashMap<UUID, Integer> balance = new HashMap<>();

    /*
    P = Filler Item - Green glass pane
    D = Deposit Item - Chest
    B = Balance Item - Emerald
    W = Withdraw Item - Barrier

    |___________|
    | PPPPPPPPP |
    | PPDPBPWPP |
    | PPPPPPPPP |
    |-----------|
     */

    public BankManager() {
        loadBalanceFromFile();

        Bukkit.getPluginManager().registerEvents(this, Questville.getInstance());
    }

    public void openBank(Player p) {
        p.openInventory(buildBankGui(p));
        openBank.add(p.getUniqueId());
    }

    private Inventory buildBankGui(Player p) {
        Inventory inv = Bukkit.createInventory(null, BANK_SIZE, "Bank");

        ItemStack filler = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        for (int i = 0; i < BANK_SIZE; i++) {
            inv.setItem(i, filler);
        }

        // Deposit item
        ItemStack deposit = new ItemStack(Material.CHEST);
        ItemMeta depositMeta = deposit.getItemMeta();
        assert depositMeta != null;
        depositMeta.setDisplayName(Utils.t("&aDeposit"));
        PersistentDataContainer depositData = depositMeta.getPersistentDataContainer();
        NbtHandler.setTag(depositData, "bank-item", PersistentDataType.STRING, "deposit");
        deposit.setItemMeta(depositMeta);
        inv.setItem(12, deposit);

        // Balance item
        ItemStack balance = new ItemStack(Material.EMERALD);
        ItemMeta balanceMeta = balance.getItemMeta();
        assert balanceMeta != null;
        balanceMeta.setDisplayName(Utils.t("&6Balance: $" + getBalance(p)));
        balance.setItemMeta(balanceMeta);
        inv.setItem(13, balance);

        // Withdraw item
        ItemStack withdraw = new ItemStack(Material.BARRIER);
        ItemMeta withdrawMeta = withdraw.getItemMeta();
        assert withdrawMeta != null;
        withdrawMeta.setDisplayName(Utils.t("&cWithdraw"));
        PersistentDataContainer withdrawData = withdrawMeta.getPersistentDataContainer();
        NbtHandler.setTag(withdrawData, "bank-item", PersistentDataType.STRING, "withdraw");
        withdraw.setItemMeta(withdrawMeta);
        inv.setItem(14, withdraw);

        return inv;
    }

    public int getBalance(Player p) {
        return balance.getOrDefault(Utils.getEffectiveUuid(p), 0);
    }

    public void setBalance(Player p, int amount) {
        balance.put(Utils.getEffectiveUuid(p), amount);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (openBank.contains(e.getWhoClicked().getUniqueId())) {
            bankClick(e);
        }

        if (openWithdraw.contains(e.getWhoClicked().getUniqueId())) {
            withdrawClick(e);
        }
    }

    private void withdrawClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getCurrentItem() == null) {
            return;
        }
        if (e.getCurrentItem().getItemMeta() == null) {
            return;
        }
        PersistentDataContainer data = e.getCurrentItem().getItemMeta().getPersistentDataContainer();
        if (!NbtHandler.hasTag(data, "currency-value", PersistentDataType.INTEGER)) {
            return;
        }

        Player p = (Player) e.getWhoClicked();

        int currencyItem = NbtHandler.getTag(data, "currency-value", PersistentDataType.INTEGER);
        int balance = getBalance(p);

        if (balance < currencyItem) {
            p.sendMessage(Utils.t("&cYou don't have enough money to withdraw that!"));
            return;
        }

        p.getInventory().addItem(e.getCurrentItem());
        setBalance(p, balance - currencyItem);
        saveBalanceToFileAsync();
    }

    private void bankClick(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getCurrentItem() == null) {
            return;
        }
        if (e.getCurrentItem().getItemMeta() == null) {
            return;
        }
        PersistentDataContainer data = e.getCurrentItem().getItemMeta().getPersistentDataContainer();
        if (!NbtHandler.hasTag(data, "bank-item", PersistentDataType.STRING)) {
            return;
        }

        Player p = (Player) e.getWhoClicked();

        String bankItem = NbtHandler.getTag(data, "bank-item", PersistentDataType.STRING);
        if (bankItem.equals("deposit")) {
            openDepositGui(p);
        } else if (bankItem.equals("withdraw")) {
            openWithdrawalGui(p);
        }
    }

    private void openDepositGui(Player p) {
        p.sendMessage(Utils.t("&aPlace currency into this inventory to deposit it."));
        Inventory inv = Bukkit.createInventory(null, DEPOSIT_SIZE, "Deposit");
        p.openInventory(inv);
        openDeposit.add(p.getUniqueId());
    }

    private void openWithdrawalGui(Player p) {
        p.sendMessage(Utils.t("&aClick on the currency you want to withdraw."));
        ItemStack[] currencyItems = Questville.getInstance().getItemManager().getCurrencyItems();
        int invSize = (int) Math.ceil(currencyItems.length / 9.0) * 9;

        Inventory inv = Bukkit.createInventory(null, invSize, "Withdraw");
        for (int i = 0; i < currencyItems.length; i++) {
            inv.setItem(i, currencyItems[i]);
        }

        p.openInventory(inv);
        openWithdraw.add(p.getUniqueId());
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if (openBank.contains(e.getPlayer().getUniqueId())) {
            openBank.remove(e.getPlayer().getUniqueId());
            return;
        }

        if (openDeposit.contains(e.getPlayer().getUniqueId())) {
            // Deposit the money
            int amount = Questville.getInstance().getItemManager().getInventoryBalance(e.getInventory());
            balance.put(Utils.getEffectiveUuid((Player) e.getPlayer()), getBalance((Player) e.getPlayer()) + amount);
            e.getPlayer().sendMessage(Utils.t("&aDeposited &6$" + amount + "&a."));
            openDeposit.remove(e.getPlayer().getUniqueId());
            saveBalanceToFileAsync();
            return;
        }

        if (openWithdraw.contains(e.getPlayer().getUniqueId())) {
            openWithdraw.remove(e.getPlayer().getUniqueId());
        }
    }

    private void saveBalanceToFileAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(Questville.getInstance(), this::saveBalanceToFile);
    }

    private void saveBalanceToFile() {
        File file = new File(Questville.getInstance().getDataFolder(), "bank-balances.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (UUID uuid : balance.keySet()) {
            config.set(uuid.toString(), balance.get(uuid));
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBalanceFromFile() {
        File file = new File(Questville.getInstance().getDataFolder(), "bank-balances.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String uuid : config.getKeys(false)) {
            balance.put(UUID.fromString(uuid), config.getInt(uuid));
        }
    }

}
