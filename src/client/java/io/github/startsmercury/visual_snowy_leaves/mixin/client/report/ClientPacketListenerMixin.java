package io.github.startsmercury.visual_snowy_leaves.mixin.client.report;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(
        method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V",
        at = @At("TAIL")
    )
    private void showReportMessage(final CallbackInfo callback) {
        final var ignored = Minecraft.getInstance().getVisualSnowyLeaves().sendReportNotice();
    }
}
