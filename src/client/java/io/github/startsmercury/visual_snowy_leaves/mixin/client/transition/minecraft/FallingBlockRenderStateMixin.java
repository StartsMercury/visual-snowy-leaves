package io.github.startsmercury.visual_snowy_leaves.mixin.client.transition.minecraft;

import io.github.startsmercury.visual_snowy_leaves.impl.client.SnowData;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowDataAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FallingBlockRenderState.class)
public class FallingBlockRenderStateMixin implements SnowAware, SnowDataAware, VisualSnowyLeavesAware {
    @Shadow
    public BlockAndTintGetter level;

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public VisualSnowyLeavesImpl getVisualSnowyLeaves() {
        assert !(this.level instanceof ServerLevel);
        assert this.level instanceof ClientLevel;
        return ((VisualSnowyLeavesAware) this.level).getVisualSnowyLeaves();
    }

    @Override
    public boolean visual_snowy_leaves$coldEnoughToSnow(final BlockPos blockPos) {
        return ((SnowAware) this.level).visual_snowy_leaves$coldEnoughToSnow(blockPos);
    }

    @Override
    public SnowData visual_snowy_leaves$getSnowData() {
        assert this.level instanceof ClientLevel;
        return ((SnowDataAware) this.level).visual_snowy_leaves$getSnowData();
    }
}
