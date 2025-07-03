# ChatHider

ChatHider is a small Minecraft mod that lets you filter chat messages based on player names. You can toggle it on/off, and maintain a whitelist and blacklist to control which messages are shown.

## Features

* Toggle the chat filter on/off with a key (`H` by default)
* Whitelist: always show messages from certain players
* Blacklist: hide pings from specific players
* Simple commands to manage lists in-game
* Config file saves changes automatically

## Commands

```
/chathider toggle
/chathider whitelist add <player>
/chathider whitelist remove <player>
/chathider whitelist list
/chathider blacklist add <player>
/chathider blacklist remove <player>
/chathider blacklist list
```

## Keybind

* Default key to toggle the mod: `H`
* Can be changed in the Minecraft controls menu

## Config

The mod creates a config file at:

```
.minecraft/config/chathider.cfg
```

You can edit the enabled state, whitelist, and blacklist directly if needed.

## Requirements

* Minecraft Forge

## Building

Standard ForgeGradle setup:

```bash
./gradlew build
```

Jar will be in `build/libs/`.

## Notes

* Messages from whitelisted players are always shown
* Messages from blacklisted players that mention your name are hidden
* Common chat prefixes like "Guild >", "Party >", "From", and "To" are ignored by the filter
