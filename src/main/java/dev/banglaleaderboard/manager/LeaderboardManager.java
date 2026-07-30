package dev.banglaleaderboard.manager;

import dev.banglaleaderboard.BanglaLeaderboard;
import dev.banglaleaderboard.model.Leaderboard;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all leaderboard instances.
 * Thread-safe, uses ConcurrentHashMap.
 */
public class LeaderboardManager {

    private final BanglaLeaderboard plugin;
    private final Map<String, Leaderboard> leaderboards = new ConcurrentHashMap<>();
    private File leaderboardsDir;

    public LeaderboardManager(BanglaLeaderboard plugin) {
        this.plugin = plugin;
    }

    /**
     * Load all leaderboard YAML files from the leaderboards/ directory.
     */
    public void loadAll() {
        leaderboards.clear();

        leaderboardsDir = new File(plugin.getDataFolder(), "leaderboards");
        if (!leaderboardsDir.exists()) {
            leaderboardsDir.mkdirs();
        }

        // Copy example leaderboard if directory is empty and option is enabled
        if (plugin.getConfigManager().isCreateExample()) {
            File exampleFile = new File(leaderboardsDir, "balance.yml");
            if (!exampleFile.exists()) {
                plugin.saveResource("leaderboards/balance.yml", false);
            }
        }

        File[] files = leaderboardsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getBLLogger().info("No leaderboards found.");
            return;
        }

        for (File file : files) {
            loadFromFile(file);
        }

        plugin.getBLLogger().info("Loaded " + leaderboards.size() + " leaderboard(s).");
    }

    /**
     * Reload all leaderboards.
     */
    public void reloadAll() {
        loadAll();
    }

    /**
     * Load a single leaderboard from a file.
     */
    public void loadFromFile(File file) {
        String name = file.getName().replace(".yml", "").toLowerCase();
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Leaderboard lb = new Leaderboard(name, file);
            lb.loadFromConfig(config);
            leaderboards.put(name, lb);

            if (plugin.getConfigManager().isDebug()) {
                plugin.getBLLogger().info("Loaded leaderboard: " + name);
            }
        } catch (Exception e) {
            plugin.getBLLogger().warn("Failed to load leaderboard: " + name + " - " + e.getMessage());
        }
    }

    /**
     * Create a new leaderboard.
     * @return true if created, false if already exists
     */
    public boolean create(String name) {
        if (leaderboards.containsKey(name.toLowerCase())) return false;

        File file = new File(leaderboardsDir, name.toLowerCase() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        Leaderboard lb = new Leaderboard(name.toLowerCase(), file);
        lb.setEnabled(true);
        lb.setPlaceholder("%vault_eco_balance%");
        lb.saveToConfig(config);

        try {
            config.save(file);
            leaderboards.put(name.toLowerCase(), lb);
            return true;
        } catch (IOException e) {
            plugin.getBLLogger().warn("Failed to create leaderboard file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a leaderboard.
     * @return true if deleted, false if not found
     */
    public boolean delete(String name) {
        Leaderboard lb = leaderboards.remove(name.toLowerCase());
        if (lb == null) return false;
        return lb.getConfigFile().delete();
    }

    /**
     * Rename a leaderboard.
     */
    public boolean rename(String oldName, String newName) {
        Leaderboard lb = leaderboards.get(oldName.toLowerCase());
        if (lb == null) return false;
        if (leaderboards.containsKey(newName.toLowerCase())) return false;

        File newFile = new File(leaderboardsDir, newName.toLowerCase() + ".yml");
        boolean renamed = lb.getConfigFile().renameTo(newFile);
        if (!renamed) return false;

        leaderboards.remove(oldName.toLowerCase());
        loadFromFile(newFile);
        return true;
    }

    /**
     * Enable a leaderboard.
     */
    public boolean setEnabled(String name, boolean enabled) {
        Leaderboard lb = leaderboards.get(name.toLowerCase());
        if (lb == null) return false;
        lb.setEnabled(enabled);
        saveLeaderboard(lb);
        return true;
    }

    /**
     * Save a leaderboard's config to disk.
     */
    public void saveLeaderboard(Leaderboard lb) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(lb.getConfigFile());
        lb.saveToConfig(config);
        try {
            config.save(lb.getConfigFile());
        } catch (IOException e) {
            plugin.getBLLogger().warn("Failed to save leaderboard: " + lb.getName());
        }
    }

    // ==========================================
    // GETTERS
    // ==========================================

    public Leaderboard getLeaderboard(String name) {
        return leaderboards.get(name.toLowerCase());
    }

    public boolean exists(String name) {
        return leaderboards.containsKey(name.toLowerCase());
    }

    public Collection<Leaderboard> getAllLeaderboards() {
        return Collections.unmodifiableCollection(leaderboards.values());
    }

    public List<String> getLeaderboardNames() {
        return new ArrayList<>(leaderboards.keySet());
    }

    public int getLeaderboardCount() {
        return leaderboards.size();
    }

    public File getLeaderboardsDir() {
        return leaderboardsDir;
    }
}
