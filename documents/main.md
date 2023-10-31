# Wiziplicity Design
This document contains a reference for commands and the config that are part of this mod.

If you are confused about anything within this document, please open an [issue](https://github.com/CompassSystem/wiziplicity/issues).

This mod is designed to help plural folk like us but may be a little harder to use for some, any comments regarding how to improve usability are welcome, please open an [issue](https://github.com/CompassSystem/wiziplicity/issues).

Note: this mod is designed after pluralkit and implements much of the same commands though it should be noted using them only affects this mod.

It should also be noted that this mod only implements front mode, other modes and individual headmate auto-proxy is not implemented.
# Commands
## Import Command
This command imports pluralkit system data from a pluralkit export replacing the headmates section of the config with the headmates in the pluralkit system.
```
Usage: pk;import <path to system.json>
       /wiziplicity import <path to system.json>
```
## Member Commands
These groups of commands enable the creation of new headmates and the editing of headmates.
```
Usage: pk;[member|m] new <id>
       pk;m <id> [rename|rn] <newid>
       pk;m <id> delete
       pk;m <id> [displayname|dn]
       pk;m <id> [displayname|dn] <display name>
       pk;m <id> [color|colour]
       pk;m <id> [color|colour] <color>
       pk;m <id> pronouns
       pk;m <id> pronouns <pronouns>
       pk;m <id> [proxy|proxytags]
       pk;m <id> [proxy|proxytags] <proxytag>
       pk;m <id> [proxy|proxytags] [add|remove] <proxytag>
```
## Switch Command
This command switches the currently active headmate.
```
Usage: pk;[switch|sw] <id>
       /wiziplicity switch <id>
```
## Config Commands
These commands can change config values, notably: `nickname_format`, `skin_change_delay` and the per server settings.
```
Usage: /wiziplicity config global nickname_format with_pronouns <nickname format>
       /wiziplicity config global nickname_format no_pronouns <nickname format>
       /wiziplicity config global skin_change_delay <delay>
       /wiziplicity config server skin_change_delay <delay>
       /wiziplicity config server alias <existing server>
```

# Config
The config will store some general settings such as a default nickname format as well as specific settings for each headmate.

## Nickname Format
This format will be used when a headmate hasn't got their own nickname defined.

A few placeholders will be supported namely `colour` / `color`, `name`, `id`, and `pronouns`

If the `name` value is not provided then it will default on `id`.

If the `color` value is not provided then it will default the color `white`.
```json5
{
  "id": "needle",
  "name": "Needle",
  "pronouns": "it/its",
  "colour": "#49D2C4",
  // Values above provided as reference
  "nickname_format": {
    "no_pronouns": "<c:{colour}>{name}</c> <gold>🧭</gold>",
    "with_pronouns": "<c:{colour}>{name}</c> <gray>({pronouns})</gray> <gold>🧭</gold>"
  }
}
```

## Skin Change Delay
The delay on the skin change in seconds.
```json
{
  "skin_change_delay": 60
}
```

## Headmates
Each entry will store a few details including: `id` (object key), and optionally: `name`, `nickname`, `pronouns`, `proxytags`, and `color`
```json5
{
  "headmates": {
    "needle": {
      "name": "Needle",
      "pronouns": "it/its",
      "color": "#49D2C4",
      "proxytags": ["text-needle"]
    }
  }
}
```

## Server Settings
Way to configure the `skin_change_delay` per server.
Can reference another entry instead of new values for aliases to the same server.
```json
{
  "server_settings": {
    "127.0.0.1": {
      "nickname_delay": 2
    },
    "example.com": "127.0.0.1"
  }
}
```