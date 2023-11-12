package compass_system.wiziplicity

import compass_system.wiziplicity.command.Commands
import compass_system.wiziplicity.config.ConfigHolder
import compass_system.wiziplicity.config.FixPosition
import compass_system.wiziplicity.config.Proxy
import compass_system.wiziplicity.misc.FrontHolder
import compass_system.wiziplicity.misc.CommandQueue
import compass_system.wiziplicity.misc.SendMessage
import compass_system.wiziplicity.misc.ServerJoin
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Main : ClientModInitializer {
    internal const val MOD_ID = "wiziplicity"
    internal val logger: Logger = LoggerFactory.getLogger("Wiziplicity")
    private var tickCounter = 0

    override fun onInitializeClient() {
        Commands.register()

        ClientLifecycleEvents.CLIENT_STARTED.register {
            ConfigHolder.load()
            FrontHolder.load()
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            saveUserData()
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            CommandQueue.add(ServerJoin)
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            tickCounter = 0

            saveUserData()

            CommandQueue.reset()
        }

        ClientTickEvents.END_WORLD_TICK.register {
            if (tickCounter % 5 == 0) { // Every 1/4 second.
                CommandQueue.processQueue()
            }

            if (tickCounter % 30 * 20 == 0) { // Every 30 seconds.
                saveUserData()
            }

            tickCounter++
        }
    }

    private fun saveUserData() {
        ConfigHolder.save()
        FrontHolder.save()
    }

    fun currentServer() = Minecraft.getInstance().currentServer?.ip ?: "singleplayer"

    fun proxyMessage(message: String): Boolean {
        val proxies: List<Pair<String, Proxy>> = buildList {
            ConfigHolder.config.headmates.forEach { (id, headmate) ->
                headmate.proxytags.let {
                    if (it.isNotEmpty()) {
                        it.forEach { proxy -> add(Pair(id, proxy)) }
                    }
                }
            }
        }.sortedByDescending { it.second.fix.length }

        for (identifiedProxy in proxies) {
            val headmate = identifiedProxy.first
            val proxy = identifiedProxy.second

            if (proxy in message) {
                if (proxy.fixPosition == FixPosition.START) {
                    CommandQueue.add(SendMessage(headmate, message.takeLast(message.length - proxy.fix.length).trimStart()))
                } else {
                    CommandQueue.add(SendMessage(headmate, message.take(message.length - proxy.fix.length).trimEnd()))
                }

                return true
            }
        }

        val intendedFront = FrontHolder.getIntendedFront()
        if (FrontHolder.getNicknameFront() != intendedFront) {
            CommandQueue.add(SendMessage(intendedFront, message))
            return true
        }

        return false
    }
}

operator fun String.contains(proxy: Proxy): Boolean {
    return if (ConfigHolder.config.caseSensitiveProxies) {
        if (proxy.fixPosition == FixPosition.START) {
            this.startsWith(proxy.fix)
        } else {
            this.endsWith(proxy.fix)
        }
    } else {
        val message = this.lowercase()

        if (proxy.fixPosition == FixPosition.START) {
            message.startsWith(proxy.fix.lowercase())
        } else {
            message.endsWith(proxy.fix.lowercase())
        }
    }
}