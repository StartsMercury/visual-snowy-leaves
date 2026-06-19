package io.github.startsmercury.visual_snowy_leaves.mixin.client.vertex;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.startsmercury.visual_snowy_leaves.impl.client.arewe.AreWeLevel;
import io.github.startsmercury.visual_snowy_leaves.impl.client.vertices.VslVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderPipeline.class)
public abstract class RenderPipelineMixin {
    @ModifyReturnValue(method = "getVertexFormat", at = @At("RETURN"))
    private VertexFormat visual_snowy_leaves$tryReplace(VertexFormat original) {
        if (AreWeLevel.yes) {
            if (original == DefaultVertexFormat.BLOCK) {
                return VslVertexFormats.TERRAIN;
            } else if (original == DefaultVertexFormat.NEW_ENTITY) {
                return VslVertexFormats.ENTITY;
            }
        }

        return original;
    }
}
