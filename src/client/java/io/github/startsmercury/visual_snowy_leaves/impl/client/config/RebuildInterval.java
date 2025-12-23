package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.time.Tick32;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.time.TickParseException;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.time.TickUtil;

@Deprecated(forRemoval = true)
public final class RebuildInterval extends Tick32<RebuildInterval> {
    public static final Codec<RebuildInterval> CODEC = Codec.STRING.comapFlatMap(
        input -> {
            try {
                return DataResult.success(new RebuildInterval(TickUtil.parse(input)));
            } catch (final TickParseException cause) {
                return DataResult.error(cause::getMessage);
            }
        },
        self -> TickUtil.format(self.asTicks(), true)
    );

    public static final RebuildInterval ZERO = RebuildInterval.fromTicks(0);

    public static RebuildInterval fromTicks(final int ticks) {
        return new RebuildInterval(ticks);
    }

    private RebuildInterval(final int ticks) {
        super(ticks);
    }

    @Override
    public RebuildInterval withTicks(final int ticks) {
        return new RebuildInterval(ticks);
    }

    @Override
    protected boolean isInstance(final Object obj) {
        return obj instanceof RebuildInterval;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof final RebuildInterval other) {
            return this.equals(other);
        } else {
            return false;
        }
    }
}
