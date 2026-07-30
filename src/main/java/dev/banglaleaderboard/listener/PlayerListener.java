package dev.banglaleaderboard.listener;

import dev.banglaleaderboard.BanglaLeaderboard;
import dev.banglaleaderboard.model.Leaderboard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join/quit events for leaderboard refresh triggers.
 */
public class PlayerListener implements Listener {

    private final BanglaLeaderboard plugin;

    public PlayerListener(BanglaLeaderboard plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Leaderboard lb : plugin.getLeaderboardManager().getAllLeaderboards()) {
            if (lb.isEnabled() && lb.isRefreshOnJoin()) {
                plugin.getRefreshService().refreshAsync(lb);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (Leaderboard lb : plugin.getLeaderboardManager().getAllLeaderboards()) {
            if (lb.isEnabled() && lb.isRefreshOnQuit()) {
                // Slight delay to ensure player data is saved by other plugins first
                plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                    plugin.getRefreshService().refreshAsync(lb);
                }, 40L);
            }
        }
    }
}
