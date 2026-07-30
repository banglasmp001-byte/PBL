package dev.banglaleaderboard.util;

import dev.banglaleaderboard.BanglaLeaderboard;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple wrapper around Java Logger with BL prefix.
 */
public class BLLogger {

    private final Logger logger;
    private final BanglaLeaderboard plugin;

    public BLLogger(BanglaLeaderboard plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message) {
        logger.warning(message);
    }

    public void severe(String message) {
        logger.severe(message);
    }

    public void severe(String message, Throwable t) {
        logger.log(Level.SEVERE, message, t);
    }

    public void debug(String message) {
        if (plugin.getConfigManager() != null && plugin.getConfigManager().isDebug()) {
            logger.info("[DEBUG] " + message);
        }
    }
}
