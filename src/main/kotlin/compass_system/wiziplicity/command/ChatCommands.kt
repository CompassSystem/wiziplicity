package compass_system.wiziplicity.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import compass_system.wiziplicity.Main
import compass_system.wiziplicity.command.arguments.ColorArgumentType
import compass_system.wiziplicity.command.arguments.HeadmateArgumentType
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object ChatCommands {
    private var lastUsedCommandPrefix = ""
    private val chatCommandDispatcher = CommandDispatcher<FabricClientCommandSource>().apply {
        funCommand("mn")
        funCommand("fire", italics = true)
        funCommand("thunder", italics = true)
        funCommand("freeze", italics = true)
        funCommand("starstorm", italics = true)
        funCommand("flash", italics = true)
        funCommand("rool", italics = true)

        declareImportCommand()
        declareMemberCommand()
        declareSwitchCommand()
    }

    private fun CommandDispatcher<FabricClientCommandSource>.funCommand(name: String, italics: Boolean = false) {
        register(name) {
            runs {
                source.sendFeedback(Component.translatable("commands.wiziplicity.$name").apply {
                    if (italics) {
                        withStyle(ChatFormatting.ITALIC)
                    }
                }.withWiziplicityPrefix())
                Command.SINGLE_SUCCESS
            }
        }
    }

    private fun CommandDispatcher<FabricClientCommandSource>.declareImportCommand() {
        register("import") {
            requiredArgument("file", StringArgumentType.greedyString()) {
                runs {
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }

    private fun CommandDispatcher<FabricClientCommandSource>.declareMemberCommand() {
        val memberCommand = register("member") {
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
                val renameCommand = literal("rename") {
                    requiredArgument("newid", StringArgumentType.string()) {
                        runs {
                            val headmate = HeadmateArgumentType.getHeadmate(this, "id")
                            val newId = StringArgumentType.getString(this, "newid")

                            Command.SINGLE_SUCCESS
                        }
                    }
                }

                literal("rn") { redirect(renameCommand) }

                literal("delete") {
                    runs {
                        val headmate = HeadmateArgumentType.getHeadmate(this, "id")

                        Command.SINGLE_SUCCESS
                    }
                }

                val displayNameCommand = literal("displayname") {
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

                literal("dn") { redirect(displayNameCommand) }

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

        register("m") { redirect(memberCommand) }
    }

    private fun CommandDispatcher<FabricClientCommandSource>.declareSwitchCommand() {
        val switchCommand = register("switch") {
            literal("out") {
                runs {
                    Command.SINGLE_SUCCESS
                }
            }

            requiredArgument("headmate", HeadmateArgumentType.headmate()) {
                runs {
                    val headmate = HeadmateArgumentType.getHeadmate(this, "headmate")

                    println(headmate)

                    Command.SINGLE_SUCCESS
                }
            }
        }

        register("sw") { redirect(switchCommand) }
    }

    @JvmStatic
    fun parseChatCommand(message: String): Boolean {
        if (message.startsWith("pk;") || message.startsWith("pk!")) {
            val prefix = message.take(3)
            val command = message.takeLast(message.length - 3)

            lastUsedCommandPrefix = prefix // todo: would like to make this part of the command source.
            val commandSource = Minecraft.getInstance().connection!!.suggestionsProvider as FabricClientCommandSource

            try {
                chatCommandDispatcher.execute(command, commandSource)
            } catch (error: Exception) {
                Main.logger.error("Error executing command: ", error)
            }

            return true
        }

        return false
    }
}
