package net.serble.questville;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;
import java.util.UUID;

public class Utils {

    public static String t(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void hideFlags(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
    }

    public static boolean isInside(Location pos1, Location pos2, Location loc) {
        Location topCorner = new Location(pos1.getWorld(), Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
        Location bottomCorner = new Location(pos1.getWorld(), Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
        return loc.getX() <= topCorner.getX() && loc.getX() >= bottomCorner.getX() && loc.getY() <= topCorner.getY() && loc.getY() >= bottomCorner.getY() && loc.getZ() <= topCorner.getZ() && loc.getZ() >= bottomCorner.getZ();
    }

    public static UUID getEffectiveUuid(Player player) {
        return player.getUniqueId();
    }

    public static Location getRandomLocation(Location pos1, Location pos2) {
        Random random = new Random();

        int x1 = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int y1 = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int z1 = Math.min(pos1.getBlockZ(), pos2.getBlockZ());

        int x2 = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int y2 = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int z2 = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        int maxLoops = 500;
        while (true) {
            maxLoops--;
            if (maxLoops == 0) {
                Bukkit.getLogger().severe("Failed to find a random location in " + pos1 + " and " + pos2);
            }

            int x = x1 + random.nextInt(x2 - x1 + 1);
            int y = y1 + random.nextInt(y2 - y1 + 1);
            int z = z1 + random.nextInt(z2 - z1 + 1);

            if (y == 0) continue;

            Location loc = new Location(pos1.getWorld(), x, y, z);

            Block block = loc.getBlock();
            Block blockBelow = loc.clone().add(0, -1, 0).getBlock();

            if (block.getType() == Material.AIR && blockBelow.getType().isSolid()) {
                return loc;
            }
        }
    }

    public static String toRomanNumeral(int number) {
        if (number < 1 || number > 3999) {
            throw new IllegalArgumentException(number + " is not in range (1,3999)");
        }
        StringBuilder sb = new StringBuilder();
        while (number >= 1000) {
            sb.append("M");
            number -= 1000;        }
        while (number >= 900) {
            sb.append("CM");
            number -= 900;
        }
        while (number >= 500) {
            sb.append("D");
            number -= 500;
        }
        while (number >= 400) {
            sb.append("CD");
            number -= 400;
        }
        while (number >= 100) {
            sb.append("C");
            number -= 100;
        }
        while (number >= 90) {
            sb.append("XC");
            number -= 90;
        }
        while (number >= 50) {
            sb.append("L");
            number -= 50;
        }
        while (number >= 40) {
            sb.append("XL");
            number -= 40;
        }
        while (number >= 10) {
            sb.append("X");
            number -= 10;
        }
        while (number >= 9) {
            sb.append("IX");
            number -= 9;
        }
        while (number >= 5) {
            sb.append("V");
            number -= 5;
        }
        while (number >= 4) {
            sb.append("IV");
            number -= 4;
        }
        while (number >= 1) {
            sb.append("I");
            number -= 1;
        }
        return sb.toString();
    }

}
