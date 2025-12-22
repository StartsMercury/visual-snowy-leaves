package io.github.startsmercury.visual_snowy_leaves.impl.client.extension;

import net.minecraft.core.BlockPos;

public interface SnowAware {
    interface PointSource extends SnowAware {
        boolean visual_snowy_leaves$isColdEnoughToSnow();

        void visual_snowy_leaves$setColdEnoughToSnow(boolean coldEnoughToSnow);

        @Override
        default boolean visual_snowy_leaves$coldEnoughToSnow(final BlockPos blockPos) {
            return this.visual_snowy_leaves$isColdEnoughToSnow();
        }
    }

    boolean visual_snowy_leaves$coldEnoughToSnow(final BlockPos blockPos);
}
