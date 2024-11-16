package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslConstants;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.IntFunction;

public enum SnowyMode implements OptionEnum {
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

    private static final IntFunction<SnowyMode> BY_ID =
        ByIdMap.continuous(SnowyMode::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);

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

    public static SnowyMode byId(final int id) {
        return BY_ID.apply(id);
    }

    @Override
    public int getId() {
        return this.ordinal();
    }

    @Override
    public @NotNull String getKey() {
        return VslConstants.MODID + ".option.snowyMode." + this.name().toLowerCase(Locale.ROOT);
    }
}
