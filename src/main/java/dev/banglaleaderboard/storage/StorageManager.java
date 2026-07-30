package dev.banglaleaderboard.storage;

import dev.banglaleaderboard.BanglaLeaderboard;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * Handles backup, restore, import, and export of leaderboard configurations.
 */
public class StorageManager {

    private final BanglaLeaderboard plugin;
    private final File backupsDir;

    public StorageManager(BanglaLeaderboard plugin) {
        this.plugin = plugin;
        this.backupsDir = new File(plugin.getDataFolder(), "backups");
        if (!backupsDir.exists()) backupsDir.mkdirs();
    }

    /**
     * Create a backup of all leaderboard YAML files.
     * @return The backup file name, or null on failure.
     */
    public String createBackup() {
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File backupDir = new File(backupsDir, "backup_" + timestamp);
            backupDir.mkdirs();

            File leaderboardsDir = plugin.getLeaderboardManager().getLeaderboardsDir();
            File[] files = leaderboardsDir.listFiles((d, n) -> n.endsWith(".yml"));

            if (files != null) {
                for (File file : files) {
                    Files.copy(file.toPath(), new File(backupDir, file.getName()).toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // Prune old backups
            pruneOldBackups();

            plugin.getBLLogger().info("Backup created: backup_" + timestamp);
            return "backup_" + timestamp;
        } catch (IOException e) {
            plugin.getBLLogger().warn("Backup failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Restore leaderboards from a backup.
     * @param backupName The name of the backup directory
     * @return true if success
     */
    public boolean restore(String backupName) {
        File backupDir = new File(backupsDir, backupName);
        if (!backupDir.exists() || !backupDir.isDirectory()) return false;

        try {
            File leaderboardsDir = plugin.getLeaderboardManager().getLeaderboardsDir();
            File[] files = backupDir.listFiles((d, n) -> n.endsWith(".yml"));
            if (files == null) return false;

            for (File file : files) {
                Files.copy(file.toPath(), new File(leaderboardsDir, file.getName()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            plugin.getLeaderboardManager().reloadAll();
            return true;
        } catch (IOException e) {
            plugin.getBLLogger().warn("Restore failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Export all leaderboard configs to a single YAML file.
     * @return The export file name, or null on failure
     */
    public String export() {
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File exportFile = new File(plugin.getDataFolder(), "export_" + timestamp + ".yml");

            YamlConfiguration export = new YamlConfiguration();
            File leaderboardsDir = plugin.getLeaderboardManager().getLeaderboardsDir();
            File[] files = leaderboardsDir.listFiles((d, n) -> n.endsWith(".yml"));

            if (files != null) {
                for (File file : files) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    String name = file.getName().replace(".yml", "");
                    export.set("leaderboards." + name, config.getValues(true));
                }
            }

            export.save(exportFile);
            return exportFile.getName();
        } catch (IOException e) {
            plugin.getBLLogger().warn("Export failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Import leaderboards from a YAML export file.
     * @param fileName File name inside plugin data folder
     * @return Number of imported leaderboards, or -1 on failure
     */
    public int importFrom(String fileName) {
        File importFile = new File(plugin.getDataFolder(), fileName);
        if (!importFile.exists()) return -1;

        try {
            YamlConfiguration importData = YamlConfiguration.loadConfiguration(importFile);
            if (!importData.contains("leaderboards")) return 0;

            File leaderboardsDir = plugin.getLeaderboardManager().getLeaderboardsDir();
            int count = 0;

            for (String name : importData.getConfigurationSection("leaderboards").getKeys(false)) {
                YamlConfiguration config = new YamlConfiguration();
                Object section = importData.get("leaderboards." + name);
                // Copy values
                if (section instanceof org.bukkit.configuration.ConfigurationSection cs) {
                    for (String key : cs.getKeys(true)) {
                        config.set(key, cs.get(key));
                    }
                }
                File outFile = new File(leaderboardsDir, name + ".yml");
                config.save(outFile);
                count++;
            }

            plugin.getLeaderboardManager().reloadAll();
            return count;
        } catch (Exception e) {
            plugin.getBLLogger().warn("Import failed: " + e.getMessage());
            return -1;
        }
    }

    private void pruneOldBackups() {
        int maxBackups = plugin.getConfigManager().getMaxBackups();
        File[] backups = backupsDir.listFiles(File::isDirectory);
        if (backups == null || backups.length <= maxBackups) return;

        Arrays.sort(backups, Comparator.comparingLong(File::lastModified));
        for (int i = 0; i < backups.length - maxBackups; i++) {
            deleteDirectory(backups[i]);
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
        dir.delete();
    }
}
