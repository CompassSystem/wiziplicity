package compass_system.wiziplicity

import compass_system.wiziplicity.config.ConfigHolder
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Main : ClientModInitializer {
    internal val logger: Logger = LoggerFactory.getLogger("Wiziplicity")

    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register {
            ConfigHolder.load()

            println(ConfigHolder.config)
        }

        // todo: Should we use a shutdown hook instead?
        ClientLifecycleEvents.CLIENT_STOPPING.register {
            ConfigHolder.save()

            println(ConfigHolder.config)
        }
    }
}