package dev.banglaleaderboard.command;

import dev.banglaleaderboard.BanglaLeaderboard;
import dev.banglaleaderboard.model.Leaderboard;
import dev.banglaleaderboard.model.LeaderboardEntry;
import dev.banglaleaderboard.util.NumberFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main /bl command handler with all subcommands.
 */
public class BLCommand implements CommandExecutor, TabCompleter {

    private final BanglaLeaderboard plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private static final List<String> SUBCOMMANDS = List.of(
            "create", "delete", "rename", "reload", "refresh", "list",
            "info", "enable", "disable", "backup", "restore", "import", "export", "help"
    );

    public BLCommand(BanglaLeaderboard plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help" -> sendHelp(sender);
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "rename" -> handleRename(sender, args);
            case "reload" -> handleReload(sender);
            case "refresh" -> handleRefresh(sender, args);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            case "enable" -> handleSetEnabled(sender, args, true);
            case "disable" -> handleSetEnabled(sender, args, false);
            case "backup" -> handleBackup(sender);
            case "restore" -> handleRestore(sender, args);
            case "import" -> handleImport(sender, args);
            case "export" -> handleExport(sender);
            default -> msg(sender, "<red>Unknown subcommand. Use <yellow>/bl help</yellow>.");
        }

        return true;
    }

    // ==========================================
    // SUBCOMMAND HANDLERS
    // ==========================================

    private void handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("banglaleaderboard.create")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            msg(sender, "<red>Usage: /bl create <name>");
            return;
        }
        String name = args[1].toLowerCase();
        if (plugin.getLeaderboardManager().create(name)) {
            plugin.getMessageManager().send(sender, "leaderboard-created", Map.of("name", name));
        } else {
            plugin.getMessageManager().send(sender, "leaderboard-already-exists", Map.of("name", name));
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("banglaleaderboard.delete")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            msg(sender, "<red>Usage: /bl delete <name>");
            return;
        }
        String name = args[1].toLowerCase();
        if (plugin.getLeaderboardManager().delete(name)) {
            plugin.getMessageManager().send(sender, "leaderboard-deleted", Map.of("name", name));
        } else {
            plugin.getMessageManager().send(sender, "leaderboard-not-found", Map.of("name", name));
        }
    }

    private void handleRename(CommandSender sender, String[] args) {
        if (!sender.hasPermission("banglaleaderboard.admin")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        if (args.length < 3) {
            msg(sender, "<red>Usage: /bl rename <old> <new>");
            return;
        }
        String old = args[1].toLowerCase();
        String newName = args[2].toLowerCase();
        if (plugin.getLeaderboardManager().rename(old, newName)) {
            plugin.getMessageManager().send(sender, "leaderboard-renamed",
                    Map.of("old", old, "new", newName));
        } else {
            plugin.getMessageManager().send(sender, "leaderboard-not-found", Map.of("name", old));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("banglaleaderboard.reload")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        try {
            plugin.reload();
            plugin.getMessageManager().send(sender, "reload-success");
        } catch (Exception e) {
            plugin.getMessageManager().send(sender, "reload-failed");
            plugin.getBLLogger().severe("Reload error", e);
        }
    }

    private void handleRefresh(CommandSender sender, String[] args) {
        if (!sender.hasPermission("banglaleaderboard.refresh")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            // Refresh all
            plugin.getRefreshService().refreshAll();
            plugin.getMessageManager().send(sender, "all-refreshed");
        } else {
            String name = args[1].toLowerCase();
            Leaderboard lb = plugin.getLeaderboardManager().getLeaderboard(name);
            if (lb == null) {
                plugin.getMessageManager().send(sender, "leaderboard-not-found", Map.of("name", name));
                return;
            }
            plugin.getRefreshService().refreshAsync(lb);
            plugin.getMessageManager().send(sender, "leaderboard-refreshed", Map.of("name", name));
        }
    }

    private void handleList(CommandSender sender) {
        Collection<Leaderboard> all = plugin.getLeaderboardManager().getAllLeaderboards();
        String countStr = String.valueOf(all.size());
        sender.sendMessage(mm.deserialize(
                plugin.getMessageManager().getRawNoPrefix("list-header")
                        .replace("{count}", countStr)
        ));

        if (all.isEmpty()) {
            plugin.getMessageManager().send(sender, "list-empty");
            return;
        }

        for (Leaderboard lb : all) {
            String key = lb.isEnabled() ? "list-entry-enabled" : "list-entry-disabled";
            sender.sendMessage(mm.deserialize(
                    plugin.getMessageManager().getRawNoPrefix(key)
                            .replace("{name}", lb.getName())
                            .replace("{placeholder}", lb.getPlaceholder())
            ));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            msg(sender, "<red>Usage: /bl info <name>");
            return;
        }
        String name = args[1].toLowerCase();
        Leaderboard lb = plugin.getLeaderboardManager().getLeaderboard(name);
        if (lb == null) {
            plugin.getMessageManager().send(sender, "leaderboard-not-found", Map.of("name", name));
            return;
        }

        String lastUpdate = lb.getLastUpdateTime() == 0 ? "Never"
                : new SimpleDateFormat("HH:mm:ss").format(new Date(lb.getLastUpdateTime()));

        sender.sendMessage(mm.deserialize(plugin.getMessageManager().getRawNoPrefix("info-header")));
        sender.sendMessage(mm.deserialize(plugin.getMessageManager().getRawNoPrefix("info-name").replace("{name}", lb.getName())));
        sender.sendMessage(mm.deserialize(plugin.getMessageManager().getRawNoPrefix("info-enabled").replace("{enabled}", String.valueOf(lb.isEnabled()))));
        sender.sendMessage(mm.deserialize(plugin.getMessageManager().getRawNoPrefix("info-placeholder").replace("{placeholder}", lb.getPlaceholder())));
        sender.sendMessage(mm.deserialize(plugin.getMessageManager().getRawNoPrefix("info-sort").replace("{sort}", lb.getSortOrder().name())));
        sender.sendMessage(mm.deserialize(plugin.getMessageManager().getRawNoPrefix("info-interval").replace("{interval}", String.valueOf(lb.getUpdateInterval()))));
        sender.sendMessage(mm.deserialize(plugin.getMessageManager().getRawNoPrefix("info-top-size").replace("{size}", String.valueOf(lb.getTopSize()))));
        sender.sendMessage(mm.deserialize(plugin.getMessageManager().getRawNoPrefix("info-last-update").replace("{time}", lastUpdate)));
        sender.sendMessage(mm.deserialize(plugin.getMessageManager().getRawNoPrefix("info-cached-entries").replace("{count}", String.valueOf(lb.getEntries().size()))));

        // Show top 3 entries if available
        List<LeaderboardEntry> entries = lb.getEntries();
        if (!entries.isEmpty()) {
            msg(sender, "<gray>  Top entries:");
            int show = Math.min(3, entries.size());
            for (int i = 0; i < show; i++) {
                LeaderboardEntry e = entries.get(i);
                String formatted = NumberFormatter.format(e.getValue(), lb.isUseSuffixes(), lb.getDecimalPlaces());
                msg(sender, "  <yellow>#" + e.getRank() + "</yellow> <white>" + e.getPlayerName() + "</white> <gray>- " + formatted);
            }
        }
    }

    private void handleSetEnabled(CommandSender sender, String[] args, boolean enabled) {
        if (!sender.hasPermission("banglaleaderboard.admin")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            msg(sender, "<red>Usage: /bl " + args[0] + " <name>");
            return;
        }
        String name = args[1].toLowerCase();
        if (plugin.getLeaderboardManager().setEnabled(name, enabled)) {
            String key = enabled ? "leaderboard-enabled" : "leaderboard-disabled";
            plugin.getMessageManager().send(sender, key, Map.of("name", name));
        } else {
            plugin.getMessageManager().send(sender, "leaderboard-not-found", Map.of("name", name));
        }
    }

    private void handleBackup(CommandSender sender) {
        if (!sender.hasPermission("banglaleaderboard.admin")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        String file = plugin.getStorageManager().createBackup();
        if (file != null) {
            plugin.getMessageManager().send(sender, "backup-success", Map.of("file", file));
        } else {
            plugin.getMessageManager().send(sender, "backup-failed");
        }
    }

    private void handleRestore(CommandSender sender, String[] args) {
        if (!sender.hasPermission("banglaleaderboard.admin")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            msg(sender, "<red>Usage: /bl restore <backup-name>");
            return;
        }
        String backupName = args[1];
        if (plugin.getStorageManager().restore(backupName)) {
            plugin.getMessageManager().send(sender, "restore-success", Map.of("file", backupName));
        } else {
            plugin.getMessageManager().send(sender, "restore-not-found", Map.of("file", backupName));
        }
    }

    private void handleImport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("banglaleaderboard.import")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            msg(sender, "<red>Usage: /bl import <filename>");
            return;
        }
        int count = plugin.getStorageManager().importFrom(args[1]);
        if (count >= 0) {
            plugin.getMessageManager().send(sender, "import-success", Map.of("count", String.valueOf(count)));
        } else {
            plugin.getMessageManager().send(sender, "import-failed");
        }
    }

    private void handleExport(CommandSender sender) {
        if (!sender.hasPermission("banglaleaderboard.export")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        String file = plugin.getStorageManager().export();
        if (file != null) {
            plugin.getMessageManager().send(sender, "export-success", Map.of("file", file));
        } else {
            msg(sender, "<red>Export failed. Check console.");
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(mm.deserialize(plugin.getMessageManager().getRawNoPrefix("help-header")));
        String[][] cmds = {
                {"create <name>", "Create a new leaderboard"},
                {"delete <name>", "Delete a leaderboard"},
                {"rename <old> <new>", "Rename a leaderboard"},
                {"reload", "Reload all configs"},
                {"refresh [name]", "Refresh leaderboard data"},
                {"list", "List all leaderboards"},
                {"info <name>", "Show leaderboard info"},
                {"enable <name>", "Enable a leaderboard"},
                {"disable <name>", "Disable a leaderboard"},
                {"backup", "Create a backup"},
                {"restore <name>", "Restore from backup"},
                {"import <file>", "Import leaderboards"},
                {"export", "Export leaderboards"},
        };
        for (String[] cmd : cmds) {
            sender.sendMessage(mm.deserialize(
                    " <aqua>/bl " + cmd[0] + " <gray>- " + cmd[1]
            ));
        }
        sender.sendMessage(mm.deserialize(plugin.getMessageManager().getRawNoPrefix("help-footer")));
    }

    // ==========================================
    // TAB COMPLETION
    // ==========================================

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }

        String sub = args[0].toLowerCase();
        if (args.length == 2) {
            return switch (sub) {
                case "delete", "info", "enable", "disable", "refresh" ->
                        filter(plugin.getLeaderboardManager().getLeaderboardNames(), args[1]);
                case "rename" ->
                        filter(plugin.getLeaderboardManager().getLeaderboardNames(), args[1]);
                default -> Collections.emptyList();
            };
        }

        if (args.length == 3 && sub.equals("rename")) {
            return filter(plugin.getLeaderboardManager().getLeaderboardNames(), args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream()
                .filter(s -> s.startsWith(lower))
                .collect(Collectors.toList());
    }

    private void msg(CommandSender sender, String miniMessageText) {
        sender.sendMessage(mm.deserialize(miniMessageText));
    }
}
