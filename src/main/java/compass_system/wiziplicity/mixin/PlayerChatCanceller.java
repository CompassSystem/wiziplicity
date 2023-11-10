package compass_system.wiziplicity.mixin;

import compass_system.wiziplicity.Main;
import compass_system.wiziplicity.command.ChatCommands;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class PlayerChatCanceller {
    @Inject(method = "sendChat(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(String message, CallbackInfo info) {
        if (ChatCommands.parseChatCommand(message) || Main.INSTANCE.proxyMessage(message)) {
            info.cancel();
        }
    }
}
