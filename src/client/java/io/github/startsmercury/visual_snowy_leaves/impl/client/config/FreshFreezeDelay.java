package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.time.Tick18;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.time.TickParseException;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.time.TickUtil;

public final class FreshFreezeDelay extends Tick18<FreshFreezeDelay> {
    public static final Codec<FreshFreezeDelay> CODEC = Codec.STRING.comapFlatMap(
        input -> {
            try {
                final var ticks = Math.min(TickUtil.parse(input), MAX_VALUE);
                return DataResult.success(new FreshFreezeDelay(ticks));
            } catch (final TickParseException cause) {
                return DataResult.error(cause::getMessage);
            }
        },
        self -> TickUtil.format(self.asTicks(), true)
    );

    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static FreshFreezeDelay fromTicksUnchecked(final int ticks) {
        return new FreshFreezeDelay(ticks);
    }

    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    private FreshFreezeDelay(final int ticks) {
        super(ticks);
    }

    @Override
    public FreshFreezeDelay withTicks(final int ticks) {
        return new FreshFreezeDelay(ticks);
    }

    @Override
    protected boolean isInstance(final Object obj) {
        return obj instanceof FreshFreezeDelay;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof final FreshFreezeDelay other) {
            return this.equals(other);
        } else {
            return false;
        }
    }
}
