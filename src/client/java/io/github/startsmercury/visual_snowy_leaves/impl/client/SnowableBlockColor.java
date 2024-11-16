package io.github.startsmercury.visual_snowy_leaves.impl.client;

import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowDataAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record SnowableBlockColor(BlockColor blockColor, int correctionMultiplier) implements BlockColor {
    public static final int COLOR_WHITE = 0xFFFFFFFF;

    private static boolean isSnowyAt(
        final @Nullable BlockAndTintGetter blockAndTintGetter,
        final @Nullable BlockPos blockPos
    ) {
        return blockPos != null
            && blockAndTintGetter instanceof final SnowAware snowAware
            && snowAware.visual_snowy_leaves$coldEnoughToSnow(blockPos);
    }

    public static SnowableBlockColor setMultiplier(BlockColor blockColor, final int multiplier) {
        while (blockColor instanceof final SnowableBlockColor snowableBlockColor) {
            blockColor = snowableBlockColor.blockColor;
        }

        return new SnowableBlockColor(blockColor, multiplier);
    }

    public SnowableBlockColor {
        Objects.requireNonNull(blockColor, "Parameter blockColor is null");
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
        if (!(blockAndTintGetter instanceof final VisualSnowyLeavesAware vslAware)) {
            return FastColor.ARGB32.multiply(base, correctionMultiplier);
        }

        final var config = vslAware.getVisualSnowyLeaves().getConfig();

        switch (config.snowyMode()) {
            case NEVER:
                return FastColor.ARGB32.multiply(base, correctionMultiplier);
            case SNOWING:
                break;
            case ALWAYS:
                return COLOR_WHITE;
        }

        if (!isSnowyAt(blockAndTintGetter, blockPos)) {
            return FastColor.ARGB32.multiply(base, correctionMultiplier);
        }

        if (!(blockAndTintGetter instanceof final SnowDataAware snowDataAware)) {
            return COLOR_WHITE;
        }

        final var curr = snowDataAware.visual_snowy_leaves$getSnowData().getAccumulatedTicks();
        final var max = config.transitionDuration().asTicks();

        final var r = FastColor.ARGB32.red(correctionMultiplier) * FastColor.ARGB32.red(base);
        final var g = FastColor.ARGB32.green(correctionMultiplier) * FastColor.ARGB32.green(base);
        final var b = FastColor.ARGB32.blue(correctionMultiplier) * FastColor.ARGB32.blue(base);

        return FastColor.ARGB32.color(
            FastColor.ARGB32.alpha(base),
            Integer.divideUnsigned(max * r + curr * (255 * 255 - r), 255 * max),
            Integer.divideUnsigned(max * g + curr * (255 * 255 - g), 255 * max),
            Integer.divideUnsigned(max * b + curr * (255 * 255 - b), 255 * max)
        );
    }
}
