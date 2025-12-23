package io.github.startsmercury.visual_snowy_leaves.impl.client.config.time;

import io.github.startsmercury.visual_snowy_leaves.impl.client.VslConstants;
import net.minecraft.SharedConstants;

public abstract class Tick32<Self extends Tick32<Self>> implements Comparable<Self> {
    public static final int MAX_VALUE = 0b11111111_11111111_11111111_11111111;

    private final int ticks;

    protected Tick32(final int ticks) {
        this.ticks = ticks;
    }

    public abstract Self withTicks(final int ticks);

    public final long asNanos() {
        return Integer.toUnsignedLong(this.ticks) * VslConstants.NANOS_PER_TICK;
    }

    public final long asMillis() {
        return Integer.toUnsignedLong(this.ticks) * VslConstants.MILLIS_PER_TICK;
    }

    public final int asTicks() {
        return this.ticks;
    }

    public final int asSeconds() {
        return Integer.divideUnsigned(this.ticks, SharedConstants.TICKS_PER_SECOND);
    }

    public final int asMinutes() {
        return Integer.divideUnsigned(this.ticks, SharedConstants.TICKS_PER_MINUTE);
    }

    public final int asHours() {
        return Integer.divideUnsigned(this.ticks, VslConstants.TICKS_PER_HOUR);
    }

    @Override
    public final int compareTo(final Self rhs) {
        return Integer.compareUnsigned(this.ticks, ((Tick32<?>) rhs).ticks);
    }

    protected abstract boolean isInstance(final Object obj);

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof final Tick32<?> other) {
            return this.equals(other) && other.isInstance(this);
        } else {
            return false;
        }
    }

    protected final boolean equals(final Tick32<?> other) {
        return this.ticks == other.ticks;
    }

    @Override
    public final int hashCode() {
        return Integer.hashCode(this.ticks);
    }
}
