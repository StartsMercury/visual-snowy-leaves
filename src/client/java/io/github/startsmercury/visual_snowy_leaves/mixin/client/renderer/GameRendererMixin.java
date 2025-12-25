package io.github.startsmercury.visual_snowy_leaves.mixin.client.renderer;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslGlobalsUniform;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowProgressAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.SodiumSnowProgress;
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
public class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private final VslGlobalsUniform globalsUniform = new VslGlobalsUniform();

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GlobalSettingsUniform;update(" +
                "I" +
                "I" +
                "D" +
                "J" +
                "Lnet/minecraft/client/DeltaTracker;" +
                "I" +
                "Lnet/minecraft/client/Camera;" +
                "Z" +
            ")V"
        )
    )
    private void syncWithGlobalUniformsUpdate(
        final CallbackInfo callback,
        final @Local(ordinal = 0, argsOnly = true) DeltaTracker deltaTracker
    ) {
        final var level = this.minecraft.level;
        if (level == null) return;

        final var vslConfig = this.minecraft.getVisualSnowyLeaves().getConfig();

        final float progress;

        if (vslConfig.requireSnowyWeather()) {
            final float ticks =
                ((SnowProgressAware) level).visual_snowy_leaves$getSnowProgress().getAccumulatedTicks();
            final var delta = deltaTracker.getGameTimeDeltaPartialTick(false);
            final var total = vslConfig.transitionDuration().asTicks();

            final var interpolated = level.isRaining() ? ticks + delta : ticks - delta;
            // More concise way to handle overshooting from `delta`
            progress = Mth.clamp(interpolated / total, 0.0f, 1.0f);
        } else {
            progress = 1.0f;
        }

        this.globalsUniform.update(progress);
        SodiumSnowProgress.set(progress);
    }

    @Inject(method = "close()V", at = @At("RETURN"))
    private void onClose(final CallbackInfo callback) {
        this.globalsUniform.close();
    }
}
