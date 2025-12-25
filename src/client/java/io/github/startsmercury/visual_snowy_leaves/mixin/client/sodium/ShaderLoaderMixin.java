package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShaderLoader.class)
public class ShaderLoaderMixin {
    @ModifyReturnValue(method = "getShaderSource", at = @At("RETURN"))
    private static String modifyShader(
        final String original,
        final @Local(ordinal = 0, argsOnly = true) Identifier name
    ) {
        if (name.getNamespace().equals("sodium")
            && name.getPath().equals("blocks/block_layer_opaque.vsh")
        ) {
            return original
                .replace(
                    "uniform float u_FadePeriodInv;",
                    "uniform float u_FadePeriodInv; uniform float u_VslSnowProgress;"
                )
                .replace(
                    "_vert_color *",
                    "vec4(" +
                        "_vert_color.a * (" +
                            // See VslMaterialParameters.java
                            "((_material_params >> 6u) & 1u) == 0u" +
                                // Unaffected
                                "? _vert_color.rgb" +
                                // Affected by snowy progress
                                ": u_VslSnowProgress + (1 - u_VslSnowProgress) * _vert_color.rgb" +
                        ")," +
                        "1.0" +
                    ") *"
                );
        } else {
            return original;
        }
    }
}
