package net.serble.questville.schemas.items;

import org.bukkit.configuration.ConfigurationSection;

public class ItemAttributes {
    private final int healthModifier;
    private final int manaModifier;
    private final int defenceModifier;
    private final int damageModifier;
    private final int speedModifier;
    private final int manaRegenModifier;

    public ItemAttributes(int healthModifier, int manaModifier, int defenceModifier, int damageModifier, int speedModifier, int manaRegenModifier) {
        this.healthModifier = healthModifier;
        this.manaModifier = manaModifier;
        this.defenceModifier = defenceModifier;
        this.damageModifier = damageModifier;
        this.speedModifier = speedModifier;
        this.manaRegenModifier = manaRegenModifier;
    }

    public ItemAttributes() {
        this.healthModifier = 0;
        this.manaModifier = 0;
        this.defenceModifier = 0;
        this.damageModifier = 0;
        this.speedModifier = 0;
        this.manaRegenModifier = 0;
    }

    public ItemAttributes(ConfigurationSection config) {
        this.healthModifier = config.getInt("health", 0);
        this.manaModifier = config.getInt("mana", 0);
        this.defenceModifier = config.getInt("defence", 0);
        this.damageModifier = config.getInt("damage", 0);
        this.speedModifier = config.getInt("speed", 0);
        this.manaRegenModifier = config.getInt("mana-regen", 0);
    }

    public ItemAttributes add(ItemAttributes other) {
        return new ItemAttributes(
                this.healthModifier + other.healthModifier,
                this.manaModifier + other.manaModifier,
                this.defenceModifier + other.defenceModifier,
                this.damageModifier + other.damageModifier,
                this.speedModifier + other.speedModifier,
                this.manaRegenModifier + other.manaRegenModifier
        );
    }

    public int getHealthModifier() {
        return healthModifier;
    }

    public int getManaModifier() {
        return manaModifier;
    }

    public int getDefenceModifier() {
        return defenceModifier;
    }

    public int getDamageModifier() {
        return damageModifier;
    }

    public int getSpeedModifier() {
        return speedModifier;
    }

    public int getManaRegenModifier() {
        return manaRegenModifier;
    }

}
