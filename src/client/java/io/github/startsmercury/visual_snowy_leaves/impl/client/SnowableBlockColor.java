package io.github.startsmercury.visual_snowy_leaves.impl.client;

import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class SnowableBlockColor implements BlockColor {
    public static SnowableBlockColor setMultiplier(
        final BlockColor blockColor,
        final int multiplier
    ) {
        final SnowableBlockColor self;
        if (blockColor instanceof final SnowableBlockColor snowableBlockColor) {
            self = snowableBlockColor;
        } else {
            self = new SnowableBlockColor(blockColor);
        }
        self.setCorrectionMultiplier(multiplier);
        return self;
    }

    private final BlockColor blockColor;

    private int correctionMultiplier;

    public SnowableBlockColor(final BlockColor blockColor) {
        Objects.requireNonNull(blockColor);
        this.blockColor = blockColor;
    }

    @Override
    public int getColor(
        final BlockState blockState,
        final @Nullable BlockAndTintGetter blockAndTintGetter,
        final @Nullable BlockPos blockPos,
        final int i
    ) {
        final var correctionMultiplier = this.correctionMultiplier;
        final var base = this.blockColor.getColor(blockState, blockAndTintGetter, blockPos, i);

        if (i != 0) {
            return base;
        }
        if (blockPos == null) {
            return FastColor.ARGB32.multiply(base, correctionMultiplier);
        }
        if (
            !(blockAndTintGetter instanceof final SnowAware snowAware)
                || !(snowAware.visual_snowy_leaves$coldEnoughToSnow(blockPos))
                || !(blockAndTintGetter instanceof final VisualSnowyLeavesAware aware)
        ) {
            return FastColor.ARGB32.multiply(base, correctionMultiplier);
        }

        final var visualSnowyLeaves = aware.getVisualSnowyLeaves();
        final var snowiness = visualSnowyLeaves.getSnowiness();
        final var maxSnowiness = visualSnowyLeaves.getMaxSnowiness();

        final var r = FastColor.ARGB32.red(correctionMultiplier) * FastColor.ARGB32.red(base);
        final var g = FastColor.ARGB32.green(correctionMultiplier) * FastColor.ARGB32.green(base);
        final var b = FastColor.ARGB32.blue(correctionMultiplier) * FastColor.ARGB32.blue(base);

        return FastColor.ARGB32.color(
            FastColor.ARGB32.alpha(base),
            (maxSnowiness * r + snowiness * (255 * 255 - r)) / (255 * maxSnowiness),
            (maxSnowiness * g + snowiness * (255 * 255 - g)) / (255 * maxSnowiness),
            (maxSnowiness * b + snowiness * (255 * 255 - b)) / (255 * maxSnowiness)
        );
    }

    public int getCorrectionMultiplier() {
        return correctionMultiplier;
    }

    public void setCorrectionMultiplier(final int correctionMultiplier) {
        this.correctionMultiplier = correctionMultiplier;
    }
}
