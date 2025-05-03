package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import static io.github.startsmercury.visual_snowy_leaves.impl.client.VslConstants.Duration.ONE_TICK;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.time.Duration;

public final class TransitionDuration extends Tick32<TransitionDuration> {
    public static final Codec<TransitionDuration> CODEC;
    private static final Duration MAX_DURATION;
    public static final TransitionDuration MAX_VALUE;
    public static final TransitionDuration MIN_VALUE;

    static {
        MIN_VALUE = TransitionDuration.fromTicksUnchecked(0);
        MAX_VALUE = TransitionDuration.fromTicksUnchecked(Integer.divideUnsigned(-1, 255 * 255));

        MAX_DURATION = ONE_TICK.multipliedBy(MAX_VALUE.asTicks());
        CODEC = Codec.STRING.comapFlatMap(
            input -> {
                try {
                    final var ticks = Math.min(TickUtil.parse(input), MAX_VALUE.asTicks());
                    return DataResult.success(new TransitionDuration(ticks));
                } catch (final TickParseException cause) {
                    return DataResult.error(cause::getMessage);
                }
            },
            self -> TickUtil.format(self.asTicks(), true)
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
        super(ticks);
    }

    @Override
    public TransitionDuration withTicks(final int ticks) {
        return new TransitionDuration(ticks);
    }

    @Override
    protected boolean isInstance(final Object obj) {
        return obj instanceof TransitionDuration;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof final TransitionDuration other) {
            return this.equals(other);
        } else {
            return false;
        }
    }
}
