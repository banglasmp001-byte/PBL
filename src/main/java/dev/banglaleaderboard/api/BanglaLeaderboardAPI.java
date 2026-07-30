package dev.banglaleaderboard.api;

import dev.banglaleaderboard.BanglaLeaderboard;
import dev.banglaleaderboard.model.Leaderboard;
import dev.banglaleaderboard.model.LeaderboardEntry;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Public API for BanglaLeaderboard.
 * Other plugins can depend on this to interact with leaderboards programmatically.
 *
 * <pre>
 * Usage example:
 *   BanglaLeaderboardAPI api = BanglaLeaderboard.getInstance().getApi();
 *   List<LeaderboardEntry> top10 = api.getEntries("balance");
 * </pre>
 */
public class BanglaLeaderboardAPI {

    private final BanglaLeaderboard plugin;

    public BanglaLeaderboardAPI(BanglaLeaderboard plugin) {
        this.plugin = plugin;
    }

    /**
     * Get all leaderboards.
     */
    public Collection<Leaderboard> getLeaderboards() {
        return plugin.getLeaderboardManager().getAllLeaderboards();
    }

    /**
     * Get a leaderboard by name.
     */
    public Leaderboard getLeaderboard(String name) {
        return plugin.getLeaderboardManager().getLeaderboard(name);
    }

    /**
     * Check if a leaderboard exists.
     */
    public boolean hasLeaderboard(String name) {
        return plugin.getLeaderboardManager().exists(name);
    }

    /**
     * Get top entries for a leaderboard.
     */
    public List<LeaderboardEntry> getEntries(String name) {
        Leaderboard lb = getLeaderboard(name);
        if (lb == null) return List.of();
        return lb.getEntries();
    }

    /**
     * Get a single entry by position (1-indexed).
     */
    public LeaderboardEntry getEntry(String leaderboardName, int position) {
        Leaderboard lb = getLeaderboard(leaderboardName);
        if (lb == null) return null;
        return lb.getEntry(position);
    }

    /**
     * Get a player's rank in a leaderboard.
     * @return rank (1-indexed), or -1 if not in leaderboard
     */
    public int getPlayerRank(String leaderboardName, String playerName) {
        Leaderboard lb = getLeaderboard(leaderboardName);
        if (lb == null) return -1;
        return lb.getPlayerRank(playerName);
    }

    /**
     * Force refresh a leaderboard asynchronously.
     */
    public CompletableFuture<Void> refreshLeaderboard(String name) {
        Leaderboard lb = getLeaderboard(name);
        if (lb == null) return CompletableFuture.completedFuture(null);
        return plugin.getRefreshService().refreshAsync(lb);
    }

    /**
     * Create a new leaderboard programmatically.
     */
    public boolean createLeaderboard(String name) {
        return plugin.getLeaderboardManager().create(name);
    }

    /**
     * Delete a leaderboard programmatically.
     */
    public boolean deleteLeaderboard(String name) {
        return plugin.getLeaderboardManager().delete(name);
    }
}
