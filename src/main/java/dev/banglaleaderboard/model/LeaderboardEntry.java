package dev.banglaleaderboard.model;

/**
 * Represents a single entry (player + value) in a leaderboard.
 */
public class LeaderboardEntry {

    private final String playerName;
    private final double value;
    private final String rawValue;
    private final int rank;

    public LeaderboardEntry(int rank, String playerName, double value, String rawValue) {
        this.rank = rank;
        this.playerName = playerName;
        this.value = value;
        this.rawValue = rawValue;
    }

    public int getRank() { return rank; }
    public String getPlayerName() { return playerName; }
    public double getValue() { return value; }
    public String getRawValue() { return rawValue; }

    @Override
    public String toString() {
        return "LeaderboardEntry{rank=" + rank + ", player='" + playerName + "', value=" + value + "}";
    }
}
