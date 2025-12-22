package io.github.startsmercury.visual_snowy_leaves.mixin.client.transition.minecraft;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.SnowProgressUniform;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.GameRendererExtension;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowDataAware;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin implements GameRendererExtension {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private final SnowProgressUniform uniform = new SnowProgressUniform();

    @Override
    public SnowProgressUniform getSnowProgressUniform() {
        return this.uniform;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlobalSettingsUniform;update(IIDJLnet/minecraft/client/DeltaTracker;ILnet/minecraft/client/Camera;Z)V"))
    private void syncWithGlobalUniformsUpdate(
        final CallbackInfo callback,
        final @Local(ordinal = 0, argsOnly = true) DeltaTracker deltaTracker
    ) {
        final var level = this.minecraft.level;
        if (level != null) {
            final float progress =
                ((SnowDataAware) level).visual_snowy_leaves$getSnowData().getAccumulatedTicks();
            final var total =
                this.minecraft.getVisualSnowyLeaves().getConfig().transitionDuration().asTicks();
            final var delta = deltaTracker.getGameTimeDeltaPartialTick(true);
//            final var delta = 0.0f;

            final var interpolated = level.isRaining() ? progress + delta : progress - delta;

            this.getSnowProgressUniform().update(Mth.clamp(interpolated / total, 0.0f, 1.0f));
        }
    }

    @Inject(method = "close()V", at = @At("RETURN"))
    private void onClose(final CallbackInfo callback) {
        this.uniform.close();
    }
}
