package net.serble.questville;

import net.serble.custombreaks.Schemas.PlayerAttemptBreakBlockEvent;
import net.serble.questville.schemas.items.ItemDropInfo;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class MiningManager implements Listener {
    private HashMap<Material, ItemDropInfo> drops;
    private final List<BukkitTask> blockReplaceTasks = new ArrayList<>();

    public MiningManager() {
        Questville.getInstance().saveResource("mining.yml", false);

        YmlConfig miningConfig;
        try {
            miningConfig = new YmlConfig("mining.yml");
        } catch (Exception e) {
            Questville.getInstance().disableDueToError(e);
            return;
        }

        // Load drops
        {
            drops = new HashMap<>();
            ConfigurationSection dropsSection = miningConfig.getConfiguration().getConfigurationSection("drops");
            assert dropsSection != null;
            Set<String> dropsSectionKeys = dropsSection.getKeys(false);

            for (String dropKey : dropsSectionKeys) {
                ConfigurationSection dropConfig = dropsSection.getConfigurationSection(dropKey);
                assert dropConfig != null;
                String type = dropConfig.getString("type");
                if (type == null) {
                    Questville.getInstance().getLogger().warning("Invalid material type 'null' for drop " + dropKey);
                    continue;
                }
                Material material = Material.getMaterial(type);
                if (material == null) {
                    Questville.getInstance().getLogger().warning("Invalid material type " + type + " for drop " + dropKey);
                    continue;
                }

                ItemDropInfo info = new ItemDropInfo(
                        dropConfig.getString("resource"),
                        dropConfig.getString("break-type", "stone"),
                        dropConfig.getInt("money", 0),
                        dropConfig.getInt("amount", 1));
                drops.put(material, info);
            }
        }

        Bukkit.getServer().getPluginManager().registerEvents(this, Questville.getInstance());
        Bukkit.getLogger().info("MiningManager enabled!");
    }

    @EventHandler
    public void onAttemptBreak(PlayerAttemptBreakBlockEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (!Questville.enabledIn(e.getPlayer())) {
            return;
        }
        ItemDropInfo info = drops.get(e.getBlock().getType());
        if (info == null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (!Questville.enabledIn(e.getPlayer())) {
            return;
        }
        ItemDropInfo info = drops.get(e.getBlock().getType());
        if (info == null) {
            e.setCancelled(true);
            return;
        }
        e.setDropItems(false);
        if (!Objects.equals(info.getResource(), "none")) {
            ItemStack drop = Questville.getInstance().getItemManager().getResource(info.getResource());
            drop.setAmount(info.getDropAmount());
            Questville.getInstance().getItemManager().privateDrop(e.getPlayer(), drop, e.getBlock().getLocation());
        }

        if (info.getMoney() > 0) {
            ItemStack[] drops = Questville.getInstance().getItemManager().getCurrencyMatchingAmount(info.getMoney());
            Questville.getInstance().getItemManager().privateDrop(e.getPlayer(), drops, e.getBlock().getLocation());
        }

        switch (info.getDropType()) {
            case "ore": {
                e.getBlock().setType(Material.STONE);
                e.setCancelled(true);
                break;
            }

            case "tree": {
                e.setCancelled(true);
                Material blockBelowType = e.getBlock().getRelative(0, -1, 0).getType();
                if (blockBelowType == Material.DIRT || blockBelowType == Material.GRASS_BLOCK) {
                    e.getBlock().setType(Material.OAK_SAPLING);
                } else {
                    e.getBlock().setType(Material.AIR);
                }
                break;
            }

            case "vanilla": {
                break;
            }

            default:
            case "stone": {
                Material blockBeforeChange = e.getBlock().getType();
                BukkitTask task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        e.getBlock().setType(blockBeforeChange);
                        this.getTaskId();
                    }
                }.runTaskLater(Questville.getInstance(), 5 * 20L);
                blockReplaceTasks.add(task);
                e.getBlock().setType(Material.BEDROCK);
                e.setCancelled(true);
                break;
            }

        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (!Questville.enabledIn(e.getPlayer())) {
            return;
        }
        e.setCancelled(true);
    }

    public void finishBlockReplaceTasks() {
        for (BukkitTask task : blockReplaceTasks) {
            task.cancel();
        }
    }

}
