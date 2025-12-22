package io.github.startsmercury.visual_snowy_leaves.mixin.client.transition.minecraft;

import io.github.startsmercury.visual_snowy_leaves.impl.client.SnowData;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowDataAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MovingBlockRenderState.class)
public class MovingBlockRenderStateMixin implements SnowAware, SnowDataAware, VisualSnowyLeavesAware {
    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public VisualSnowyLeavesImpl getVisualSnowyLeaves() {
        assert ((MovingBlockRenderState) (Object) this).level instanceof ClientLevel;
        return ((VisualSnowyLeavesAware) ((MovingBlockRenderState) (Object) this).level).getVisualSnowyLeaves();
    }

    @Override
    public boolean visual_snowy_leaves$coldEnoughToSnow(final BlockPos blockPos) {
        return ((SnowAware) ((MovingBlockRenderState) (Object) this).level).visual_snowy_leaves$coldEnoughToSnow(blockPos);
    }

    @Override
    public SnowData visual_snowy_leaves$getSnowData() {
        assert ((MovingBlockRenderState) (Object) this).level instanceof ClientLevel;
        return ((SnowDataAware) ((MovingBlockRenderState) (Object) this).level).visual_snowy_leaves$getSnowData();
    }
}
