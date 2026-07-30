package dev.banglaleaderboard.util;

import java.text.DecimalFormat;

/**
 * Formats numbers with K/M/B/T suffixes or plain decimal format.
 */
public final class NumberFormatter {

    private NumberFormatter() {}

    /**
     * Format a double value.
     *
     * @param value         The raw value
     * @param useSuffixes   Whether to use K/M/B/T suffixes
     * @param decimalPlaces How many decimal places to show
     * @return Formatted string
     */
    public static String format(double value, boolean useSuffixes, int decimalPlaces) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return "0";

        if (!useSuffixes) {
            return buildFormat(decimalPlaces).format(value);
        }

        double abs = Math.abs(value);

        if (abs >= 1_000_000_000_000.0) {
            return buildFormat(decimalPlaces).format(value / 1_000_000_000_000.0) + "T";
        } else if (abs >= 1_000_000_000.0) {
            return buildFormat(decimalPlaces).format(value / 1_000_000_000.0) + "B";
        } else if (abs >= 1_000_000.0) {
            return buildFormat(decimalPlaces).format(value / 1_000_000.0) + "M";
        } else if (abs >= 1_000.0) {
            return buildFormat(decimalPlaces).format(value / 1_000.0) + "K";
        } else {
            return buildFormat(decimalPlaces).format(value);
        }
    }

    private static DecimalFormat buildFormat(int decimals) {
        if (decimals <= 0) return new DecimalFormat("#");
        String pattern = "#." + "#".repeat(decimals);
        return new DecimalFormat(pattern);
    }
}
