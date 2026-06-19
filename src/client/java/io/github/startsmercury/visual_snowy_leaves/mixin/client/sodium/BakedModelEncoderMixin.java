package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.EntityVertexExtension;
import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.SodiumBlockFeatureContext;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.render.immediate.model.BakedModelEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO: do we still need this?
@Mixin(BakedModelEncoder.class)
public class BakedModelEncoderMixin {
    @Inject(
        method = {
            "writeQuadVertices(" +
                "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;" +
                "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;" +
                "Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadView;" +
                "F" +
                "F" +
                "F" +
                "F" +
                "[F" +
                "[I" +
                "I" +
            ")V",
            "writeQuadVertices(" +
                "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;" +
                "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;" +
                "Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadView;" +
                "I" +
                "I" +
                "I" +
                "Z" +
            ")V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/api/vertex/format/common/EntityVertex;write(" +
                "J" +
                "F" +
                "F" +
                "F" +
                "I" +
                "F" +
                "F" +
                "I" +
                "I" +
                "I" +
            ")V")
    )
    private static void encodeVertexExtensions(
        final CallbackInfo callback,
        final @Local(ordinal = 0, argsOnly = true) VertexBufferWriter buffer,
        final @Local(name = "ptr") long ptr
    ) {
        if (((VertexConsumerExtension) buffer).visual_snowy_leaves$isExtended()) {
            EntityVertexExtension.write(ptr, SodiumBlockFeatureContext.isSnowMarked() ? 1 : 0);
        }
    }
}
