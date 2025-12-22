package io.github.startsmercury.visual_snowy_leaves.mixin.client.vertex;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin extends VertexConsumerExtension {
//    @ModifyArg(method = "putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFFF[II)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(FFFIFFIIFFF)V"), index = 3)
//    private int a(
//            final int color,
//            final @Local(ordinal = 0, argsOnly = true) float[] brightness,
//            final @Local(ordinal = 0, argsOnly = true) float red,
//            final @Local(ordinal = 1, argsOnly = true) float green,
//            final @Local(ordinal = 2, argsOnly = true) float blue,
//            final @Local(ordinal = 3, argsOnly = true) float alpha,
//            final @Local(ordinal = 2) int vertex
//            ) {
//        if (this.visual_snowy_leaves$alphaAsAo()) {
//            return ARGB.colorFromFloat(alpha * brightness[vertex], red, green, blue);
//        } else {
//            return color;
//        }
//    }


    @WrapOperation(method = "putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFFF[II)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(FFFIFFIIFFF)V"))
    private void a(
            final VertexConsumer instance,
            final float x,
            final float y,
            final float z,
            int color,
            final float u,
            final float v,
            final int overlayCoords,
            final int lightCoords,
            final float nx,
            final float ny,
            final float nz,
            final Operation<Void> original,
            final @Local(ordinal = 0, argsOnly = true) float[] brightness,
            final @Local(ordinal = 0, argsOnly = true) float red,
            final @Local(ordinal = 1, argsOnly = true) float green,
            final @Local(ordinal = 2, argsOnly = true) float blue,
            final @Local(ordinal = 3, argsOnly = true) float alpha,
            final @Local(ordinal = 2) int vertex
    ) {
        if (this.visual_snowy_leaves$alphaAsAo()) {
             color = ARGB.colorFromFloat(alpha * brightness[vertex], red, green, blue);
        }
        original.call(instance, x, y, z, color, u, v, overlayCoords, lightCoords, nx, ny, nz);
    }
}
