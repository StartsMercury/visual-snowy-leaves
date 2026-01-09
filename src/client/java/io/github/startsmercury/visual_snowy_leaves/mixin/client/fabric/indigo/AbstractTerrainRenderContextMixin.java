package io.github.startsmercury.visual_snowy_leaves.mixin.client.fabric.indigo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.fabric.indigo.AbstractRenderContextExtension;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractTerrainRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractTerrainRenderContext.class)
public class AbstractTerrainRenderContextMixin implements AbstractRenderContextExtension {
    @Shadow
    @Final
    protected BlockRenderInfo blockInfo;

    @Unique
    private boolean alphaAsBrightness;

    @Override
    public @Nullable BlockRenderInfo visual_snowy_leaves$blockInfo() {
        return this.blockInfo;
    }

    @ModifyExpressionValue(method = "bufferQuad", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractTerrainRenderContext;getVertexConsumer(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayer;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer captureAlphaAsBrightness(final VertexConsumer original) {
        this.alphaAsBrightness = original.visual_snowy_leaves$alphaAsBrightness();
        return original;
    }

    @WrapOperation(
        method = { "shadeQuad", "flatLight" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/ARGB;scaleRGB(IF)I"
        )
    )
    private int storeBrightnessInAlpha(final int i, final float f, final Operation<Integer> original) {
        if (this.alphaAsBrightness) {
            return ARGB.multiplyAlpha(i, f);
        } else {
            return original.call(i, f);
        }
    }
}
