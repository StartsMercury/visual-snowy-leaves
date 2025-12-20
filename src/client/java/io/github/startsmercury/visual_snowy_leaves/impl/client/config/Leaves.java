package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import net.minecraft.resources.Identifier;

final class Leaves {
    private static Identifier getLeaves(final String baseName) {
        return Identifier.parse(baseName + "_leaves");
    }

    // Version 0
    public static final Identifier OAK = getLeaves("oak");
    public static final Identifier SPRUCE = getLeaves("spruce");
    public static final Identifier BIRCH = getLeaves("birch");
    public static final Identifier ACACIA = getLeaves("acacia");
    public static final Identifier JUNGLE = getLeaves("jungle");
    public static final Identifier DARK_OAK = getLeaves("dark_oak");
    public static final Identifier MANGROVE = getLeaves("mangrove");

    // Version 1
    public static final Identifier PALE_OAK = getLeaves("pale_oak");

    private Leaves() {}
}
