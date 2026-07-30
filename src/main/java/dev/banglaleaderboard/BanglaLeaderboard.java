package dev.banglaleaderboard;

import dev.banglaleaderboard.api.BanglaLeaderboardAPI;
import dev.banglaleaderboard.cache.CacheManager;
import dev.banglaleaderboard.command.BLCommand;
import dev.banglaleaderboard.config.ConfigManager;
import dev.banglaleaderboard.config.MessageManager;
import dev.banglaleaderboard.expansion.BLExpansion;
import dev.banglaleaderboard.integration.IntegrationManager;
import dev.banglaleaderboard.listener.PlayerListener;
import dev.banglaleaderboard.manager.LeaderboardManager;
import dev.banglaleaderboard.service.RefreshService;
import dev.banglaleaderboard.storage.StorageManager;
import dev.banglaleaderboard.util.BLLogger;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * BanglaLeaderboard - Production-grade PlaceholderAPI-powered leaderboard plugin.
 * <p>
 * Author: NoTXGameR | UBMC STUDIO
 * Compatible: Paper 1.21 - 1.21.11
 */
public final class BanglaLeaderboard extends JavaPlugin {

    private static BanglaLeaderboard instance;

    // Core managers
    private ConfigManager configManager;
    private MessageManager messageManager;
    private StorageManager storageManager;
    private CacheManager cacheManager;
    private LeaderboardManager leaderboardManager;
    private RefreshService refreshService;
    private IntegrationManager integrationManager;
    private BLLogger blLogger;

    // Public API
    private BanglaLeaderboardAPI api;

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();

        // Initialize logger first
        blLogger = new BLLogger(this);
        blLogger.info("Starting BanglaLeaderboard v" + getDescription().getVersion() + "...");

        // Save default resources
        saveDefaultConfigs();

        // Initialize core systems
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        storageManager = new StorageManager(this);
        cacheManager = new CacheManager(this);

        // Initialize integration manager (checks for optional plugins)
        integrationManager = new IntegrationManager(this);
        integrationManager.setup();

        // Initialize leaderboard manager
        leaderboardManager = new LeaderboardManager(this);
        leaderboardManager.loadAll();

        // Initialize refresh service
        refreshService = new RefreshService(this);
        refreshService.start();

        // Register PlaceholderAPI expansion
        if (integrationManager.isPlaceholderAPIEnabled()) {
            new BLExpansion(this).register();
            blLogger.info("PlaceholderAPI expansion registered.");
        } else {
            blLogger.warn("PlaceholderAPI not found! Placeholders will not work.");
        }

        // Register commands
        BLCommand blCommand = new BLCommand(this);
        Objects.requireNonNull(getCommand("bl")).setExecutor(blCommand);
        Objects.requireNonNull(getCommand("bl")).setTabCompleter(blCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Initialize public API
        api = new BanglaLeaderboardAPI(this);

        long elapsed = System.currentTimeMillis() - startTime;
        blLogger.info("BanglaLeaderboard enabled in " + elapsed + "ms with "
                + leaderboardManager.getLeaderboardCount() + " leaderboard(s).");
    }

    @Override
    public void onDisable() {
        blLogger.info("Shutting down BanglaLeaderboard...");

        // Stop refresh service
        if (refreshService != null) {
            refreshService.stop();
        }

        // Auto backup if enabled
        if (configManager != null && configManager.getConfig().getBoolean("backup.auto-backup", true)) {
            storageManager.createBackup();
        }

        // Save cache to disk
        if (cacheManager != null) {
            cacheManager.saveAll();
        }

        // Unregister PlaceholderAPI expansion
        if (integrationManager != null && integrationManager.isPlaceholderAPIEnabled()) {
            me.clip.placeholderapi.expansion.PlaceholderExpansion expansion =
                    me.clip.placeholderapi.PlaceholderAPI.getRegisteredExpansion("bl");
            if (expansion != null) {
                expansion.unregister();
            }
        }

        blLogger.info("BanglaLeaderboard disabled. Goodbye!");
    }

    /**
     * Reload the entire plugin.
     */
    public void reload() {
        blLogger.info("Reloading BanglaLeaderboard...");

        // Stop refresh service
        if (refreshService != null) refreshService.stop();

        // Reload configs
        configManager.reload();
        messageManager.reload();

        // Reload leaderboards
        leaderboardManager.reloadAll();

        // Restart refresh service
        refreshService = new RefreshService(this);
        refreshService.start();

        blLogger.info("Reload complete.");
    }

    // ==========================================
    // GETTERS
    // ==========================================

    public static BanglaLeaderboard getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public RefreshService getRefreshService() {
        return refreshService;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public BanglaLeaderboardAPI getApi() {
        return api;
    }

    public BLLogger getBLLogger() {
        return blLogger;
    }

    // ==========================================
    // PRIVATE HELPERS
    // ==========================================

    private void saveDefaultConfigs() {
        saveDefaultConfig();
        saveResource("messages.yml", false);
        saveResource("settings.yml", false);
    }
}
