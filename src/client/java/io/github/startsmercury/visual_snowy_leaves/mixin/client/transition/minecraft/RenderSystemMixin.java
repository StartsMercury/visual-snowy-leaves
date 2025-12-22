package io.github.startsmercury.visual_snowy_leaves.mixin.client.transition.minecraft;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @Inject(method = "bindDefaultUniforms(Lcom/mojang/blaze3d/systems/RenderPass;)V", at = @At("RETURN"))
    private static void a(
            final CallbackInfo callback,
            final @Local(ordinal = 0, argsOnly = true) RenderPass renderPass
    ) {

        final var globalUniform = VisualSnowyLeavesImpl.getSnowProgress();
        if (globalUniform != null) {
            renderPass.setUniform("SnowProgress", globalUniform);
        }

    }
}
