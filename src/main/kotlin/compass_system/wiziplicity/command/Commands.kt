package compass_system.wiziplicity.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import compass_system.wiziplicity.Main
import compass_system.wiziplicity.command.arguments.ColorArgumentType
import compass_system.wiziplicity.command.arguments.HeadmateArgumentType
import compass_system.wiziplicity.command.arguments.ServerArgumentType
import compass_system.wiziplicity.config.*
import compass_system.wiziplicity.config.ignoreKeysJson
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readText

object Commands {
    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, context ->
            dispatcher.register(Main.MOD_ID) {
                declareImportCommand()
                declareMemberCommand()
                declareSwitchCommand()
                declareConfigCommand()
            }
        }
    }

    fun LiteralArgumentBuilder<FabricClientCommandSource>.declareImportCommand() {
        literal("import") {
            requiredArgument("file", StringArgumentType.greedyString()) {
                runs {
                    val file = StringArgumentType.getString(this, "file").let {
                        if (it.startsWith('"') && it.endsWith('"')) {
                            it.substring(1..<it.lastIndex)
                        } else {
                            it
                        }
                    }

                    val path = try {
                        Path(file)
                    } catch (_: Exception) {
                        try {
                            URI(file)
                        } catch (_: Exception) {
                            null
                        }
                    }

                    if (path == null) {
                        source.sendFeedback(Component.translatable("commands.wiziplicity.import.file_not_found"))

                        return@runs Command.SINGLE_SUCCESS
                    }

                    val contents = if (path is Path) {
                        path.readText(charset = StandardCharsets.UTF_8)
                    } else {
                        null
                    }

                    if (contents == null) {
                        source.sendFeedback(Component.translatable("commands.wiziplicity.import.file_not_readable"))

                        return@runs Command.SINGLE_SUCCESS
                    }

                    val systemData = try {
                        ignoreKeysJson.decodeFromString<PluralKitDataExport>(contents)
                    } catch (_: Exception) {
                        source.sendFeedback(Component.translatable("commands.wiziplicity.import.file_not_valid"))

                        return@runs Command.SINGLE_SUCCESS
                    }

                    ConfigHolder.config.headmates.clear()

                    systemData.members.forEach { headmate ->
                        ConfigHolder.config.headmates[headmate.name] = Headmate(
                                pronouns = headmate.pronouns,
                                color =  headmate.color?.let { "#$it" },
                                proxytags = headmate.proxyTags.map { it.toConfigProxy() }.toMutableList()
                        )
                    }

                    ConfigHolder.changed = true

                    source.sendFeedback(Component.translatable("commands.wiziplicity.import.success"))

                    Command.SINGLE_SUCCESS
                }
            }
        }
    }

    fun LiteralArgumentBuilder<FabricClientCommandSource>.declareMemberCommand(registerShorthands: Boolean = false) {
        val memberCommand = literal("member") {
            literal("new") {
                requiredArgument("id", StringArgumentType.string()) {
                    runs {
                        val id = StringArgumentType.getString(this, "id")

                        if (ConfigHolder.createHeadmate(id)) {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.new.success", id).withWiziplicityPrefix())
                        } else {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.new.failure", id).withWiziplicityPrefix())
                        }

                        Command.SINGLE_SUCCESS
                    }
                }
            }

            literal("list") {
                runs {
                    val headmates = ConfigHolder.config.headmates

                    if (headmates.isEmpty()) {
                        source.sendFeedback(Component.translatable("command.wiziplicity.member.list.empty").withWiziplicityPrefix())
                    } else {
                        source.sendFeedback(Component.translatable("command.wiziplicity.member.list.header").withWiziplicityPrefix())
                        ConfigHolder.config.headmates.forEach { (id, headmate) ->
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.list.line", id))
                        }
                    }

                    Command.SINGLE_SUCCESS
                }
            }

            requiredArgument("id", HeadmateArgumentType.headmate()) {
                val renameAction: LiteralArgumentBuilder<FabricClientCommandSource>.() -> Unit = {
                    requiredArgument("newid", StringArgumentType.string()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val newId = StringArgumentType.getString(this, "newid")

                            if (ConfigHolder.renameHeadmate(headmate.first, newId)) {
                                source.sendFeedback(Component.translatable("command.wiziplicity.member.rename.success", headmate.first, newId).withWiziplicityPrefix())
                            } else {
                                source.sendFeedback(Component.translatable("command.wiziplicity.member.rename.failure", headmate.first, newId).withWiziplicityPrefix())
                            }

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                val displayNameAction: LiteralArgumentBuilder<FabricClientCommandSource>.() -> Unit = {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                        val displayName = headmate.second.name

                        if (displayName != null) {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.displayname.success", headmate.first, displayName).withWiziplicityPrefix())
                        } else {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.displayname.failure", headmate.first).withWiziplicityPrefix())
                        }

                        Command.SINGLE_SUCCESS
                    }

                    requiredArgument("name", StringArgumentType.string()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val displayName = StringArgumentType.getString(this, "name")

                            headmate.second.name = displayName
                            ConfigHolder.changed = true

                            source.sendFeedback(Component.translatable("command.wiziplicity.member.displayname.set", headmate.first, displayName).withWiziplicityPrefix())

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                val colorAction: LiteralArgumentBuilder<FabricClientCommandSource>.() -> Unit  = {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                        val color = headmate.second.color

                        if (color != null) {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.color.success", headmate.first, color).withWiziplicityPrefix())
                        } else {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.color.failure", headmate.first).withWiziplicityPrefix())
                        }

                        Command.SINGLE_SUCCESS
                    }

                    requiredArgument("color", ColorArgumentType.color()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val color = ColorArgumentType.getColor(this, "color")

                            headmate.second.color = color
                            ConfigHolder.changed = true

                            source.sendFeedback(Component.translatable("command.wiziplicity.member.color.set", headmate.first, color).withWiziplicityPrefix())

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                val proxyAction: LiteralArgumentBuilder<FabricClientCommandSource>.() -> Unit = {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")

                        val proxys = headmate.second.proxytags

                        if (proxys.isEmpty()) {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.proxy.empty", headmate.first).withWiziplicityPrefix())
                        } else {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.proxy.header", headmate.first).withWiziplicityPrefix())
                            proxys.forEach {
                                source.sendFeedback(Component.translatable("command.wiziplicity.member.proxy.line", it.toString()))
                            }
                        }

                        Command.SINGLE_SUCCESS
                    }

                    literal("add") {
                        requiredArgument("proxy", StringArgumentType.string()) {
                            runs {
                                val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                                val proxy = StringArgumentType.getString(this, "proxy")

                                try {
                                    val proxyObj = Proxy.of(proxy, ::RuntimeException)

                                    if (headmate.second.addProxy(proxyObj)) {
                                        source.sendFeedback(Component.translatable("command.wiziplicity.member.proxy.add.success", proxyObj.toString(), headmate.first).withWiziplicityPrefix())

                                        ConfigHolder.changed = true
                                    } else {
                                        source.sendFeedback(Component.translatable("command.wiziplicity.member.proxy.add.exists", headmate.first, proxyObj.toString()).withWiziplicityPrefix())
                                    }
                                } catch (error: Exception) {
                                    source.sendFeedback(Component.translatable("command.wiziplicity.member.proxy.add.failure", proxy).withWiziplicityPrefix())
                                }

                                Command.SINGLE_SUCCESS
                            }
                        }
                    }

                    literal("remove") {
                        requiredArgument("proxy", StringArgumentType.string()) {
                            runs {
                                val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                                val proxy = StringArgumentType.getString(this, "proxy")

                                try {
                                    val proxyObj = Proxy.of(proxy, ::RuntimeException)

                                    if (headmate.second.removeProxy(proxyObj)) {
                                        source.sendFeedback(Component.translatable("command.wiziplicity.member.proxy.remove.success", headmate.first).withWiziplicityPrefix())

                                        ConfigHolder.changed = true
                                    } else {
                                        source.sendFeedback(Component.translatable("command.wiziplicity.member.proxy.remove.missing", headmate.first).withWiziplicityPrefix())
                                    }
                                } catch (error: Exception) {
                                    source.sendFeedback(Component.translatable("command.wiziplicity.member.proxy.remove.failure", proxy).withWiziplicityPrefix())
                                }

                                Command.SINGLE_SUCCESS
                            }
                        }
                    }

                    requiredArgument("proxy", StringArgumentType.string()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val proxy = StringArgumentType.getString(this, "proxy")

                            try {
                                val proxyObj = Proxy.of(proxy, ::RuntimeException)

                                headmate.second.setProxy(proxyObj)
                                source.sendFeedback(Component.translatable("command.wiziplicity.member.proxy.set.success", headmate.first, proxyObj.toString()).withWiziplicityPrefix())
                                ConfigHolder.changed = true
                            } catch (error: Exception) {
                                source.sendFeedback(Component.translatable("command.wiziplicity.member.proxy.set.failure", proxy).withWiziplicityPrefix())
                                Main.logger.error("POYO! ", error)
                            }

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("rename", renameAction)
                literal("displayname", displayNameAction)
                literal("color", colorAction)
                literal("colour", colorAction)
                literal("proxy", proxyAction)
                literal("proxytags", proxyAction)

                if (registerShorthands) {
                    literal("rn", renameAction)
                    literal("dn", displayNameAction)
                }

                literal("delete") {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")

                        ConfigHolder.deleteHeadmate(headmate.first)
                        source.sendFeedback(Component.translatable("command.wiziplicity.member.delete.success", headmate.first).withWiziplicityPrefix())

                        Command.SINGLE_SUCCESS
                    }
                }

                literal("pronouns") {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                        val pronouns = headmate.second.pronouns

                        if (pronouns != null) {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.pronouns.success", headmate.first, pronouns).withWiziplicityPrefix())
                        } else {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.pronouns.failure", headmate.first).withWiziplicityPrefix())
                        }

                        Command.SINGLE_SUCCESS
                    }

                    requiredArgument("pronouns", StringArgumentType.string()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val pronouns = StringArgumentType.getString(this, "pronouns")

                            headmate.second.pronouns = pronouns
                            ConfigHolder.changed = true

                            source.sendFeedback(Component.translatable("command.wiziplicity.member.pronouns.set", headmate.first, pronouns).withWiziplicityPrefix())

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("skin") {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                        val skin = headmate.second.skin

                        if (skin != null) {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.skin.success", headmate.first, skin).withWiziplicityPrefix())
                        } else {
                            source.sendFeedback(Component.translatable("command.wiziplicity.member.skin.failure", headmate.first).withWiziplicityPrefix())
                        }

                        Command.SINGLE_SUCCESS
                    }

                    requiredArgument("url", StringArgumentType.greedyString()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val url = StringArgumentType.getString(this, "url")

                            headmate.second.skin = url
                            ConfigHolder.changed = true

                            source.sendFeedback(Component.translatable("command.wiziplicity.member.skin.set", headmate.first, url).withWiziplicityPrefix())

                            Command.SINGLE_SUCCESS
                        }
                    }
                }
            }
        }

        if (registerShorthands) {
            literal("m") { redirect(memberCommand) }
        }
    }

    fun LiteralArgumentBuilder<FabricClientCommandSource>.declareSwitchCommand(registerShorthands: Boolean = false) {
        val switchCommand = literal<FabricClientCommandSource>("switch") {
            literal("out") {
                runs {
                    Minecraft.getInstance().connection?.sendCommand("nick clear")

                    source.sendFeedback(Component.translatable("commands.wiziplicity.switch.out.success").withWiziplicityPrefix())

                    Command.SINGLE_SUCCESS
                }
            }

            requiredArgument("headmate", HeadmateArgumentType.headmate()) {
                runs {
                    val headmate = HeadmateArgumentType.getHeadmate(this, "headmate")
                    Minecraft.getInstance().connection?.sendCommand("nick set ${headmate.second.getStyledNickname()}")

                    source.sendFeedback(Component.translatable("commands.wiziplicity.switch.headmate.success", headmate.first).withWiziplicityPrefix())

                    Command.SINGLE_SUCCESS
                }
            }
        }

        if (registerShorthands) {
            literal("sw") { redirect(switchCommand) }
        }
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.declareConfigCommand() {
        literal("config") {
            literal("global") {
                literal("nickname_format") {
                    literal("with_pronouns") {
                        requiredArgument("format", StringArgumentType.greedyString()) {
                            runs {
                                val format = StringArgumentType.getString(this, "format")

                                val tokens = ConfigHolder.getTokens(format, allowPronouns = true)

                                if (tokens.invalid.isEmpty() && tokens.valid.isNotEmpty()) {
                                    ConfigHolder.setNickNameFormatWithPronouns(format)

                                    source.sendFeedback(Component.translatable("commands.wiziplicity.config.global.nickname_format.success").withWiziplicityPrefix())
                                } else {
                                    if (tokens.valid.isEmpty()) {
                                        source.sendFeedback(Component.translatable("commands.wiziplicity.config.global.nickname_format.no_valid_tokens").withWiziplicityPrefix())
                                    } else {
                                        source.sendFeedback(Component.translatable("commands.wiziplicity.config.global.nickname_format.invalid_tokens", tokens.invalid.joinToString { ", " }).withWiziplicityPrefix())
                                    }
                                }

                                Command.SINGLE_SUCCESS
                            }
                        }
                    }

                    literal("no_pronouns") {
                        requiredArgument("format", StringArgumentType.greedyString()) {
                            runs {
                                val format = StringArgumentType.getString(this, "format")

                                val tokens = ConfigHolder.getTokens(format, allowPronouns = false)

                                if (tokens.invalid.isEmpty() && tokens.valid.isNotEmpty()) {
                                    ConfigHolder.setNickNameFormatNoPronouns(format)

                                    source.sendFeedback(Component.translatable("commands.wiziplicity.config.global.nickname_format.success").withWiziplicityPrefix())
                                } else {
                                    if (tokens.valid.isEmpty()) {
                                        source.sendFeedback(Component.translatable("commands.wiziplicity.config.global.nickname_format.no_valid_tokens").withWiziplicityPrefix())
                                    } else {
                                        source.sendFeedback(Component.translatable("commands.wiziplicity.config.global.nickname_format.invalid_tokens").withWiziplicityPrefix())
                                    }
                                }

                                Command.SINGLE_SUCCESS
                            }
                        }
                    }
                }

                literal("skin_change_delay") {
                    requiredArgument("delay", IntegerArgumentType.integer(0)) {
                        runs {
                            val delay = IntegerArgumentType.getInteger(this, "delay")

                            ConfigHolder.skinChangeDelay(delay)

                            source.sendFeedback(Component.translatable("commands.wiziplicity.config.global.skin_change_delay", delay).withWiziplicityPrefix())

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("preserve_last_fronter") {
                    requiredArgument("boolean", BoolArgumentType.bool()) {
                        runs {
                            val preserveLastFronter = BoolArgumentType.getBool(this, "boolean")

                            ConfigHolder.preserveLastFronter(preserveLastFronter)

                            if (preserveLastFronter) {
                                source.sendFeedback(Component.translatable("commands.wiziplicity.config.global.preserve_last_fronter.true").withWiziplicityPrefix())
                            } else {
                                source.sendFeedback(Component.translatable("commands.wiziplicity.config.global.preserve_last_fronter.false").withWiziplicityPrefix())
                            }

                            Command.SINGLE_SUCCESS
                        }
                    }
                }
            }

            literal("server") {
                literal("skin_change_delay") {
                    requiredArgument("delay", IntegerArgumentType.integer(0)) {
                        runs {
                            val delay = IntegerArgumentType.getInteger(this, "delay")

                            val ip = ConfigHolder.configureServer() {
                                skinChangeDelay = delay
                            }

                            source.sendFeedback(Component.translatable("commands.wiziplicity.config.server.skin_change_delay", ip, delay).withWiziplicityPrefix())

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("alias") {
                    requiredArgument("server", ServerArgumentType.server()) {
                        runs {
                            val server = ServerArgumentType.getServer(this, "server")

                            val ip = ConfigHolder.createAlias(to = server.first)

                            source.sendFeedback(Component.translatable("commands.wiziplicity.config.server.alias", ip, server.first).withWiziplicityPrefix())

                            Command.SINGLE_SUCCESS
                        }
                    }
                }
            }
        }
    }
}
