package io.github.startsmercury.visual_snowy_leaves.mixin.client.core.transition.minecraft;

import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelReader.class)
public interface LevelReaderMixin extends SnowAware {
    @Override
    default boolean visual_snowy_leaves$coldEnoughToSnow(final BlockPos blockPos) {
        return ((LevelReader) this).getBiome(blockPos).value().coldEnoughToSnow(blockPos);
    }
}
