package compass_system.wiziplicity.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import compass_system.wiziplicity.config.FixPosition
import compass_system.wiziplicity.config.Proxy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import java.lang.IllegalStateException

private val PREFIX = Component.literal("")
        .append(Component.translatable("wiziplicity.mod_name").withStyle(ChatFormatting.DARK_AQUA))
        .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))

fun Component.withWiziplicityPrefix(): Component = PREFIX.copy().append(this)

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

@Serializable
data class PluralKitDataExport(
        val members: List<PluralKitMember>
)

@Serializable
data class PluralKitMember(
        val name: String,
        val color: String?,
        val pronouns: String?,
        @SerialName("proxy_tags")
        val proxyTags: List<PluralKitProxy>
)

@Serializable
data class PluralKitProxy(
        val prefix: String? = null,
        val suffix: String? = null
) {
    fun toConfigProxy(): Proxy {
        return if (prefix != null && suffix == null) {
            Proxy(prefix, FixPosition.START)
        } else if (suffix != null && prefix == null) {
            Proxy(suffix, FixPosition.END)
        } else {
            throw IllegalStateException("Illegal plural kit proxy.")
        }
    }
}