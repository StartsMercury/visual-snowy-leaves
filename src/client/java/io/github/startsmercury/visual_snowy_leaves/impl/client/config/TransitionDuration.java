package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslConstants;
import net.minecraft.SharedConstants;

import java.time.Duration;

import static io.github.startsmercury.visual_snowy_leaves.impl.client.VslConstants.Duration.ONE_TICK;

public final class TransitionDuration implements Comparable<TransitionDuration> {
    public static final Codec<TransitionDuration> CODEC;
    private static final Duration MAX_DURATION;
    public static final TransitionDuration MAX_VALUE;
    public static final TransitionDuration MIN_VALUE;

    static {
        MIN_VALUE = TransitionDuration.fromTicksUnchecked(0);
        MAX_VALUE = TransitionDuration.fromTicksUnchecked(Integer.divideUnsigned(-1, 255 * 255));

        MAX_DURATION = ONE_TICK.multipliedBy(MAX_VALUE.ticks);
        CODEC = Codec.STRING.comapFlatMap(
            input -> {
                try {
                    final var ticks = Math.min(TickUtil.parse(input), MAX_VALUE.ticks);
                    return DataResult.success(new TransitionDuration(ticks));
                } catch (final TickParseException cause) {
                    return DataResult.error(cause::getMessage);
                }
            },
            self -> TickUtil.format(self.ticks, true)
        );
    }

    /**
     * Creates transition duration.
     *
     * @param ticks  The duration in ticks.
     * @return A transition duration.
     */
    public static TransitionDuration tryFromTicks(final int ticks) {
        if (Integer.compareUnsigned(ticks, TransitionDuration.MAX_VALUE.asTicks()) >= 0) {
            final var message = "Unsupported ticks is greater than TransitionDuration.MAX_VALUE";
            throw new IllegalArgumentException(message);
        }
        return new TransitionDuration(ticks);
    }

    /**
     * Creates transition ticks without validation.
     *
     * @param ticks  The raw ticks.
     * @deprecated Input ticks must not be greater than {@link #MAX_VALUE}.
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static TransitionDuration fromTicksUnchecked(final int ticks) {
        return new TransitionDuration(ticks);
    }

    private final int ticks;

    /**
     * Creates a new transition duration.
     *
     * @param ticks The duration in ticks.
     * @deprecated Internals should prefer {@code tryFromTicks},
     *     {@code fromTicksUnchecked}, or equivalent wrappers.
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    private TransitionDuration(final int ticks) {
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
    public int compareTo(final TransitionDuration rhs) {
        return Integer.compareUnsigned(this.ticks, rhs.ticks);
    }
}
