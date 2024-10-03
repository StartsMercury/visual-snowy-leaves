package io.github.startsmercury.visual_snowy_leaves.impl.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;

public final class VisualSnowyLeavesImpl {
    public static final String MODID = "visual-snowy-leaves";

    public static final TagKey<Block> SNOWY = TagKey.create(Registries.BLOCK, new ResourceLocation(MODID, "snowy"));

    public static boolean isSnowyAt(final LevelAccessor levelAccessor, final BlockPos blockPos) {
        return levelAccessor.getLevelData().isRaining()
            && levelAccessor.getBiome(blockPos).value().coldEnoughToSnow(blockPos)
            && levelAccessor.getBlockState(blockPos).is(BlockTags.LEAVES);
    }

    private VisualSnowyLeavesImpl() {}
}
