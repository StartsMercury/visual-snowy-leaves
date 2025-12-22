package io.github.startsmercury.visual_snowy_leaves.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderPipelines.class)
public class RenderPipelinesMixin {
    // FIXME, make the relative target a little more sound...
    @ModifyExpressionValue(method = "<clinit>()V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline$Builder;withUniform(Ljava/lang/String;Lcom/mojang/blaze3d/shaders/UniformType;)Lcom/mojang/blaze3d/pipeline/RenderPipeline$Builder;"))
    private static RenderPipeline.Builder a(RenderPipeline.Builder builder) {
        return builder.withUniform("SnowProgress", UniformType.UNIFORM_BUFFER);
    }
}
