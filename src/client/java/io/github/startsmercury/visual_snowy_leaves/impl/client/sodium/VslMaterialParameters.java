package io.github.startsmercury.visual_snowy_leaves.impl.client.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.MaterialParameters;

/**
 * Extended bits on unused offsets from Sodium's {@code MaterialParameters}.
 * @see MaterialParameters
 */
public final class VslMaterialParameters {
    /** @see #OFFSET_SNOW_MARKED */
    private static final int MASK_SNOW_MARKED = 0b01000000;

    /**
     * Offset where {@code snowMarked} flag injects into {@code Material.packed}.
     * @implSpec This must be set to something other than what Sodium uses, as of
     * {@code 0.8.2}, of which are:
     * <ul>
     *     <li>{@code 0b0000000_} - {@code Material.mipped} bit flag</li>
     *     <li>{@code 0b00000__0} - encodes {@code Material.alphaCutOff}</li>
     *     <li>{@code 0b_____000} - unused.</li>
     * </ul>
     * @implNote The offset to be used will account the (unlikely) increase of
     *     {@code Material.alphaCutOff} bits and the possibility of another
     *     injection possibly utilizing the last bit at offset seven.
     * @see Material#packed
     */
    public static final int OFFSET_SNOW_MARKED = Integer.numberOfTrailingZeros(MASK_SNOW_MARKED);

    /**
     * Encodes extended bits into unused offsets.
     * <p>
     * This method expects valid {@code MaterialParameters.pack} bits.
     * @param base  The original bits to encode into.
     * @return  The modified bits with additional encoded bits.
     */
    public static int packInto(final int base, final boolean snowMarked) {
        // NOTE: implemented as such instead of branched if-else
        return
            // Unset snowMarked if it was set
            base & ~MASK_SNOW_MARKED
            // Reflect snowMarked being true
            | (snowMarked ? 1 : 0) << OFFSET_SNOW_MARKED;
    }
}
