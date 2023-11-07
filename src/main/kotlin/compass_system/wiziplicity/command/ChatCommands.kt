package compass_system.wiziplicity.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.RootCommandNode
import compass_system.wiziplicity.Main
import compass_system.wiziplicity.command.Commands.declareImportCommand
import compass_system.wiziplicity.command.Commands.declareMemberCommand
import compass_system.wiziplicity.command.Commands.declareSwitchCommand
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandRuntimeException
import net.minecraft.network.chat.Component

object ChatCommands {
    private val chatCommandDispatcher = RootCommandNode<FabricClientCommandSource>().let { rootNode ->
        val fakeRoot = LiteralArgumentBuilder.literal<FabricClientCommandSource>("root")

        fakeRoot.apply {
            funCommand("mn")
            funCommand("fire", italics = true)
            funCommand("thunder", italics = true)
            funCommand("freeze", italics = true)
            funCommand("starstorm", italics = true)
            funCommand("flash", italics = true)
            funCommand("rool", italics = true)

            declareImportCommand()
            declareMemberCommand(registerShorthands = true)
            declareSwitchCommand(registerShorthands = true)
        }

        fakeRoot.arguments.forEach { rootNode.addChild(it) }

        CommandDispatcher(rootNode)
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.funCommand(name: String, italics: Boolean = false) {
        literal(name) {
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

    @JvmStatic
    fun parseChatCommand(message: String): Boolean {
        if (message.startsWith("pk;") || message.startsWith("pk!")) {
            val command = message.takeLast(message.length - 3)

            val commandSource = Minecraft.getInstance().connection!!.suggestionsProvider as FabricClientCommandSource

            try {
                chatCommandDispatcher.execute(command, commandSource)
            } catch (error: CommandRuntimeException) {
                commandSource.sendError(error.component)
            }
            catch (error: Exception) {
                Main.logger.error("Error executing command: ", error)
            }

            return true
        }

        return false
    }
}
