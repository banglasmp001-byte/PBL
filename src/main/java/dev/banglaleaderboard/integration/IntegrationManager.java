package dev.banglaleaderboard.integration;

import dev.banglaleaderboard.BanglaLeaderboard;
import org.bukkit.Bukkit;

/**
 * Checks and tracks optional plugin integrations.
 */
public class IntegrationManager {

    private final BanglaLeaderboard plugin;

    private boolean placeholderAPI;
    private boolean vault;
    private boolean luckPerms;
    private boolean essentials;

    public IntegrationManager(BanglaLeaderboard plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        placeholderAPI = isPluginPresent("PlaceholderAPI");
        vault = isPluginPresent("Vault");
        luckPerms = isPluginPresent("LuckPerms");
        essentials = isPluginPresent("Essentials");

        plugin.getBLLogger().info("Integrations: "
                + "PlaceholderAPI=" + placeholderAPI
                + " | Vault=" + vault
                + " | LuckPerms=" + luckPerms
                + " | Essentials=" + essentials);
    }

    private boolean isPluginPresent(String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null
                && Bukkit.getPluginManager().isPluginEnabled(name);
    }

    public boolean isPlaceholderAPIEnabled() { return placeholderAPI; }
    public boolean isVaultEnabled() { return vault; }
    public boolean isLuckPermsEnabled() { return luckPerms; }
    public boolean isEssentialsEnabled() { return essentials; }
}
