# 🧙⚙️ Wiziplicity
A client side helper mod for plural folk to help with changing [nicknames](https://modrinth.com/mod/styled-nicknames) and [skins](https://modrinth.com/mod/fabrictailor).

For information about plurality see: https://morethanone.info/

## Getting Started

### Importing system data
If you use PluralKit you can import a system export using `/wiziplicity import <file>` or `pk;import <file>` where file is either the path to the file on disk or the `URL` of the export.

### Creating members
If you don't have an existing system already you can easily create a member with the usual PluralKit command `pk;[m|member] new <id>` or using `/wiziplicity member new <id>`
> Note: ids must consist of only alphanumeric characters, underscores and dashes
```
pk;m new brenn
```
It is recommended you also set a display name, pronouns, and color for each member.
```
pk;m brenn displayname Brenn
pk;m brenn pronouns she/her
pk;m brenn color #B58EDF
```
> Note: colors can be any valid 6 digit hex, #ffffff, or any of the builtin minecraft colors:
>
> `black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`, `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`
>
> Additionally, the following aliases provided by Placeholder API can be used:
>
> `orange`, `grey`, `pink`, `dark_grey`

### Finalizing Setup
To finish setup you'll likely want to set a nickname format for members with and without pronouns like so:
```
/wiziplicity config global nickname_format with_pronouns <c:{colour}>{name}</c> <gray>({pronouns})</gray> <blue>🪣</blue>
/wiziplicity config global nickname_format no_pronouns <c:{colour}>{name}</c> <blue>🪣</blue>
```
> Note: nickname formats can take advantage of any [Placeholder API](https://placeholders.pb4.eu/user/text-format/) formatting, some may be restricted depending on the server.

> Note the following tags are support:
>
> `{name}` The display name for the member defaulting on the member id
>
> `{pronouns}`
>
> `{color}` or `{colour}` The color for the member defaulting on white

Finally, you need to switch in using `pk;[sw|switch] <id>` or `/wiziplicity switch <id>`.

For more information please read the [reference document](https://github.com/CompassSystem/wiziplicity/blob/main/documents/main.md).

## Why Wiziplicity?

We were in a VC whilst trying to come up with names for this mod. We thought up the name "System Helper" however we thought this would be confusing for singlets.

Another person said Wizzerd, a misspelling of Wizard and a reference to the Discworld book series.

From wizard, which is a program which helps the user through a complex process, the mod name Wiziplicity was suggested, a combination of wizard and multiplicity.

