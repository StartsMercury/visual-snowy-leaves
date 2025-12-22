package io.github.startsmercury.visual_snowy_leaves.impl.client.color;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.util.Objects;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public record MultipliedBlockColor(BlockColor blockColor, Int2IntMap correctionMultipliers) implements BlockColor {
    public static MultipliedBlockColor setMultiplier(BlockColor blockColor, final Int2IntMap multipliers) {
        while (blockColor instanceof final MultipliedBlockColor multipliedBlockColor) {
            blockColor = multipliedBlockColor.blockColor;
        }

        return new MultipliedBlockColor(blockColor, multipliers);
    }

    public MultipliedBlockColor {
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
        return ARGB.multiply(base, correctionMultiplier);
    }
}
