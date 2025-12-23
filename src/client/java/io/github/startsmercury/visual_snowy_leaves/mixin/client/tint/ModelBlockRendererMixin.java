package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
    @WrapOperation(
        method = "putQuadData(" +
            "Lnet/minecraft/world/level/BlockAndTintGetter;" +
            "Lnet/minecraft/world/level/block/state/BlockState;" +
            "Lnet/minecraft/core/BlockPos;" +
            "Lcom/mojang/blaze3d/vertex/VertexConsumer;" +
            "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;" +
            "Lnet/minecraft/client/renderer/block/model/BakedQuad;" +
            "Lnet/minecraft/client/renderer/block/ModelBlockRenderer$CommonRenderStorage;" +
            "I" +
        ")V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(" +
                "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;" +
                "Lnet/minecraft/client/renderer/block/model/BakedQuad;" +
                "[F" +
                "F" +
                "F" +
                "F" +
                "F" +
                "[I" +
                "I" +
            ")V"
        )
    )
    private void distinguishSnowyableQuads(
        final VertexConsumer instance,
        final PoseStack.Pose pose,
        final BakedQuad quad,
        final float[] brightness,
        final float r,
        final float g,
        final float b,
        final float a,
        final int[] lightmapCoord,
        final int overlayCoords,
        final Operation<Void> original,
        final @Local(ordinal = 0, argsOnly = true) BlockAndTintGetter level,
        final @Local(ordinal = 0, argsOnly = true) BlockState blockState,
        final @Local(ordinal = 0, argsOnly = true) BlockPos blockPos
    ) {
        final var vslConfig = Minecraft.getInstance().getVisualSnowyLeaves().getConfig();
        if (level instanceof final SnowAware snowAware
            && snowAware.visual_snowy_leaves$coldEnoughToSnow(blockPos)
            && (!vslConfig.requireSnowyBiomes() || vslConfig
                .targetBlockKeys()
                .contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()))
            )
        ) {
            instance.visual_snowy_leaves$beginIndex(VertexConsumerExtension.SNOW_MARKED);
        }
        original.call(instance, pose, quad, brightness, r, g, b, a, lightmapCoord, overlayCoords);
        instance.visual_snowy_leaves$endIndex();
    }
}
