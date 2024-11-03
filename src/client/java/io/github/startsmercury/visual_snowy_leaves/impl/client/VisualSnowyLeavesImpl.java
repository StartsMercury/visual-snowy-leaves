package io.github.startsmercury.visual_snowy_leaves.impl.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VisualSnowyLeavesImpl {
    public static final Set<? extends ResourceLocation> TARGET_BLOCKS = Stream.of(
        "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove"
    )
        .map(base -> base + "_leaves")
        .map(ResourceLocation::new)
        .collect(Collectors.toSet());

    public static boolean isSnowyAt(final LevelAccessor levelAccessor, final BlockState blockState, final BlockPos blockPos) {
        return levelAccessor.getLevelData().isRaining()
            && levelAccessor.getBiome(blockPos).value().coldEnoughToSnow(blockPos)
            && TARGET_BLOCKS.contains(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()));
    }

    private VisualSnowyLeavesImpl() {}
}
