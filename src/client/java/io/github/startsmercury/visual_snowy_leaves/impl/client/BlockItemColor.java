package io.github.startsmercury.visual_snowy_leaves.impl.client;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public record BlockItemColor(BlockColors blockColors) implements ItemColor {
    @Override
    public int getColor(final ItemStack itemStack, final int i) {
        final var blockState = ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();
        return blockColors.getColor(blockState, null, null, i);
    }
}
