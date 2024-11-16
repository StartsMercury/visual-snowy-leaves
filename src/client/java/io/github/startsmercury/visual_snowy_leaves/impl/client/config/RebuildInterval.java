package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslConstants;
import net.minecraft.SharedConstants;

public final class RebuildInterval implements Comparable<RebuildInterval> {
    public static final Codec<RebuildInterval> CODEC = Codec.STRING.comapFlatMap(
        input -> {
            try {
                return DataResult.success(new RebuildInterval(TickUtil.parse(input)));
            } catch (final TickParseException cause) {
                return DataResult.error(cause::getMessage);
            }
        },
        self -> TickUtil.format(self.ticks, true)
    );

    public static RebuildInterval fromTicks(final int ticks) {
        return new RebuildInterval(ticks);
    }

    private final int ticks;

    private RebuildInterval(final int ticks) {
        this.ticks = ticks;
    }

    public long asNanos() {
        return Integer.toUnsignedLong(this.ticks) * VslConstants.NANOS_PER_TICK;
    }

    public long asMillis() {
        return Integer.toUnsignedLong(this.ticks) * VslConstants.MILLIS_PER_TICK;
    }

    public int asTicks() {
        return this.ticks;
    }

    public int asSeconds() {
        return Integer.divideUnsigned(this.ticks, SharedConstants.TICKS_PER_SECOND);
    }

    public int asMinutes() {
        return Integer.divideUnsigned(this.ticks, SharedConstants.TICKS_PER_MINUTE);
    }

    public int asHours() {
        return Integer.divideUnsigned(this.ticks, VslConstants.TICKS_PER_HOUR);
    }

    @Override
    public int compareTo(final RebuildInterval rhs) {
        return Integer.compareUnsigned(this.ticks, rhs.ticks);
    }
}
