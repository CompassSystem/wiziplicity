package compass_system.wiziplicity

import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Main : ClientModInitializer {
    private val logger: Logger = LoggerFactory.getLogger("Wiziplicity")

    override fun onInitializeClient() {
        logger.info("Hello Minecraft!")
    }
}