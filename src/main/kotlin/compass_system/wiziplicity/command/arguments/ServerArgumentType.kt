package compass_system.wiziplicity.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import compass_system.wiziplicity.config.ConfigHolder
import compass_system.wiziplicity.config.ServerSettings
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

class ServerArgumentType : ArgumentType<ServerSettings> {
    private val invalidValueError = DynamicCommandExceptionType { Component.translatable("argument.wiziplicity.server.invalid", it) }

    override fun parse(reader: StringReader): ServerSettings {
        val id = reader.readString()

        return ConfigHolder.config.serverSettings[id] ?: throw invalidValueError.create(id)
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        if (context.source is SharedSuggestionProvider) {
            val servers = ConfigHolder.config.serverSettings.keys

            return if (servers.isNotEmpty()) {
                SharedSuggestionProvider.suggest(servers, builder)
            } else {
                Suggestions.empty()
            }
        }

        return Suggestions.empty()
    }

    companion object {
        fun server() = ServerArgumentType()
        fun getServer(context: CommandContext<FabricClientCommandSource>, name: String): ServerSettings = context.getArgument(name, ServerSettings::class.java)
    }
}