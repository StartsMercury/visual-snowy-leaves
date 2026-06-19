package io.github.startsmercury.visual_snowy_leaves.mixin.client.renderer;

import io.github.startsmercury.visual_snowy_leaves.impl.client.arewe.AreWeLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void onRenderLevelStart(final CallbackInfo callback) {
        AreWeLevel.yes = true;
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRenderLevelEnd(final CallbackInfo callback) {
        AreWeLevel.yes = false;
    }
}
