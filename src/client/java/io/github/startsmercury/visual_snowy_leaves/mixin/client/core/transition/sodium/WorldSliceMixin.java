package io.github.startsmercury.visual_snowy_leaves.mixin.client.core.transition.sodium;

import io.github.startsmercury.visual_snowy_leaves.impl.client.SnowData;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowDataAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldSlice.class)
public class WorldSliceMixin implements SnowAware, SnowDataAware, VisualSnowyLeavesAware {
    @Final
    @Shadow
    private ClientLevel world;

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public VisualSnowyLeavesImpl getVisualSnowyLeaves() {
        return ((VisualSnowyLeavesAware) this.world).getVisualSnowyLeaves();
    }

    @Override
    public boolean visual_snowy_leaves$coldEnoughToSnow(final BlockPos blockPos) {
        return ((SnowAware) this.world).visual_snowy_leaves$coldEnoughToSnow(blockPos);
    }

    @Override
    public SnowData visual_snowy_leaves$getSnowData() {
        return ((SnowDataAware) this.world).visual_snowy_leaves$getSnowData();
    }
}
