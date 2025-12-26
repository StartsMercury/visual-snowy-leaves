package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.SodiumBlockFeatureContext;
import net.caffeinemc.mods.sodium.api.util.ColorMixer;
import net.caffeinemc.mods.sodium.client.render.frapi.render.NonTerrainBlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NonTerrainBlockRenderContext.class)
public abstract class NonTerrainBlockRenderContextMixin extends AbstractBlockRenderContext {
    @Inject(method = "renderModel", at = @At("HEAD"))
    private void snowMark(
        final CallbackInfo callback,
        final @Local(ordinal = 0, argsOnly = true) BlockAndTintGetter blockView,
        final @Local(ordinal = 0, argsOnly = true) BlockState blockState,
        final @Local(ordinal = 0, argsOnly = true) BlockPos blockPos
    ) {
        final var vslConfig = Minecraft.getInstance().getVisualSnowyLeaves().getConfig();

        final var correctBiome = !vslConfig.requireSnowyBiomes()
             || blockView instanceof final SnowAware snowAware
             && snowAware.visual_snowy_leaves$coldEnoughToSnow(blockPos);
        SodiumBlockFeatureContext.setSnowMarked(correctBiome && vslConfig
            .targetBlockKeys()
            .contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))
        );
    }

    @WrapOperation(
        method = "shadeQuad",
        at = @At(
            value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/api/util/ColorARGB;mulRGB(IF)I"
        )
    )
    private int alphaAsBrightness(final int color, final float factor, final Operation<Integer> original) {
        if (this.defaultRenderType.pipeline().getVertexFormat() == DefaultVertexFormat.BLOCK) {
            return ColorMixer.mul(color, factor) & 0xFF000000 | color & 0x00FFFFFF;
        } else {
            return original.call(color, factor);
        }
    }
}
