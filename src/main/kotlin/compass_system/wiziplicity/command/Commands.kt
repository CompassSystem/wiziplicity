package compass_system.wiziplicity.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import compass_system.wiziplicity.Main
import compass_system.wiziplicity.command.arguments.ColorArgumentType
import compass_system.wiziplicity.command.arguments.HeadmateArgumentType
import compass_system.wiziplicity.command.arguments.ServerArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

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

                                Command.SINGLE_SUCCESS
                            }
                        }
                    }

                    literal("no_pronouns") {
                        requiredArgument("format", StringArgumentType.greedyString()) {
                            runs {
                                val format = StringArgumentType.getString(this, "format")

                                Command.SINGLE_SUCCESS
                            }
                        }
                    }
                }

                literal("skin_change_delay") {
                    requiredArgument("delay", IntegerArgumentType.integer(0)) {
                        runs {
                            val delay = IntegerArgumentType.getInteger(this, "delay")

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

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("alias") {
                    requiredArgument("server", ServerArgumentType.server()) {
                        runs {
                            val server = ServerArgumentType.getServer(this, "server")

                            Command.SINGLE_SUCCESS
                        }
                    }
                }
            }
        }
    }
}

internal fun <Source> ArgumentBuilder<Source, *>.literal(
        name: String,
        action: LiteralArgumentBuilder<Source>.() -> Unit
): CommandNode<Source> {
    val node = LiteralArgumentBuilder.literal<Source>(name).apply(action).build()

    then(node)

    return node
}

internal fun <Source> CommandDispatcher<Source>.register(
        name: String,
        action: LiteralArgumentBuilder<Source>.() -> Unit
): LiteralCommandNode<Source> {
    val argument = LiteralArgumentBuilder.literal<Source>(name)
    action.invoke(argument)
    return register(argument)
}

internal fun <Source, Type2> ArgumentBuilder<Source, *>.requiredArgument(
        name: String,
        type: ArgumentType<Type2>,
        action: ArgumentBuilder<Source, *>.() -> Unit
) {
    val argument = RequiredArgumentBuilder.argument<Source, Type2>(name, type)
    action.invoke(argument)
    then(argument)
}


internal fun <Source, Builder : ArgumentBuilder<Source, Builder>> ArgumentBuilder<Source, Builder>.runs(
        action: CommandContext<Source>.() -> Int
) {
    executes {
        action.invoke(it)
    }
}