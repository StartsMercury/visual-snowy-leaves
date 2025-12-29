package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.EntityVertexExtension;
import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.SodiumBlockFeatureContext;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.client.render.model.QuadEncoder;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(QuadEncoder.class)
public class QuadEncoderMixin {
    @Inject(
        method = "writeQuadVertices(" +
            "Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;" +
            "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;" +
            "I" +
            "Lorg/joml/Matrix4f;ZLorg/joml/Matrix3f;" +
        ")V",
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
        if (((VertexConsumerExtension) buffer).visual_snowy_leaves$mainVertexFormat()) {
            EntityVertexExtension.write(ptr, SodiumBlockFeatureContext.isSnowMarked() ? 1 : 0);
        }
    }

    @ModifyExpressionValue(
        method = "writeQuadVertices(" +
            "Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;" +
            "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;" +
            "I" +
            "Lorg/joml/Matrix4f;" +
            "Z" +
            "Lorg/joml/Matrix3f;" +
        ")V",
        at = @At(value = "CONSTANT", args = "longValue=36")
    )
    private static long increaseStride(
        final long original,
        final @Local(ordinal = 0, argsOnly = true) VertexBufferWriter buffer
    ) {
        if (((VertexConsumerExtension) buffer).visual_snowy_leaves$mainVertexFormat()) {
            return original + 4;
        } else {
            return original;
        }
    }
}
