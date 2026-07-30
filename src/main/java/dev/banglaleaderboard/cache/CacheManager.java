package dev.banglaleaderboard.cache;

import dev.banglaleaderboard.BanglaLeaderboard;
import dev.banglaleaderboard.model.LeaderboardEntry;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory and disk-persistent cache for leaderboard entries.
 */
public class CacheManager {

    private final BanglaLeaderboard plugin;
    private final File cacheDir;

    // leaderboard name -> entries
    private final Map<String, List<LeaderboardEntry>> cache = new ConcurrentHashMap<>();

    public CacheManager(BanglaLeaderboard plugin) {
        this.plugin = plugin;
        this.cacheDir = new File(plugin.getDataFolder(), "cache");
        if (!cacheDir.exists()) cacheDir.mkdirs();
        loadAll();
    }

    /**
     * Update cache for a leaderboard.
     */
    public void updateCache(String name, List<LeaderboardEntry> entries) {
        cache.put(name.toLowerCase(), new ArrayList<>(entries));
    }

    /**
     * Get cached entries for a leaderboard.
     */
    public List<LeaderboardEntry> getCached(String name) {
        return Collections.unmodifiableList(
                cache.getOrDefault(name.toLowerCase(), Collections.emptyList())
        );
    }

    /**
     * Clear cache for a specific leaderboard.
     */
    public void clearCache(String name) {
        cache.remove(name.toLowerCase());
    }

    /**
     * Clear all caches.
     */
    public void clearAll() {
        cache.clear();
    }

    /**
     * Save all cache entries to disk (called on shutdown).
     */
    public void saveAll() {
        for (Map.Entry<String, List<LeaderboardEntry>> entry : cache.entrySet()) {
            saveToDisk(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Load all cache files from disk.
     */
    private void loadAll() {
        File[] files = cacheDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String name = file.getName().replace(".yml", "");
            List<LeaderboardEntry> entries = loadFromDisk(file);
            if (!entries.isEmpty()) {
                cache.put(name, entries);
            }
        }
    }

    private void saveToDisk(String name, List<LeaderboardEntry> entries) {
        File file = new File(cacheDir, name + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry e = entries.get(i);
            String path = "entries." + i;
            config.set(path + ".rank", e.getRank());
            config.set(path + ".player", e.getPlayerName());
            config.set(path + ".value", e.getValue());
            config.set(path + ".raw", e.getRawValue());
        }

        try {
            config.save(file);
        } catch (IOException ignored) {}
    }

    private List<LeaderboardEntry> loadFromDisk(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<LeaderboardEntry> entries = new ArrayList<>();

        if (!config.contains("entries")) return entries;

        for (String key : Objects.requireNonNull(config.getConfigurationSection("entries")).getKeys(false)) {
            String path = "entries." + key;
            int rank = config.getInt(path + ".rank");
            String player = config.getString(path + ".player", "Unknown");
            double value = config.getDouble(path + ".value");
            String raw = config.getString(path + ".raw", "");
            entries.add(new LeaderboardEntry(rank, player, value, raw));
        }

        return entries;
    }
}
