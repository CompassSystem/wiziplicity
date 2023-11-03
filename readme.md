# 🧙⚙️ Wiziplicity
A helper mod for systems to avoid having to change nicknames, partially integrates with PluralKit.

For information about plurality see: https://morethanone.info/

## Getting Started

### Importing system data
If you use PluralKit you can import a system export using `/wiziplicity import <file>` or `pk;import <file>` where file is either the path to the file on disk or the `URL` of the export.

### Creating members
If you don't have an existing system already you can easily create a member with the usual PluralKit command `pk;[m|member] new <id>` or using `/wiziplicity member new <id>`
> Note: ids must consist of only alphanumeric characters, underscores and dashes
```
pk;m new needle
```
It is recommended you also set a display name, pronouns, and color for each member.
```
pk;m needle displayname Needle
pk;m needle pronouns it/its
pk;m needle color #49D2C4
```
> Note: colors can be any valid 6 digit hex, #ffffff, or any of the builtin minecraft colors:
> 
> `black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`, `gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`

### Finalizing Setup
To finish setup you'll likely want to set a nickname format for members with and without pronouns like so:
```
/wiziplicity config global nickname_format with_pronouns <c:{colour}>{name}</c> <gray>({pronouns})</gray> <gold>🧭</gold>
/wiziplicity config global nickname_format no_pronouns <c:{colour}>{name}</c> <gold>🧭</gold>
```
> Note: nickname formats can take advantage of any [Styled Chat](https://placeholders.pb4.eu/user/text-format/) formatting, some may be restricted depending on the server.

> Note the following tags are support:
>
> `{name}` The display name for the member defaulting on the member id
>
> `{id}`, `{pronouns}`
> 
> `{color}` or `{colour}` The color for the member defaulting on white

Finally, you need to switch in using `pk;[sw|switch] <id>` or `/wiziplicity switch <id>`.

For more information please read the [reference document](https://github.com/CompassSystem/wiziplicity/blob/main/documents/main.md).

## Why Wiziplicity?

We were in a VC whilst trying to come up with names for this mod. We thought up the name "System Helper" however we thought this would be confusing for singlets.

Another person said Wizzerd, a misspelling of Wizard and a reference to the Discworld book series.

From wizard, which is a program which helps the user through a complex process, the mod name Wiziplicity was suggested, a combination of wizard and multiplicity.

