package net.serble.questville.schemas;

import net.serble.questville.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Objects;

public class MobSpawnArea {
    private final String id;
    private final List<String> mobs;
    private final Location pos1;
    private final Location pos2;
    private final int maxMobs;

    public MobSpawnArea(ConfigurationSection config) {
        id = Objects.requireNonNull(config.getName());
        mobs = config.getStringList("mobs");
        String worldName = config.getString("world");
        pos1 = getLocationFromString(Objects.requireNonNull(config.getString("pos1")), worldName);
        pos2 = getLocationFromString(Objects.requireNonNull(config.getString("pos2")), worldName);
        maxMobs = config.getInt("max-mobs");
    }

    private Location getLocationFromString(String loc, String world) {
        String[] locSplit = loc.split(",");
        return new Location(Bukkit.getWorld(world), Double.parseDouble(locSplit[0]), Double.parseDouble(locSplit[1]), Double.parseDouble(locSplit[2]));
    }

    public String getId() {
        return id;
    }

    public List<String> getMobs() {
        return mobs;
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public int getMaxMobs() {
        return maxMobs;
    }

    public boolean isInside(Location loc) {
        return Utils.isInside(pos1, pos2, loc);
    }

    public Location getRandomLocation() {
        return Utils.getRandomLocation(pos1, pos2);
    }
}
