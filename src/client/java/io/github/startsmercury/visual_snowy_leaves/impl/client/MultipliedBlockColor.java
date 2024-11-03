package io.github.startsmercury.visual_snowy_leaves.impl.client;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MultipliedBlockColor implements BlockColor {
    public static MultipliedBlockColor setMultiplier(
        final BlockColor blockColor,
        final int multiplier
    ) {
        final MultipliedBlockColor self;
        if (blockColor instanceof final MultipliedBlockColor multipliedBlockColor) {
            self = multipliedBlockColor;
        } else {
            self = new MultipliedBlockColor(blockColor);
        }
        self.setMultiplier(multiplier);
        return self;
    }

    private final BlockColor blockColor;

    private int multiplier;

    public MultipliedBlockColor(final BlockColor blockColor) {
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
        final var base = this.blockColor.getColor(blockState, blockAndTintGetter, blockPos, i);
        return i != 0 ? base : FastColor.ARGB32.multiply(base, this.getMultiplier());
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(final int multiplier) {
        this.multiplier = multiplier;
    }
}
