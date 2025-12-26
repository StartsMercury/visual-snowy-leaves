package io.github.startsmercury.visual_snowy_leaves.mixin.client.shader;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShaderManager.class)
public class ShaderManagerMixin {
    @ModifyExpressionValue(
        method = "loadShader(" +
            "Lnet/minecraft/resources/Identifier;" +
            "Lnet/minecraft/server/packs/resources/Resource;" +
            "Lcom/mojang/blaze3d/shaders/ShaderType;" +
            "Ljava/util/Map;" +
            "Lcom/google/common/collect/ImmutableMap$Builder;" +
        ")V",
        at = @At(
            value = "INVOKE",
            target = "Lorg/apache/commons/io/IOUtils;toString(Ljava/io/Reader;)Ljava/lang/String;"
        )
    )
    private static String modifyBlockShaders(
        final String source,
        final @Local(ordinal = 0, argsOnly = true) Identifier location,
        final @Local(ordinal = 0, argsOnly = true) ShaderType type
    ) {
        if (type == ShaderType.VERTEX
            && location.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)
            && switch (location.getPath()) {
                case "shaders/core/block.vsh",
                     "shaders/core/rendertype_translucent_moving_block.vsh",
                     "shaders/core/terrain.vsh" -> true;
                default -> false;
            }
        ) {
            return source
                .replace("in vec3 Normal;", "in vec3 Normal; in int VslIndex;")
                .replace(
                    "uniform sampler2D Sampler2;",
                    "uniform sampler2D Sampler2; layout(std140) uniform SnowProgress {" +
                        "float Progress;" +
                    "};"
                )
                .replace(
                    "Color *",
                    "vec4(" +
//                        "Color.a * (VslIndex != 1 ? Color.rgb : Progress + (1 - Progress) * Color.rgb)," +
                        // FIXME temporary fix since I can't figure out the correct value to encode `1`
                        //       in QuadEncoderMixin
                        "Color.a * (VslIndex == 0 ? Color.rgb : Progress + (1 - Progress) * Color.rgb)," +
                        " 1.0" +
                    ") *"
                );
        } else {
            return source;
        }
    }
}
