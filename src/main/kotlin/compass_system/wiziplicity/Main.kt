package compass_system.wiziplicity

import compass_system.wiziplicity.command.Commands
import compass_system.wiziplicity.config.ConfigHolder
import compass_system.wiziplicity.config.FrontHolder
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Main : ClientModInitializer {
    internal const val MOD_ID = "wiziplicity"
    internal val logger: Logger = LoggerFactory.getLogger("Wiziplicity")

    override fun onInitializeClient() {
        Commands.register()

        ClientLifecycleEvents.CLIENT_STARTED.register {
            ConfigHolder.load()
            FrontHolder.load()
        }

        // todo: Should we use a shutdown hook instead?
        ClientLifecycleEvents.CLIENT_STOPPING.register {
            ConfigHolder.save()
            FrontHolder.save()
        }
    }
}
