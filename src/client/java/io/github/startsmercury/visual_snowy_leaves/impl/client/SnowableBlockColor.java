package io.github.startsmercury.visual_snowy_leaves.impl.client;

import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowDataAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record SnowableBlockColor(BlockColor blockColor, Int2IntMap correctionMultipliers) implements BlockColor {
    public static final int COLOR_WHITE = 0xFFFFFFFF;

    private static boolean isSnowyAt(
        final @Nullable BlockAndTintGetter blockAndTintGetter,
        final @Nullable BlockPos blockPos
    ) {
        return blockPos != null
            && blockAndTintGetter instanceof final SnowAware snowAware
            && snowAware.visual_snowy_leaves$coldEnoughToSnow(blockPos);
    }

    public static SnowableBlockColor setMultiplier(BlockColor blockColor, final Int2IntMap multipliers) {
        while (blockColor instanceof final SnowableBlockColor snowableBlockColor) {
            blockColor = snowableBlockColor.blockColor;
        }

        return new SnowableBlockColor(blockColor, multipliers);
    }

    public SnowableBlockColor {
        Objects.requireNonNull(blockColor, "Parameter blockColor is null");
        Objects.requireNonNull(correctionMultipliers, "Parameter correctionMultipliers is null");
    }

    @Override
    public int getColor(
        final BlockState blockState,
        final @Nullable BlockAndTintGetter blockAndTintGetter,
        final @Nullable BlockPos blockPos,
        final int i
    ) {
        final var base = this.blockColor.getColor(blockState, blockAndTintGetter, blockPos, i);
        final var correctionMultiplier = this.correctionMultipliers.get(i);

        if (correctionMultiplier == COLOR_WHITE) {
            return base;
        }

        if (!(blockAndTintGetter instanceof final VisualSnowyLeavesAware vslAware)) {
            return ARGB.multiply(base, correctionMultiplier);
        }

        final var config = vslAware.getVisualSnowyLeaves().getConfig();

        switch (config.snowyMode()) {
            case NEVER:
                return ARGB.multiply(base, correctionMultiplier);
            case SNOWING:
                break;
            case ALWAYS:
                return COLOR_WHITE;
        }

        if (!isSnowyAt(blockAndTintGetter, blockPos)) {
            return ARGB.multiply(base, correctionMultiplier);
        }

        if (!(blockAndTintGetter instanceof final SnowDataAware snowDataAware)) {
            return COLOR_WHITE;
        }

        final var curr = snowDataAware.visual_snowy_leaves$getSnowData().getAccumulatedTicks();
        final var max = config.transitionDuration().asTicks();

        final var r = ARGB.red(correctionMultiplier) * ARGB.red(base);
        final var g = ARGB.green(correctionMultiplier) * ARGB.green(base);
        final var b = ARGB.blue(correctionMultiplier) * ARGB.blue(base);

        return ARGB.color(
            ARGB.alpha(base),
            Integer.divideUnsigned(max * r + curr * (255 * 255 - r), 255 * max),
            Integer.divideUnsigned(max * g + curr * (255 * 255 - g), 255 * max),
            Integer.divideUnsigned(max * b + curr * (255 * 255 - b), 255 * max)
        );
    }
}
