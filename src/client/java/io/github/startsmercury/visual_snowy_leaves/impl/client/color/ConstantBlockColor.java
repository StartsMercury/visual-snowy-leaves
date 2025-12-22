package io.github.startsmercury.visual_snowy_leaves.impl.client.color;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public record ConstantBlockColor(int argb) implements BlockColor {
    public static final ConstantBlockColor WHITE = new ConstantBlockColor(CommonColors.WHITE);

    @Override
    public int getColor(
        final BlockState blockState,
        final @Nullable BlockAndTintGetter blockAndTintGetter,
        final @Nullable BlockPos blockPos,
        final int i
    ) {
        return this.argb;
    }
}
