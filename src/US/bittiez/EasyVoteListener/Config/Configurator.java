package US.bittiez.EasyVoteListener.Config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Configurator {
    public FileConfiguration config;

    public Configurator(FileConfiguration config) {
        setConfig(config);
    }

    public Configurator() {}

    public void setConfig(FileConfiguration config) {
        this.config = config;
    }

    public void setConfig(Plugin plugin) {
        config = plugin.getConfig();
    }

    public void saveDefaultConfig(Plugin plugin) {
        plugin.saveDefaultConfig();
    }

    public void reloadPluginDefaultConfig(Plugin plugin) {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
}
