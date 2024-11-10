package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockRenderer.class)
public class BlockRendererMixin {
    @ModifyExpressionValue(
        method = "getVertexColors",
        at = @At(
            value = "INVOKE",
            target = "Lme/jellysquid/mods/sodium/client/model/quad/BakedQuadView;hasColor()Z"
        ),
        remap = false
    )
    private boolean modifyColor(
        final boolean original,
        final @Local(ordinal = 0, argsOnly = true) BlockRenderContext ctx,
        final @Local(ordinal = 0, argsOnly = true) ColorProvider<BlockState> provider
    ) {
        final var blockPos = ctx.pos();
        final var blockState = ctx.state();
        final var world = ((WorldSliceAccessor) (Object) ctx.world()).getWorld();
        return !VisualSnowyLeavesImpl.isSnowyAt(world, blockState, blockPos);
    }
}
