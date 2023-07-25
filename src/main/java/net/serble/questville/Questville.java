package net.serble.questville;

import net.serble.questville.enchants.CustomEnchants;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Questville extends JavaPlugin {
    private static Questville instance;
    private YmlConfig config;
    private ItemManager itemManager;
    private NpcManager npcManager;
    private MiningManager miningManager;
    private BankManager bankManager;
    private MobManager mobManager;
    private CraftingManager craftingManager;
    private CustomEnchants customEnchants;
    private PlayerPropertyManager playerPropertyManager;

    public static Questville getInstance() {
        return instance;
    }

    public YmlConfig getYmlConfig() {
        return config;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public NpcManager getNpcManager() {
        return npcManager;
    }

    public MiningManager getMiningManager() {
        return miningManager;
    }

    public BankManager getBankManager() {
        return bankManager;
    }

    public MobManager getMobManager() {
        return mobManager;
    }

    public CraftingManager getCraftingManager() {
        return craftingManager;
    }

    public CustomEnchants getCustomEnchantsManager() {
        return customEnchants;
    }

    public PlayerPropertyManager getPlayerPropertyManager() {
        return playerPropertyManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            config = new YmlConfig("config.yml");
        } catch (Exception e) {
            disableDueToError(e);
            return;
        }
        saveConfigDefaults();

        customEnchants = new CustomEnchants();
        itemManager = new ItemManager();
        npcManager = new NpcManager();
        miningManager = new MiningManager();
        bankManager = new BankManager();
        mobManager = new MobManager();
        craftingManager = new CraftingManager();
        playerPropertyManager = new PlayerPropertyManager();

        new QuestvilleCommand();
        new DeathEvents();
        new PlayerManagerItem();

        getLogger().info("Questville enabled!");
    }

    public void disableDueToError(Exception e) {
        getLogger().severe("Questville disabled due to error:");
        e.printStackTrace();
        getServer().getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        itemManager.saveEditions();
        mobManager.purgeMobs();
        getLogger().info("Questville disabled!");
    }

    private void saveConfigDefaults() {
        saveResource("config.yml", false);
        AtomicBoolean changed = new AtomicBoolean(false);

        config.checkOrSet(changed, "enabled-worlds.worlds", new String[] {"questville"});
        config.checkOrSet(changed, "enabled-worlds.enabled", true);
        config.checkOrSet(changed, "enabled-worlds.is-blacklist", false);

        config.checkOrSet(changed, "death-actions.money-loss", 0);
        config.checkOrSet(changed, "death-actions.money-percent-loss", 0);
        config.checkOrSet(changed, "death-actions.resource-percent-loss", 0);

        if (changed.get()) {
            try {
                config.save();
            } catch (IOException e) {
                disableDueToError(e);
            }
        }
    }

    public static boolean enabledIn(String world) {
        return instance.getYmlConfig().getConfiguration().getStringList("enabled-worlds.worlds").contains(world);
    }

    public static boolean enabledIn(World world) {
        return enabledIn(world.getName());
    }

    public static boolean enabledIn(Player p) {
        return enabledIn(p.getWorld());
    }

}
