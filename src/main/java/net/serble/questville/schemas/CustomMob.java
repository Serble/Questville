package net.serble.questville.schemas;

import net.serble.questville.Questville;
import net.serble.questville.Utils;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Objects;

public class CustomMob {
    private final String id;
    private final EntityType entityType;
    private final String name;
    private final int health;
    private final int damage;
    private final String weapon;
    private final String helmet;
    private final String chestplate;
    private final String leggings;
    private final String boots;
    private final int moneyReward;
    private final String[] drops;
    private final ChanceReward[] chanceRewards;

    public CustomMob(ConfigurationSection config) {
        id = Objects.requireNonNull(config.getName());
        entityType = EntityType.valueOf(config.getString("type"));
        name = config.getString("name");
        health = config.getInt("health", 20);
        damage = config.getInt("damage", 1);
        weapon = config.getString("weapon", "none");
        helmet = config.getString("helmet", "none");
        chestplate = config.getString("chestplate", "none");
        leggings = config.getString("leggings", "none");
        boots = config.getString("boots", "none");
        moneyReward = config.getInt("rewards.money", 0);
        drops = config.getStringList("rewards.items").toArray(new String[0]);

        ConfigurationSection chanceRewardsSection = config.getConfigurationSection("rewards.chance");
        if (chanceRewardsSection != null) {
            chanceRewards = new ChanceReward[chanceRewardsSection.getKeys(false).size()];
            int i = 0;
            for (String key : chanceRewardsSection.getKeys(false)) {
                ConfigurationSection chanceRewardSection = chanceRewardsSection.getConfigurationSection(key);
                assert chanceRewardSection != null;
                chanceRewards[i] = new ChanceReward(chanceRewardSection);
                i++;
            }
        } else {
            chanceRewards = new ChanceReward[0];
        }
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public String getWeapon() {
        return weapon;
    }

    public String getHelmet() {
        return helmet;
    }

    public String getChestplate() {
        return chestplate;
    }

    public String getLeggings() {
        return leggings;
    }

    public String getBoots() {
        return boots;
    }

    public int getMoneyReward() {
        return moneyReward;
    }

    public String[] getDrops() {
        return drops;
    }

    public ChanceReward[] getChanceRewards() {
        return chanceRewards;
    }

    public LivingEntity spawn(Location loc) {
        assert loc.getWorld() != null;
        LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, entityType);

        AttributeInstance maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(health);
        }

        entity.setCustomName(Utils.t(name));
        entity.setCustomNameVisible(true);
        entity.setHealth(health);
        entity.setRemoveWhenFarAway(false);

        assert entity.getEquipment() != null;
        if (!Objects.equals(weapon, "none")) {
            entity.getEquipment().setItemInMainHand(Questville.getInstance().getItemManager().createFakeItem(null, weapon));
        }
        entity.getEquipment().setHelmet(Questville.getInstance().getItemManager().createFakeItem(null, helmet));
        entity.getEquipment().setChestplate(Questville.getInstance().getItemManager().createFakeItem(null, chestplate));
        entity.getEquipment().setLeggings(Questville.getInstance().getItemManager().createFakeItem(null, leggings));
        entity.getEquipment().setBoots(Questville.getInstance().getItemManager().createFakeItem(null, boots));

        entity.addScoreboardTag("questville_custom_mob:" + id);
        return entity;
    }

}
