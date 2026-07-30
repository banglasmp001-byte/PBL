package dev.banglaleaderboard.service;

import dev.banglaleaderboard.BanglaLeaderboard;
import dev.banglaleaderboard.model.Leaderboard;
import dev.banglaleaderboard.model.LeaderboardEntry;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles asynchronous leaderboard data refresh.
 * Collects placeholder values for all online (and offline) players,
 * sorts results, and updates leaderboard entries.
 */
public class RefreshService {

    private final BanglaLeaderboard plugin;
    private BukkitTask schedulerTask;
    private final AtomicBoolean running = new AtomicBoolean(false);

    // Tracks which leaderboards are currently being refreshed (prevents overlap)
    private final Set<String> refreshingNow = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public RefreshService(BanglaLeaderboard plugin) {
        this.plugin = plugin;
    }

    /**
     * Start the auto-refresh scheduler.
     */
    public void start() {
        if (running.getAndSet(true)) return;

        // Check every 20 ticks (1 second) which leaderboards need refreshing
        schedulerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Leaderboard lb : plugin.getLeaderboardManager().getAllLeaderboards()) {
                if (lb.isEnabled() && lb.needsRefresh()) {
                    refreshAsync(lb);
                }
            }
        }, 20L, 20L);
    }

    /**
     * Stop the scheduler.
     */
    public void stop() {
        running.set(false);
        if (schedulerTask != null) {
            schedulerTask.cancel();
        }
    }

    /**
     * Refresh a specific leaderboard asynchronously.
     */
    public CompletableFuture<Void> refreshAsync(Leaderboard lb) {
        if (!refreshingNow.add(lb.getName())) {
            // Already refreshing, skip
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                collectAndUpdate(lb);
            } catch (Exception e) {
                plugin.getBLLogger().warn("Error refreshing leaderboard '" + lb.getName() + "': " + e.getMessage());
            } finally {
                refreshingNow.remove(lb.getName());
            }
        });
    }

    /**
     * Refresh all leaderboards.
     */
    public void refreshAll() {
        for (Leaderboard lb : plugin.getLeaderboardManager().getAllLeaderboards()) {
            if (lb.isEnabled()) {
                refreshAsync(lb);
            }
        }
    }

    /**
     * Core data collection logic.
     * Evaluates PlaceholderAPI placeholders for all players and sorts results.
     */
    private void collectAndUpdate(Leaderboard lb) {
        if (!plugin.getIntegrationManager().isPlaceholderAPIEnabled()) return;

        String placeholder = lb.getPlaceholder();
        if (placeholder == null || placeholder.isBlank()) return;

        // Collect ALL known players (online + offline with stats)
        Set<OfflinePlayer> players = getAllTrackedPlayers(lb);
        if (players.isEmpty()) return;

        List<RawEntry> rawEntries = new ArrayList<>();
        int batchSize = plugin.getConfigManager().getBatchSize();
        List<OfflinePlayer> playerList = new ArrayList<>(players);

        // Process in batches to avoid TPS spikes
        for (int i = 0; i < playerList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, playerList.size());
            List<OfflinePlayer> batch = playerList.subList(i, end);

            for (OfflinePlayer player : batch) {
                try {
                    // PlaceholderAPI.setPlaceholders must run on main thread for some expansions
                    // We use offline player which most expansions support async
                    String raw = PlaceholderAPI.setPlaceholders(player, placeholder);
                    double value = parseDouble(raw);
                    rawEntries.add(new RawEntry(player.getName() != null ? player.getName() : "Unknown", value, raw));
                } catch (Exception ignored) {
                    // Player or expansion returned null/error - skip
                }
            }
        }

        // Sort entries
        rawEntries.sort((a, b) -> {
            if (lb.getSortOrder() == Leaderboard.SortOrder.DESC) {
                return Double.compare(b.value, a.value);
            } else {
                return Double.compare(a.value, b.value);
            }
        });

        // Build final entry list limited to topSize
        List<LeaderboardEntry> entries = new ArrayList<>();
        int limit = Math.min(lb.getTopSize(), rawEntries.size());
        for (int i = 0; i < limit; i++) {
            RawEntry raw = rawEntries.get(i);
            entries.add(new LeaderboardEntry(i + 1, raw.playerName, raw.value, raw.rawValue));
        }

        // Update leaderboard (thread-safe)
        lb.updateEntries(entries);

        // Update cache
        if (lb.isCacheEnabled()) {
            plugin.getCacheManager().updateCache(lb.getName(), entries);
        }

        if (plugin.getConfigManager().isDebug()) {
            plugin.getBLLogger().info("Refreshed '" + lb.getName() + "' with " + entries.size() + " entries.");
        }
    }

    /**
     * Get all players to evaluate.
     * Includes online players + offline players who have joined before.
     */
    private Set<OfflinePlayer> getAllTrackedPlayers(Leaderboard lb) {
        Set<OfflinePlayer> players = new HashSet<>();

        // Online players
        players.addAll(Bukkit.getOnlinePlayers());

        // Offline players (all who have ever joined)
        // This can be large on big servers but is necessary for global leaderboards
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        if (offlinePlayers.length < 5000) {
            // Only load offline players if count is manageable
            Collections.addAll(players, offlinePlayers);
        }

        // Apply world filter if set (only applicable for online players)
        List<String> worldFilter = lb.getWorldFilter();
        if (!worldFilter.isEmpty()) {
            players.removeIf(p -> {
                if (p.getPlayer() != null && p.getPlayer().isOnline()) {
                    String world = p.getPlayer().getWorld().getName();
                    return !worldFilter.contains(world);
                }
                return false; // Keep offline players regardless
            });
        }

        return players;
    }

    /**
     * Parse a string as double, stripping currency symbols and suffixes.
     */
    private double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) return 0.0;

        // Remove common currency and formatting characters
        String cleaned = raw
                .replaceAll("[,$£€¥₹₩฿]", "")
                .replaceAll("[^0-9.\\-+EeKkMmBbTt]", "")
                .trim();

        // Handle suffixes K/M/B/T
        if (cleaned.isEmpty()) return 0.0;

        char last = cleaned.charAt(cleaned.length() - 1);
        double multiplier = 1.0;

        if (Character.isLetter(last)) {
            multiplier = switch (Character.toUpperCase(last)) {
                case 'K' -> 1_000.0;
                case 'M' -> 1_000_000.0;
                case 'B' -> 1_000_000_000.0;
                case 'T' -> 1_000_000_000_000.0;
                default -> 1.0;
            };
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        try {
            return Double.parseDouble(cleaned) * multiplier;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private record RawEntry(String playerName, double value, String rawValue) {}
}
