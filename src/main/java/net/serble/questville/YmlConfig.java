package net.serble.questville;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class YmlConfig {
    private FileConfiguration configuration;
    private final File file;

    public YmlConfig(File file) throws IOException, InvalidConfigurationException {
        this.file = file;
        load(file);
    }

    public YmlConfig(String fileName) throws IOException, InvalidConfigurationException {
        this(new File(Questville.getInstance().getDataFolder(), fileName));
    }

    public void save() throws IOException {
        configuration.save(file);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void load(File file) throws IOException, InvalidConfigurationException {
        Questville.getInstance().getDataFolder().mkdirs();
        file.createNewFile();
        configuration = new YamlConfiguration();
        configuration.load(file);
    }

    public void load(String fileName) throws IOException, InvalidConfigurationException {
        load(new File(Questville.getInstance().getDataFolder(), fileName));
    }

    public void checkOrSet(AtomicBoolean changed, String key, Object value) {
        if (!configuration.isSet(key)) {
            if (value instanceof Map) {
                configuration.createSection(key, (Map<?, ?>) value);
            } else {
                configuration.set(key, value);
            }
            changed.set(true);
        }
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }
}
