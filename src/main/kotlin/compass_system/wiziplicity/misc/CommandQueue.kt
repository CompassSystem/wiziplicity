package compass_system.wiziplicity.misc

import compass_system.wiziplicity.config.ConfigHolder
import net.minecraft.client.Minecraft
import java.time.Instant

object CommandQueue {
    internal var nextSkinChange: Instant = Instant.MIN
    private val queue: MutableList<QueuedCommand> = mutableListOf()

    fun processQueue() {
        if (queue.isEmpty()) {
            return
        }

        val entryIndex = queue.indexOfFirst { it.canProcess() }

        if (entryIndex != -1) {
            val entry = queue.removeAt(entryIndex)

            entry.process().reversed().forEach { queue.add(entryIndex, it) }
        }
    }

    fun add(command: QueuedCommand) {
        if (command is Switch) {
            if (command.forMessage) {
                throw IllegalArgumentException("Switches for messages can't be added manually.")
            }

            queue.removeIf { it is SwitchSkin }
            queue.removeIf { it is Switch && !it.forMessage }
        }

        if (command is SendMessage) {
            if (command.canSendInstantly()) {
                command.process()
            }
        }

        queue.add(command)
    }

    fun reset() {
        nextSkinChange = Instant.MIN
        queue.clear()
    }
}

interface QueuedCommand {
    fun canProcess(): Boolean

    fun process(): List<QueuedCommand>

    fun sendCommand(command: String) = Minecraft.getInstance().connection!!.sendCommand(command)
}

data class Switch(val headmate: String?, val forMessage: Boolean = false) : QueuedCommand {
    override fun canProcess() = true

    override fun process(): List<QueuedCommand> {
        if (headmate == null) {
            sendCommand("nickname clear")

            FrontHolder.setNicknameHolder(null)
            FrontHolder.setIntendedHolder(null)

            return listOf(SwitchSkin(null))
        } else {
            sendCommand("nick set ${ConfigHolder.config.headmates[headmate]!!.getStyledNickname()}")

            return if (forMessage) {
                FrontHolder.setNicknameHolder(headmate)

                emptyList()
            } else {
                FrontHolder.setNicknameHolder(headmate)
                FrontHolder.setIntendedHolder(headmate)

                listOf(SwitchSkin(headmate))
            }
        }
    }
}

data class SwitchSkin(val headmate: String?) : QueuedCommand {
    override fun canProcess() = CommandQueue.nextSkinChange < Instant.now()

    override fun process(): List<QueuedCommand> {
        if (headmate == null) {
            sendCommand("skin clear")
        } else {
            ConfigHolder.config.headmates[headmate]!!.let {
                if (it.skin == null || it.skinType == null) {
                    sendCommand("skin clear")
                } else {
                    sendCommand("skin set URL ${it.skinType} ${it.skin}")
                }
            }
        }

        CommandQueue.nextSkinChange = Instant.now().plusSeconds(ConfigHolder.getSkinChangeDelay() + 1L)

        return emptyList()
    }
}

data class SendMessage(val headmate: String?, val message: String) : QueuedCommand {
    override fun canProcess() = true

    override fun process(): List<QueuedCommand> {
        val nicknameHolder = FrontHolder.getNicknameFront()

        return if (nicknameHolder == headmate) {
            Minecraft.getInstance().connection!!.sendChat(message)

            emptyList()
        } else {
            listOf(Switch(headmate, true), SendMessage(headmate, message), Switch(nicknameHolder, true))
        }
    }

    fun canSendInstantly() = FrontHolder.getNicknameFront() == headmate
}

object ServerJoin : QueuedCommand {
    override fun canProcess() = true

    override fun process(): List<QueuedCommand> {
        val intendedFront = FrontHolder.getIntendedFront()

        val skinNeedsUpdating = false // todo: add check
        val nickNeedsUpdating = FrontHolder.getNicknameFront() != intendedFront // todo: check if nickname doesn't match current fronter

        return if (skinNeedsUpdating && nickNeedsUpdating) {
            listOf(Switch(intendedFront))
        } else if (nickNeedsUpdating) {
            listOf(Switch(intendedFront, forMessage = true))
        } else if (skinNeedsUpdating) {
            listOf(SwitchSkin(intendedFront))
        } else {
            emptyList()
        }
    }
}