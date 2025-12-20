package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public enum SnowyMode {
    /**
     * Snow leaves disabled.
     */
    NEVER,
    /**
     * Snowy leaves when it is snowing at a block.
     */
    SNOWING,
    /**
     * Snowy leaves regardless of weather or temperature.
     */
    ALWAYS;

    public static final Codec<SnowyMode> CODEC = Codec.STRING.comapFlatMap(
        input -> {
            try {
                return DataResult.success(SnowyMode.valueOf(input.toUpperCase(Locale.ROOT)));
            } catch (final IllegalArgumentException cause) {
                return DataResult.error(() -> "Unrecognized snowy mode");
            }
        },
        self -> self.name().toLowerCase(Locale.ROOT)
    );
}
