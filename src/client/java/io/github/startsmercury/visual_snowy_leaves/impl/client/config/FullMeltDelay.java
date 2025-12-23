package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.time.Tick18;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.time.TickParseException;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.time.TickUtil;

public final class FullMeltDelay extends Tick18<FullMeltDelay> {
    public static final Codec<FullMeltDelay> CODEC = Codec.STRING.comapFlatMap(
        input -> {
            try {
                final var ticks = Math.min(TickUtil.parse(input), MAX_VALUE);
                return DataResult.success(new FullMeltDelay(ticks));
            } catch (final TickParseException cause) {
                return DataResult.error(cause::getMessage);
            }
        },
        self -> TickUtil.format(self.asTicks(), true)
    );

    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static FullMeltDelay fromTicksUnchecked(final int ticks) {
        return new FullMeltDelay(ticks);
    }

    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    private FullMeltDelay(final int ticks) {
        super(ticks);
    }

    @Override
    public FullMeltDelay withTicks(final int ticks) {
        return new FullMeltDelay(ticks);
    }

    @Override
    protected boolean isInstance(final Object obj) {
        return obj instanceof FullMeltDelay;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof final FullMeltDelay other) {
            return this.equals(other);
        } else {
            return false;
        }
    }
}
