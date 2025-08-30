package io.github.startsmercury.visual_snowy_leaves.mixin.client.transition.minecraft;

import io.github.startsmercury.visual_snowy_leaves.impl.client.SnowData;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowDataAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FallingBlockRenderState.class)
public class FallingBlockRenderStateMixin implements SnowAware, SnowDataAware, VisualSnowyLeavesAware {
    @Shadow
    public MovingBlockRenderState movingBlockRenderState;

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public VisualSnowyLeavesImpl getVisualSnowyLeaves() {
        final var level = this.movingBlockRenderState.level;
        assert !(level instanceof ServerLevel);
        assert level instanceof ClientLevel;
        return ((VisualSnowyLeavesAware) level).getVisualSnowyLeaves();
    }

    @Override
    public boolean visual_snowy_leaves$coldEnoughToSnow(final BlockPos blockPos) {
        final var level = this.movingBlockRenderState.level;
        return ((SnowAware) level).visual_snowy_leaves$coldEnoughToSnow(blockPos);
    }

    @Override
    public SnowData visual_snowy_leaves$getSnowData() {
        final var level = this.movingBlockRenderState.level;
        assert level instanceof ClientLevel;
        return ((SnowDataAware) level).visual_snowy_leaves$getSnowData();
    }
}
