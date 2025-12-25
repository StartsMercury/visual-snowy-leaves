package io.github.startsmercury.visual_snowy_leaves.mixin.client.vertex;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VertexConsumer.class)
public interface VertexConsumerMixin extends VertexConsumerExtension {
    @ModifyArg(
        method = "putBulkData(" +
            "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;" +
            "Lnet/minecraft/client/renderer/block/model/BakedQuad;" +
            "[F" +
            "F" +
            "F" +
            "F" +
            "F" +
            "[I" +
            "I" +
        ")V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(FFFIFFIIFFF)V"
        ),
        index = 3
    )
    private int storeBrightnessInAlpha(
        final int color,
        final @Local(ordinal = 0, argsOnly = true) float[] brightness,
        final @Local(ordinal = 0, argsOnly = true) float red,
        final @Local(ordinal = 1, argsOnly = true) float green,
        final @Local(ordinal = 2, argsOnly = true) float blue,
        final @Local(ordinal = 3, argsOnly = true) float alpha,
        final @Local(ordinal = 2) int vertex
    ) {
        if (this.visual_snowy_leaves$alphaAsBrightness()) {
             return ARGB.colorFromFloat(alpha * brightness[vertex], red, green, blue);
        } else {
            return color;
        }
    }
}
