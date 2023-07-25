package net.serble.questville.schemas;

import org.bukkit.configuration.ConfigurationSection;

public class ChanceReward {
    private final int chance;
    private final String reward;

    public ChanceReward(int chance, String reward) {
        this.chance = chance;
        this.reward = reward;
    }

    public ChanceReward(ConfigurationSection config) {
        chance = config.getInt("percent");
        reward = config.getString("item");
    }

    public int getChance() {
        return chance;
    }

    public String getReward() {
        return reward;
    }
}
