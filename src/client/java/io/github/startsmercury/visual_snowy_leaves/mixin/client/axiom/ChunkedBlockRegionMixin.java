package io.github.startsmercury.visual_snowy_leaves.mixin.client.axiom;

import com.moulberry.axiom.render.regions.ChunkedBlockRegion;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkedBlockRegion.class)
public class ChunkedBlockRegionMixin implements SnowAware {
    @Override
    public boolean visual_snowy_leaves$coldEnoughToSnow(final @NotNull BlockPos blockPos) {
        return false;
    }
}
