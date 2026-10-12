# OnlineTime

OnlineTime is a simple to use plugin to keep track of how long players have spent on your Minecraft server/network.  
It's built for BungeeCord & Velocity proxy servers.  
Good for servers that want to reward active players, keep an eye on the most active users, or just keep everyone a counter of how much time they spent

For placeholder support and richer display integrations, see the companion project: https://github.com/itsTyrion/PlaytimeExpansion

Total DL count: ![GitHub All Releases](https://img.shields.io/github/downloads/itsTyrion/OnlineTime/total.svg)

## Features

- Tracks player online time across sessions and total lifetime playtime
- Supports both BungeeCord and Velocity proxy setups
- Shows personal online time and leaderboard-style top times
- Can reset a single player or the full database
- Supports placeholder updates for external displays and scoreboards
- Offers server rewards based on time thresholds (optional)
- Uses its own SQLite DB by default, optionally supports MySQL/MariaDB.
- Blacklist certain servers by name, e.g. `Hub-1`

## Supported settings

The plugin generates a `settings.yml` file on first run. A few of the main options you may want to adjust are:

- `plugin.commandAliases`: changes the command names used to access the plugin
- `plugin.usePlaceholderApi`: enables placeholder updates for integrations
- `plugin.placeholderRefreshSeconds`: controls how often placeholders are refreshed
- `plugin.topOnlineTimePageLimit`: limits how many players appear per top-time page
- `rewards`: maps playtime minutes to commands and messages that run when a player reaches a threshold
- `mySQL.enabled`: turns MySQL mode on or off
- `mySQL.host`, `mySQL.port`, `mySQL.database`, `mySQL.username`, `mySQL.password`: configure database access when MySQL is enabled


## Installation

Just download the latest GitHub release and drop it into the plugins folder.  

Alternatively, you can easily compile it yourself using Grade.
You just need JDK 17 or higher installed (The "Adoptium" or "Azul Zulu" distributions have always worked for me), then run `./gradlew build`

## Usage

Once enabled, the plugin exposes commands for checking playtime, viewing leaderboard entries, and the admin commands to reset stored data.
This plugin does *not* support a standalone Vanilla/Bukkit/Spigot/Paper server.
