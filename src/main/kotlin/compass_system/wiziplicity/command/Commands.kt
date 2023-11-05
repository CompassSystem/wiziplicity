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
import compass_system.wiziplicity.config.ConfigHolder
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

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

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.declareImportCommand() {
        literal("import") {
            requiredArgument("file", StringArgumentType.greedyString()) {
                runs {
                    val file = StringArgumentType.getString(this, "file")

                    Command.SINGLE_SUCCESS
                }
            }
        }
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.declareMemberCommand() {
        literal("member") {
            literal("new") {
                requiredArgument("id", StringArgumentType.string()) {
                    runs {
                        val id = StringArgumentType.getString(this, "id")

                        Command.SINGLE_SUCCESS
                    }
                }
            }

            literal("list") {
                runs {
                    Command.SINGLE_SUCCESS
                }
            }

            requiredArgument("id", HeadmateArgumentType.headmate()) {
                literal("rename") {
                    requiredArgument("newid", StringArgumentType.string()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val newId = StringArgumentType.getString(this, "newid")

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("delete") {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")

                        Command.SINGLE_SUCCESS
                    }
                }

                literal("displayname") {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")

                        Command.SINGLE_SUCCESS
                    }

                    requiredArgument("name", StringArgumentType.string()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val displayName = StringArgumentType.getString(this, "name")

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                val colorCommand = literal("color") {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")

                        Command.SINGLE_SUCCESS
                    }

                    requiredArgument("color", ColorArgumentType.color()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val color = ColorArgumentType.getColor(this, "color")

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("colour") { redirect(colorCommand) }

                literal("pronouns") {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")

                        Command.SINGLE_SUCCESS
                    }

                    requiredArgument("pronouns", StringArgumentType.string()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val pronouns = StringArgumentType.getString(this, "pronouns")

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                val proxyCommand = literal("proxy") {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")

                        Command.SINGLE_SUCCESS
                    }

                    literal("add") {
                        requiredArgument("proxy", StringArgumentType.string()) {
                            runs {
                                val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                                val proxy = StringArgumentType.getString(this, "proxy")

                                Command.SINGLE_SUCCESS
                            }
                        }
                    }

                    literal("remove") {
                        requiredArgument("proxy", StringArgumentType.string()) {
                            runs {
                                val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                                val proxy = StringArgumentType.getString(this, "proxy")

                                Command.SINGLE_SUCCESS
                            }
                        }
                    }

                    requiredArgument("proxy", StringArgumentType.string()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val proxy = StringArgumentType.getString(this, "proxy")

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("proxytags") { redirect(proxyCommand) }

                literal("skin") {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")

                        Command.SINGLE_SUCCESS
                    }

                    requiredArgument("url", StringArgumentType.greedyString()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val url = StringArgumentType.getString(this, "url")

                            Command.SINGLE_SUCCESS
                        }
                    }
                }
            }
        }
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.declareSwitchCommand() {
        literal("switch") {
            literal("out") {
                runs {
                    Command.SINGLE_SUCCESS
                }
            }

            requiredArgument("headmate", HeadmateArgumentType.headmate()) {
                runs {
                    val headmate = HeadmateArgumentType.getHeadmate(this, "headmate")

                    Command.SINGLE_SUCCESS
                }
            }
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

                                    source.sendFeedback(Component.translatable("command.wiziplicity.config.global.nickname_format.with_pronouns.success").withWiziplicityPrefix())
                                } else {
                                    if (tokens.valid.isEmpty()) {
                                        source.sendFeedback(Component.translatable("command.wiziplicity.config.global.nickname_format.with_pronouns.no_valid_tokens").withWiziplicityPrefix())
                                    } else {
                                        source.sendFeedback(Component.translatable("command.wiziplicity.config.global.nickname_format.with_pronouns.invalid_tokens").withWiziplicityPrefix())
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

                                    source.sendFeedback(Component.translatable("command.wiziplicity.config.global.nickname_format.no_pronouns.success").withWiziplicityPrefix())
                                } else {
                                    source.sendFeedback(Component.translatable("command.wiziplicity.config.global.nickname_format.no_pronouns.failure").withWiziplicityPrefix())
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

                            source.sendFeedback(Component.translatable("command.wiziplicity.config.global.skin_change_delay", delay).withWiziplicityPrefix())

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("preserve_last_fronter") {
                    requiredArgument("boolean", BoolArgumentType.bool()) {
                        runs {
                            val preserveLastFronter = BoolArgumentType.getBool(this, "boolean")

                            ConfigHolder.preserveLastFronter(preserveLastFronter)

                            source.sendFeedback(Component.translatable("command.wiziplicity.config.global.preserve_last_fronter", preserveLastFronter).withWiziplicityPrefix())

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

                            source.sendFeedback(Component.translatable("command.wiziplicity.config.server.skin_change_delay", ip, delay).withWiziplicityPrefix())

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("alias") {
                    requiredArgument("server", ServerArgumentType.server()) {
                        runs {
                            val server = ServerArgumentType.getServer(this, "server")

                            val ip = ConfigHolder.createAlias(to = server.first)

                            source.sendFeedback(Component.translatable("command.wiziplicity.config.server.alias", server.first, ip).withWiziplicityPrefix())

                            Command.SINGLE_SUCCESS
                        }
                    }
                }
            }
        }
    }
}
