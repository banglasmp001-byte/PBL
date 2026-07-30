package dev.banglaleaderboard.model;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single leaderboard definition.
 */
public class Leaderboard {

    private final String name;
    private String displayName;
    private String placeholder;
    private SortOrder sortOrder;
    private int updateInterval;
    private boolean cacheEnabled;
    private int cacheDuration;
    private int topSize;
    private boolean numberFormat;
    private boolean useSuffixes;
    private int decimalPlaces;
    private String permission;
    private List<String> worldFilter;
    private boolean refreshOnJoin;
    private boolean refreshOnQuit;
    private boolean refreshOnPlaceholderChange;
    private String combinedFormat;
    private boolean enabled;

    // Runtime data
    private final List<LeaderboardEntry> entries;
    private long lastUpdateTime;
    private final File configFile;

    public Leaderboard(String name, File configFile) {
        this.name = name;
        this.configFile = configFile;
        this.entries = new ArrayList<>();
        this.lastUpdateTime = 0;

        // Defaults
        this.displayName = name;
        this.placeholder = "";
        this.sortOrder = SortOrder.DESC;
        this.updateInterval = 300;
        this.cacheEnabled = true;
        this.cacheDuration = 300;
        this.topSize = 10;
        this.numberFormat = true;
        this.useSuffixes = true;
        this.decimalPlaces = 2;
        this.permission = "";
        this.worldFilter = new ArrayList<>();
        this.refreshOnJoin = false;
        this.refreshOnQuit = false;
        this.refreshOnPlaceholderChange = false;
        this.combinedFormat = "{medal} {name} - {formatted_value}";
        this.enabled = true;
    }

    /**
     * Load leaderboard data from a YAML config.
     */
    public void loadFromConfig(YamlConfiguration config) {
        this.displayName = config.getString("display-name", name);
        this.placeholder = config.getString("placeholder", "");
        this.sortOrder = SortOrder.fromString(config.getString("sort", "DESC"));
        this.updateInterval = config.getInt("update-interval", 300);
        this.cacheEnabled = config.getBoolean("cache.enabled", true);
        this.cacheDuration = config.getInt("cache.duration", 300);
        this.topSize = Math.max(1, Math.min(config.getInt("top-size", 10), 100));
        this.numberFormat = config.getBoolean("number-format.enabled", true);
        this.useSuffixes = config.getBoolean("number-format.use-suffixes", true);
        this.decimalPlaces = config.getInt("number-format.decimal-places", 2);
        this.permission = config.getString("permission", "");
        this.worldFilter = config.getStringList("world-filter");
        this.refreshOnJoin = config.getBoolean("refresh-on-join", false);
        this.refreshOnQuit = config.getBoolean("refresh-on-quit", false);
        this.refreshOnPlaceholderChange = config.getBoolean("refresh-on-placeholder-change", false);
        this.combinedFormat = config.getString("combined-format", "{medal} {name} - {formatted_value}");
        this.enabled = config.getBoolean("enabled", true);
    }

    /**
     * Save leaderboard data to YAML config.
     */
    public void saveToConfig(YamlConfiguration config) {
        config.set("name", name);
        config.set("enabled", enabled);
        config.set("display-name", displayName);
        config.set("placeholder", placeholder);
        config.set("sort", sortOrder.name());
        config.set("update-interval", updateInterval);
        config.set("cache.enabled", cacheEnabled);
        config.set("cache.duration", cacheDuration);
        config.set("top-size", topSize);
        config.set("number-format.enabled", numberFormat);
        config.set("number-format.use-suffixes", useSuffixes);
        config.set("number-format.decimal-places", decimalPlaces);
        config.set("permission", permission);
        config.set("world-filter", worldFilter);
        config.set("refresh-on-join", refreshOnJoin);
        config.set("refresh-on-quit", refreshOnQuit);
        config.set("refresh-on-placeholder-change", refreshOnPlaceholderChange);
        config.set("combined-format", combinedFormat);
    }

    // ==========================================
    // ENTRY MANAGEMENT
    // ==========================================

    public synchronized void updateEntries(List<LeaderboardEntry> newEntries) {
        this.entries.clear();
        this.entries.addAll(newEntries);
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public synchronized List<LeaderboardEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public synchronized LeaderboardEntry getEntry(int position) {
        if (position < 1 || position > entries.size()) return null;
        return entries.get(position - 1);
    }

    public synchronized int getPlayerRank(String playerName) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getPlayerName().equalsIgnoreCase(playerName)) {
                return i + 1;
            }
        }
        return -1;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    public String getName() { return name; }
    public File getConfigFile() { return configFile; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }

    public SortOrder getSortOrder() { return sortOrder; }
    public void setSortOrder(SortOrder sortOrder) { this.sortOrder = sortOrder; }

    public int getUpdateInterval() { return updateInterval; }
    public void setUpdateInterval(int updateInterval) { this.updateInterval = updateInterval; }

    public boolean isCacheEnabled() { return cacheEnabled; }
    public int getCacheDuration() { return cacheDuration; }

    public int getTopSize() { return topSize; }
    public void setTopSize(int topSize) { this.topSize = topSize; }

    public boolean isNumberFormat() { return numberFormat; }
    public boolean isUseSuffixes() { return useSuffixes; }
    public int getDecimalPlaces() { return decimalPlaces; }

    public String getPermission() { return permission; }

    public List<String> getWorldFilter() { return Collections.unmodifiableList(worldFilter); }

    public boolean isRefreshOnJoin() { return refreshOnJoin; }
    public boolean isRefreshOnQuit() { return refreshOnQuit; }
    public boolean isRefreshOnPlaceholderChange() { return refreshOnPlaceholderChange; }

    public String getCombinedFormat() { return combinedFormat; }
    public void setCombinedFormat(String combinedFormat) { this.combinedFormat = combinedFormat; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getLastUpdateTime() { return lastUpdateTime; }

    public boolean needsRefresh() {
        if (updateInterval <= 0) return false;
        return System.currentTimeMillis() - lastUpdateTime >= (long) updateInterval * 1000;
    }

    public enum SortOrder {
        ASC, DESC;

        public static SortOrder fromString(String s) {
            try {
                return valueOf(s.toUpperCase());
            } catch (Exception e) {
                return DESC;
            }
        }
    }
}
