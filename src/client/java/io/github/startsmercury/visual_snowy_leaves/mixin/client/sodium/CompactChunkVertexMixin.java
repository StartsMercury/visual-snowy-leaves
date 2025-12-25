package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.impl.CompactChunkVertex;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CompactChunkVertex.class)
public class CompactChunkVertexMixin {
    @WrapOperation(method = "lambda$getEncoder$0", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/util/ColorARGB;mulRGB(IF)I"))
    private static int encodeAoAsAlpha(final int color, final float factor, final Operation<Integer> original) {
        return ARGB.multiplyAlpha(color, factor);
    }
}
