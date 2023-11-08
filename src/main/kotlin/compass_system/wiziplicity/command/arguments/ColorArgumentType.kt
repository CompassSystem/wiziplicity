package compass_system.wiziplicity.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.ChatFormatting
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.ColorArgument
import java.util.concurrent.CompletableFuture

class ColorArgumentType : ArgumentType<String> {
    private val hexRegex = "^#[a-fA-F0-9]{6}$".toRegex()
    private val colours: Set<String> = ChatFormatting.entries.filter { it.isColor }.map { it.name.lowercase() }.toSet() + setOf("orange", "grey", "pink", "dark_grey")

    override fun parse(reader: StringReader): String {
        val value = reader.readTillSpace()

        if (value in colours || isValidHexColour(value)) {
            return value
        }

        throw ColorArgument.ERROR_INVALID_VALUE.create(value)
    }

    private fun StringReader.readTillSpace(): String {
        if (!canRead()) {
            return ""
        }

        return buildString {
            while (canRead()) {
                val char = peek()

                if (char == ' ') {
                    break
                }

                append(read())
            }
        }
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        if (context.source is SharedSuggestionProvider) {
            return SharedSuggestionProvider.suggest(colours, builder)
        }

        return Suggestions.empty()
    }

    private fun isValidHexColour(value: String): Boolean {
        return value.length == 7 && hexRegex.matches(value)
    }

    companion object {
        fun color() = ColorArgumentType()
        fun getColor(context: CommandContext<FabricClientCommandSource>, name: String): String = context.getArgument(name, String::class.java)
    }
}