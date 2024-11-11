package io.github.startsmercury.visual_snowy_leaves.mixin.client.core.transition.sodium;

import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldSlice.class)
public class WorldSliceMixin implements SnowAware, VisualSnowyLeavesAware {
    @Shadow
    @Final
    private ClientLevel world;

    @Override
    public VisualSnowyLeavesImpl getVisualSnowyLeaves() {
        return ((VisualSnowyLeavesAware) this.world).getVisualSnowyLeaves();
    }

    @Override
    public boolean visual_snowy_leaves$coldEnoughToSnow(final BlockPos blockPos) {
        return ((SnowAware) this.world).visual_snowy_leaves$coldEnoughToSnow(blockPos);
    }
}
