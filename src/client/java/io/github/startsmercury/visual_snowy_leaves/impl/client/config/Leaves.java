package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import net.minecraft.resources.ResourceLocation;

final class Leaves {
    private static ResourceLocation getLeaves(final String baseName) {
        return ResourceLocation.parse(baseName + "_leaves");
    }

    // Version 0
    public static final ResourceLocation OAK = getLeaves("oak");
    public static final ResourceLocation SPRUCE = getLeaves("spruce");
    public static final ResourceLocation BIRCH = getLeaves("birch");
    public static final ResourceLocation ACACIA = getLeaves("acacia");
    public static final ResourceLocation JUNGLE = getLeaves("jungle");
    public static final ResourceLocation DARK_OAK = getLeaves("dark_oak");
    public static final ResourceLocation MANGROVE = getLeaves("mangrove");

    // Version 1
    public static final ResourceLocation PALE_OAK = getLeaves("pale_oak");

    private Leaves() {}
}
