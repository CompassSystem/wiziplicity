package compass_system.wiziplicity.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import compass_system.wiziplicity.config.ConfigHolder
import compass_system.wiziplicity.config.Headmate
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

class HeadmateArgumentType : ArgumentType<Headmate> {
    private val invalidValueError = DynamicCommandExceptionType { Component.translatable("argument.wiziplicity.headmate.invalid", it) }

    override fun parse(reader: StringReader): Headmate {
        val id = reader.readString()

        return ConfigHolder.config.headmates[id] ?: throw invalidValueError.create(id)
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        if (context.source is SharedSuggestionProvider) {
            val headmates = ConfigHolder.config.headmates.keys

            return if (headmates.isNotEmpty()) {
                SharedSuggestionProvider.suggest(headmates, builder)
            } else {
                Suggestions.empty()
            }
        }

        return Suggestions.empty()
    }

    companion object {
        fun headmate() = HeadmateArgumentType()
        fun getHeadmate(context: CommandContext<FabricClientCommandSource>, name: String): Headmate = context.getArgument(name, Headmate::class.java)
    }
}