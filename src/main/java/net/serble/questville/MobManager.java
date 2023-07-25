package net.serble.questville;

import net.serble.questville.schemas.ChanceReward;
import net.serble.questville.schemas.CustomMob;
import net.serble.questville.schemas.MobSpawnArea;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MobManager implements Listener {
    private final HashMap<String, CustomMob> mobs = new HashMap<>();
    private List<MobSpawnArea> mobSpawnAreas;
    private final HashMap<String, List<LivingEntity>> spawnedMobs = new HashMap<>();

    public MobManager() {
        Questville.getInstance().saveResource("mobs.yml", false);

        YmlConfig mobsConfig;
        try {
            mobsConfig = new YmlConfig("mobs.yml");
        } catch (Exception e) {
            Questville.getInstance().disableDueToError(e);
            return;
        }

        // Load mobs
        {
            ConfigurationSection mobsListSection = mobsConfig.getConfiguration().getConfigurationSection("mobs");
            assert mobsListSection != null;
            for (String mobKey : mobsListSection.getKeys(false)) {
                ConfigurationSection mobConfig = mobsListSection.getConfigurationSection(mobKey);
                assert mobConfig != null;
                mobs.put(mobKey, new CustomMob(mobConfig));
            }
        }

        // Load mob spawn areas
        {
            Bukkit.getLogger().info("Loading mob spawn areas");
            mobSpawnAreas = new ArrayList<>();
            ConfigurationSection mobSpawnAreasSection = mobsConfig.getConfiguration().getConfigurationSection("areas");
            assert mobSpawnAreasSection != null;
            for (String areaKey : mobSpawnAreasSection.getKeys(false)) {
                ConfigurationSection areaConfig = mobSpawnAreasSection.getConfigurationSection(areaKey);
                assert areaConfig != null;
                mobSpawnAreas.add(new MobSpawnArea(areaConfig));
                Bukkit.getLogger().info("Loaded mob spawn area " + areaKey);
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (MobSpawnArea area : mobSpawnAreas) {
                    if (!spawnedMobs.containsKey(area.getId())) {
                        spawnedMobs.put(area.getId(), new ArrayList<>());
                    }
                    if (spawnedMobs.get(area.getId()).size() >= area.getMaxMobs()) {
                        //Bukkit.getLogger().info("Skipping spawn area " + area.getId() + " because it has " + spawnedMobs.get(area.getId()).size() + " mobs, it needs " + area.getMaxMobs() + " mobs");
                        continue;
                    }

                    // Spawn a mob
                    String mobId = area.getMobs().get(new Random().nextInt(area.getMobs().size()));

                    // Find a random air block within pos1 and pos2
                    Location spawnLocation = area.getRandomLocation();

                    Bukkit.getLogger().info("Spawning mob " + mobId + " at " + spawnLocation);
                    if (spawnedMobs.containsKey(area.getId())) {
                        spawnedMobs.get(area.getId()).add(spawnMob(mobId, spawnLocation));
                    } else {
                        List<LivingEntity> mobs = new ArrayList<>();
                        mobs.add(spawnMob(mobId, spawnLocation));
                        spawnedMobs.put(area.getId(), mobs);
                    }
                }
            }
        }.runTaskTimer(Questville.getInstance(), 20, 20 * 2);
        Bukkit.getPluginManager().registerEvents(this, Questville.getInstance());
    }

    public void purgeMobs() {
        for (List<LivingEntity> mobs : spawnedMobs.values()) {
            for (LivingEntity mob : mobs) {
                mob.remove();
            }
        }
        spawnedMobs.clear();
    }

    @EventHandler
    public void entityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        LivingEntity entity = event.getEntity();
        Player p = entity.getKiller();
        CustomMob mob = getMob(entity);
        if (mob == null) {
            // Not a custom mob
            return;
        }

        // If it is being tracked, remove it from the list
        removeEntityFromList(entity);

        // Rewards
        ItemStack[] money = Questville.getInstance().getItemManager().getCurrencyMatchingAmount(mob.getMoneyReward());
        Questville.getInstance().getItemManager().privateDrop(p, money, entity.getLocation());

        String[] items = mob.getDrops();
        for (String item : items) {
            ItemStack itemStack = Questville.getInstance().getItemManager().createItem(p, item);
            ItemStack resourceStack = Questville.getInstance().getItemManager().getResource(item);
            if (item == null && resourceStack == null) continue;
            Questville.getInstance().getItemManager().privateDrop(p, itemStack == null ? resourceStack : itemStack, entity.getLocation());
        }

        // Chance rewards
        for (ChanceReward item : mob.getChanceRewards()) {
            if (new Random().nextInt(100) < item.getChance()) {
                ItemStack itemStack = Questville.getInstance().getItemManager().createItem(p, item.getReward());
                ItemStack resourceStack = Questville.getInstance().getItemManager().getResource(item.getReward());
                if (itemStack == null && resourceStack == null) continue;
                Questville.getInstance().getItemManager().privateDrop(p, itemStack == null ? resourceStack : itemStack, entity.getLocation());
            }
        }
    }

    public LivingEntity spawnMob(String mobName, Location loc) {
        CustomMob mob = mobs.get(mobName);
        if (mob == null) {
            Questville.getInstance().getLogger().warning("Invalid mob name " + mobName);
            return null;
        }
        return mob.spawn(loc);
    }

    private void removeEntityFromList(LivingEntity entity) {
        for (List<LivingEntity> mobs : spawnedMobs.values()) {
            mobs.remove(entity);
        }
    }

    public CustomMob getMob(LivingEntity entity) {
        Optional<String> scoreboardTag = entity.getScoreboardTags().stream().filter(t -> t.startsWith("questville_custom_mob:")).findFirst();
        if (scoreboardTag.isEmpty()) return null;
        String mobId = scoreboardTag.get().replace("questville_custom_mob:", "");
        return mobs.get(mobId);
    }

}
