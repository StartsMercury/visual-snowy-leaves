package io.github.startsmercury.visual_snowy_leaves.mixin.client.vertex;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslVertexFormatElement;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DefaultVertexFormat.class)
public abstract class DefaultVertexFormatMixin {
    @ModifyExpressionValue(
        method = "<clinit>()V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexFormat$Builder;padding(I)Lcom/mojang/blaze3d/vertex/VertexFormat$Builder;",
            ordinal = 0
        )
    )
    private static VertexFormat.Builder addToBlockVertexFormat(VertexFormat.Builder builder) {
        // FIXME temporary fix
        // TODO proper Sodium support for moving blocks
        if (!FabricLoader.getInstance().isModLoaded("sodium")) {
        for (final var element : VslVertexFormatElement.values()) {
            builder = builder.add(element.name(), element.value());
        }
        }
        return builder;
    }
}
