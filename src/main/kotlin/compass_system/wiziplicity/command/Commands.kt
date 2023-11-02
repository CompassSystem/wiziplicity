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
import compass_system.wiziplicity.Main
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object Commands {
    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, context ->
            dispatcher.register(Main.MOD_ID) {
                declareImportCommand()
                declareSwitchCommand()
                declareConfigCommand()
            }
        }
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.declareImportCommand() {
        literal("import") {
            requiredArgument("file", StringArgumentType.greedyString()) {
                runs {
                    Command.SINGLE_SUCCESS
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

            // todo: We really want our own argument type here.
            requiredArgument("headmate", StringArgumentType.string()) {
                runs {
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
                                Command.SINGLE_SUCCESS
                            }
                        }
                    }

                    literal("no_pronouns") {
                        requiredArgument("format", StringArgumentType.greedyString()) {
                            runs {
                                Command.SINGLE_SUCCESS
                            }
                        }
                    }
                }

                literal("skin_change_delay") {
                    requiredArgument("delay", IntegerArgumentType.integer(0)) {
                        runs {
                            Command.SINGLE_SUCCESS
                        }
                    }
                }
            }

            literal("server") {
                literal("skin_change_delay") {
                    requiredArgument("delay", IntegerArgumentType.integer(0)) {
                        runs {
                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("alias") {
                    // todo: We really want our own argument type here.
                    requiredArgument("server", StringArgumentType.string()) {
                        runs {
                            Command.SINGLE_SUCCESS
                        }
                    }
                }
            }
        }
    }
}

internal fun <Source> ArgumentBuilder<Source, *>.literal(name: String, action: LiteralArgumentBuilder<Source>.() -> Unit) {
    val argument = LiteralArgumentBuilder.literal<Source>(name)
    action.invoke(argument)
    then(argument)
}

internal fun <Source> CommandDispatcher<Source>.register(
        name: String,
        action: LiteralArgumentBuilder<Source>.() -> Unit
) {
    val argument = LiteralArgumentBuilder.literal<Source>(name)
    action.invoke(argument)
    register(argument)
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