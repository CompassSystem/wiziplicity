package compass_system.wiziplicity.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

class SkinTypeArgumentType : ArgumentType<String> {
    private val invalidValueError = DynamicCommandExceptionType { Component.translatable("argument.wiziplicity.skin_type.invalid", it) }
    private val options = listOf("classic", "slim")

    override fun parse(reader: StringReader): String {
        val value = reader.readString()

        if (value in options) {
            return value
        } else {
            throw invalidValueError.create(value)
        }
    }

    override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        if (context.source is SharedSuggestionProvider) {
            return SharedSuggestionProvider.suggest(options, builder)
        }

        return Suggestions.empty()
    }

    companion object {
        fun skinType() = SkinTypeArgumentType()
        fun getSkinType(context: CommandContext<FabricClientCommandSource>, name: String): String = context.getArgument(name, String::class.java)
    }
}