package io.github.startsmercury.visual_snowy_leaves.mixin.client.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {
    @ModifyExpressionValue(method = "putQuadData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BakedQuad;isTinted()Z"))
    private boolean overrideBlockTint(
        final boolean original,
        @Local(ordinal = 0, argsOnly = true) BlockAndTintGetter blockAndTintGetter,
        @Local(ordinal = 0, argsOnly = true) BlockState blockState,
        @Local(ordinal = 0, argsOnly = true) BlockPos blockPos
    ) {
        final LevelAccessor levelAccessor;

        if (!original) {
            return false;
        } else if (blockAndTintGetter instanceof final LevelAccessor levelAccessor2) {
            levelAccessor = levelAccessor2;
        } else if (blockAndTintGetter instanceof final RenderChunkRegionAccessor renderChunkRegionAccessor) {
            levelAccessor = renderChunkRegionAccessor.getLevel();
        } else {
            return true;
        }

        return !VisualSnowyLeavesImpl.isSnowyAt(levelAccessor, blockState, blockPos);
    }
}
