package dev.banglaleaderboard.config;

import dev.banglaleaderboard.BanglaLeaderboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

/**
 * Manages messages.yml and sends formatted Adventure messages.
 */
public class MessageManager {

    private final BanglaLeaderboard plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private FileConfiguration messages;

    public MessageManager(BanglaLeaderboard plugin) {
        this.plugin = plugin;
        load();
    }

    public void reload() {
        load();
    }

    private void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Get a raw message string from messages.yml.
     */
    public String getRaw(String key) {
        String prefix = messages.getString("prefix", "<gradient:#00C9FF:#92FE9D>[BL]</gradient> ");
        String msg = messages.getString(key, "<red>Missing message: " + key);
        return prefix + msg;
    }

    /**
     * Get message without prefix.
     */
    public String getRawNoPrefix(String key) {
        return messages.getString(key, "<red>Missing message: " + key);
    }

    /**
     * Parse MiniMessage and replace placeholders in a message.
     */
    public Component get(String key, Map<String, String> replacements) {
        String raw = getRaw(key);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return miniMessage.deserialize(raw);
    }

    /**
     * Parse simple message with no replacements.
     */
    public Component get(String key) {
        return get(key, Map.of());
    }

    /**
     * Send a message to a CommandSender.
     */
    public void send(CommandSender sender, String key) {
        sender.sendMessage(get(key));
    }

    /**
     * Send a message to a CommandSender with replacements.
     */
    public void send(CommandSender sender, String key, Map<String, String> replacements) {
        sender.sendMessage(get(key, replacements));
    }

    /**
     * Parse a MiniMessage string directly.
     */
    public Component parse(String miniMessageString) {
        return miniMessage.deserialize(miniMessageString);
    }
}
