# 🏆 BanglaLeaderboard

**Production-ready, PlaceholderAPI-powered leaderboard plugin for Paper 1.21–1.21.11**

[![Build](https://github.com/NoTXGameR/BanglaLeaderboard/actions/workflows/build.yml/badge.svg)](https://github.com/NoTXGameR/BanglaLeaderboard/actions)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)
[![Paper](https://img.shields.io/badge/Paper-1.21--1.21.11-blue)](https://papermc.io/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

---

## ✨ Features

- 🔗 **Any PlaceholderAPI placeholder** as a leaderboard source
- ⚡ **Async data collection** — zero TPS impact
- 💾 **Smart cache system** with disk persistence
- 🎯 **Automatic PAPI placeholders** generated per leaderboard
- 🏅 **Medal system** — 🥇🥈🥉 for top 3
- 📊 **Number formatting** — 1.23M, 456K, 7.8B
- 🔄 **Configurable refresh intervals** per leaderboard
- 🌍 **World filter** support
- 💾 **Backup & restore** system
- 📦 **Import / Export** leaderboard configs
- 🎨 **MiniMessage** support in all display strings
- 🔌 **Public API** for other plugins

---

## 📦 Requirements

| Dependency | Required |
|---|---|
| Paper 1.21–1.21.11 | ✅ |
| Java 21 | ✅ |
| PlaceholderAPI | ✅ |
| Vault | Optional |
| LuckPerms | Optional |
| EssentialsX | Optional |

---

## 🚀 Quick Start

1. Drop `BanglaLeaderboard.jar` into your `plugins/` folder
2. Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
3. Restart server
4. Edit `plugins/BanglaLeaderboard/leaderboards/balance.yml`
5. Use the generated placeholders anywhere PAPI works!

---

## 📖 Placeholder System

Every leaderboard **automatically** generates these PAPI placeholders:

```
%bl_<name>_top_<pos>%              → 🥇 PlayerName - $1.23M
%bl_<name>_top_<pos>_name%         → PlayerName
%bl_<name>_top_<pos>_value%        → 1234567.89
%bl_<name>_top_<pos>_formatted%    → 1.23M
%bl_<name>_top_<pos>_rank%         → 1

%bl_<name>_player_rank%            → Your rank in the leaderboard
%bl_<name>_player_value%           → Your raw value
%bl_<name>_player_formatted%       → Your formatted value
```

### Example — Balance Leaderboard

```
%bl_balance_top_1%         → 🥇 NoTXGameR - $5.67M
%bl_balance_top_1_name%    → NoTXGameR
%bl_balance_top_1_value%   → 5670000
%bl_balance_top_2%         → 🥈 Player2 - $3.2M
%bl_balance_top_3%         → 🥉 Player3 - $1.1M
```

---

## ⚙️ Leaderboard Config

Each leaderboard has its own YAML in `plugins/BanglaLeaderboard/leaderboards/`:

```yaml
name: "balance"
enabled: true
display-name: "<gold>Top Balance"
placeholder: "%vault_eco_balance%"
sort: DESC
update-interval: 300
top-size: 10
number-format:
  enabled: true
  use-suffixes: true
  decimal-places: 2
combined-format: "{medal} {name} - {formatted_value}"
```

---

## 🛠️ Commands

| Command | Description | Permission |
|---|---|---|
| `/bl create <name>` | Create new leaderboard | `banglaleaderboard.create` |
| `/bl delete <name>` | Delete a leaderboard | `banglaleaderboard.delete` |
| `/bl rename <old> <new>` | Rename a leaderboard | `banglaleaderboard.admin` |
| `/bl reload` | Reload all configs | `banglaleaderboard.reload` |
| `/bl refresh [name]` | Force refresh data | `banglaleaderboard.refresh` |
| `/bl list` | List all leaderboards | `banglaleaderboard.admin` |
| `/bl info <name>` | Show leaderboard details | `banglaleaderboard.admin` |
| `/bl enable <name>` | Enable a leaderboard | `banglaleaderboard.admin` |
| `/bl disable <name>` | Disable a leaderboard | `banglaleaderboard.admin` |
| `/bl backup` | Create a backup | `banglaleaderboard.admin` |
| `/bl restore <name>` | Restore from backup | `banglaleaderboard.admin` |
| `/bl import <file>` | Import leaderboards | `banglaleaderboard.import` |
| `/bl export` | Export leaderboards | `banglaleaderboard.export` |

---

## 🔌 Works With

- **BanglaHologram** — display leaderboards as floating holograms
- **TAB** — show top players in the tab list
- **FeatherBoard / CMI Scoreboards** — sidebar leaderboards
- **DeluxeMenus / ChestCommands** — GUI leaderboard menus
- **Any PlaceholderAPI-supported plugin**

---

## 👨‍💻 Developer API

```java
// Get the API
BanglaLeaderboardAPI api = BanglaLeaderboard.getInstance().getApi();

// Get top entries
List<LeaderboardEntry> top10 = api.getEntries("balance");

// Get player rank
int rank = api.getPlayerRank("balance", player.getName());

// Force refresh
api.refreshLeaderboard("balance");
```

---

## 📄 License

MIT License — see [LICENSE](LICENSE)

---

**Made with ❤️ by [NoTXGameR](https://github.com/NoTXGameR) | UBMC STUDIO**
