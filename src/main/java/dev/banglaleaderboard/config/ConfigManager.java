package dev.banglaleaderboard.config;

import dev.banglaleaderboard.BanglaLeaderboard;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manages the main config.yml.
 */
public class ConfigManager {

    private final BanglaLeaderboard plugin;
    private FileConfiguration config;

    public ConfigManager(BanglaLeaderboard plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public boolean isDebug() {
        return config.getBoolean("general.debug", false);
    }

    public boolean isCreateExample() {
        return config.getBoolean("general.create-example", true);
    }

    public int getDefaultUpdateInterval() {
        return config.getInt("update.default-interval", 300);
    }

    public boolean isAsyncUpdate() {
        return config.getBoolean("update.async", true);
    }

    public int getBatchSize() {
        return config.getInt("update.batch-size", 10);
    }

    public int getBatchDelay() {
        return config.getInt("update.batch-delay", 1);
    }

    public String getMedal(int rank) {
        return switch (rank) {
            case 1 -> config.getString("display.medal-1", "🥇");
            case 2 -> config.getString("display.medal-2", "🥈");
            case 3 -> config.getString("display.medal-3", "🥉");
            default -> config.getString("display.medal-default", "✦");
        };
    }

    public String getCombinedFormat() {
        return config.getString("display.combined-format", "{medal} {name} - {formatted_value}");
    }

    public boolean isNumberFormat() {
        return config.getBoolean("display.number-format.enabled", true);
    }

    public boolean isUseSuffixes() {
        return config.getBoolean("display.number-format.use-suffixes", true);
    }

    public int getDecimalPlaces() {
        return config.getInt("display.number-format.decimal-places", 2);
    }

    public int getMaxBackups() {
        return config.getInt("backup.max-backups", 10);
    }
}
