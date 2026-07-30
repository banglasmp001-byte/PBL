package dev.banglaleaderboard.expansion;

import dev.banglaleaderboard.BanglaLeaderboard;
import dev.banglaleaderboard.model.Leaderboard;
import dev.banglaleaderboard.model.LeaderboardEntry;
import dev.banglaleaderboard.util.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BanglaLeaderboard PlaceholderAPI Expansion.
 *
 * Registered identifier: "bl"
 *
 * ─────────────────────────────────────────────────
 * Supported placeholders:
 *
 *   %bl_<leaderboard>_top_<pos>_name%         → Player name at position
 *   %bl_<leaderboard>_top_<pos>_value%        → Raw numeric value
 *   %bl_<leaderboard>_top_<pos>_formatted%    → Formatted value (e.g. 1.2M)
 *   %bl_<leaderboard>_top_<pos>_rank%         → Rank number
 *   %bl_<leaderboard>_top_<pos>%              → Combined (e.g. "🥇 PlayerName - $1.2M")
 *   %bl_<leaderboard>_player_rank%            → The requesting player's rank in the lb
 *   %bl_<leaderboard>_player_value%           → The requesting player's value
 *   %bl_<leaderboard>_player_formatted%       → The requesting player's formatted value
 * ─────────────────────────────────────────────────
 */
public class BLExpansion extends PlaceholderExpansion {

    private final BanglaLeaderboard plugin;

    public BLExpansion(BanglaLeaderboard plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bl";
    }

    @Override
    public @NotNull String getAuthor() {
        return "NoTXGameR";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Don't unregister when PlaceholderAPI reloads
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        // params format: <leaderboard>_top_<pos>_<type>
        // or: <leaderboard>_top_<pos>
        // or: <leaderboard>_player_<type>

        if (params.isBlank()) return null;

        String[] parts = params.split("_");
        if (parts.length < 3) return null;

        // Find leaderboard name (may contain underscores)
        // Strategy: try longest match first
        Leaderboard lb = findLeaderboard(parts);
        if (lb == null) return null;

        String lbName = lb.getName();
        // Get the remaining part after leaderboard name
        String remaining = params.substring(lbName.length() + 1); // skip "name_"

        // Handle: player_<type>
        if (remaining.startsWith("player_")) {
            return handlePlayerPlaceholder(player, lb, remaining.substring(7));
        }

        // Handle: top_<pos> or top_<pos>_<type>
        if (remaining.startsWith("top_")) {
            String afterTop = remaining.substring(4);
            String[] topParts = afterTop.split("_", 2);
            int pos;
            try {
                pos = Integer.parseInt(topParts[0]);
            } catch (NumberFormatException e) {
                return null;
            }

            String type = topParts.length > 1 ? topParts[1] : "combined";
            return handleTopPlaceholder(lb, pos, type);
        }

        return null;
    }

    /**
     * Handle %bl_<lb>_top_<pos>_<type>%
     */
    private String handleTopPlaceholder(Leaderboard lb, int pos, String type) {
        LeaderboardEntry entry = lb.getEntry(pos);
        if (entry == null) return getEmptySlot(pos);

        return switch (type) {
            case "name" -> entry.getPlayerName();
            case "value" -> String.valueOf(entry.getValue());
            case "formatted" -> NumberFormatter.format(entry.getValue(), lb.isUseSuffixes(), lb.getDecimalPlaces());
            case "rank" -> String.valueOf(entry.getRank());
            case "combined", "" -> buildCombined(lb, entry);
            default -> null;
        };
    }

    /**
     * Handle %bl_<lb>_player_<type>%
     */
    private String handlePlayerPlaceholder(OfflinePlayer player, Leaderboard lb, String type) {
        if (player == null) return "N/A";

        int rank = lb.getPlayerRank(player.getName());

        return switch (type) {
            case "rank" -> rank == -1 ? "N/A" : String.valueOf(rank);
            case "value" -> {
                if (rank == -1) yield "0";
                LeaderboardEntry e = lb.getEntry(rank);
                yield e != null ? String.valueOf(e.getValue()) : "0";
            }
            case "formatted" -> {
                if (rank == -1) yield "0";
                LeaderboardEntry e = lb.getEntry(rank);
                yield e != null ? NumberFormatter.format(e.getValue(), lb.isUseSuffixes(), lb.getDecimalPlaces()) : "0";
            }
            default -> null;
        };
    }

    /**
     * Build the combined placeholder string.
     * e.g. "🥇 NoTXGameR - $1.23M"
     */
    private String buildCombined(Leaderboard lb, LeaderboardEntry entry) {
        String format = lb.getCombinedFormat();
        if (format == null || format.isBlank()) {
            format = plugin.getConfigManager().getCombinedFormat();
        }

        String medal = plugin.getConfigManager().getMedal(entry.getRank());
        String formattedValue = NumberFormatter.format(entry.getValue(), lb.isUseSuffixes(), lb.getDecimalPlaces());

        return format
                .replace("{medal}", medal)
                .replace("{rank}", String.valueOf(entry.getRank()))
                .replace("{name}", entry.getPlayerName())
                .replace("{value}", String.valueOf(entry.getValue()))
                .replace("{formatted_value}", formattedValue);
    }

    /**
     * Return a default empty slot string.
     */
    private String getEmptySlot(int pos) {
        String medal = plugin.getConfigManager().getMedal(pos);
        return medal + " Empty";
    }

    /**
     * Find leaderboard by trying to match the start of the params string.
     * Supports leaderboard names with underscores.
     */
    private Leaderboard findLeaderboard(String[] parts) {
        // Try matching from longest to shortest prefix
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length - 2; i++) {
            if (i > 0) sb.append("_");
            sb.append(parts[i]);
            Leaderboard lb = plugin.getLeaderboardManager().getLeaderboard(sb.toString());
            if (lb != null) {
                // Verify next segment is "top" or "player"
                String next = parts[i + 1];
                if (next.equals("top") || next.equals("player")) {
                    return lb;
                }
            }
        }
        return null;
    }
}
