package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.IdMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockColors.class)
public interface BlockColorsAccessor {
    @Accessor
    IdMapper<BlockColor> getBlockColors();
}
