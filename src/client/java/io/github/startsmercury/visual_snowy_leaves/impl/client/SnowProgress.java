package io.github.startsmercury.visual_snowy_leaves.impl.client;

import io.github.startsmercury.visual_snowy_leaves.impl.client.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public class SnowProgress {
    private long accumulatedTicks;
    private long minimum;
    private long maximum;

    public long getAccumulatedTicks() {
        return this.accumulatedTicks;
    }

    public void update(final Config config) {
        this.update(
            -Integer.toUnsignedLong(config.freshFreezeDelay().asTicks()),
            Integer.toUnsignedLong(config.transitionDuration().asTicks())
                + Integer.toUnsignedLong(config.fullMeltDelay().asTicks())
        );
    }

    public void update(final long minimum, final long maximum) {
        this.accumulatedTicks = this.maximum == 0 || maximum == 0 ? 0 : Mth.clamp(
            Math.round((double) this.accumulatedTicks * maximum / this.maximum),
            minimum,
            maximum
        );

        this.minimum = minimum;
        this.maximum = maximum;
    }

    public void tick(final ClientLevel level) {
        if (level.isRaining()) {
            this.tickSnowinessIncrement();
        } else {
            this.tickSnowinessDecrement();
        }
    }

    public void tickSnowinessDecrement() {
        final var accumulatedTicks = this.accumulatedTicks;
        if (accumulatedTicks > this.minimum) {
            this.accumulatedTicks = accumulatedTicks - 1;
        } else {
            this.accumulatedTicks = this.minimum;
        }
    }

    public void tickSnowinessIncrement() {
        final var accumulatedTicks = this.accumulatedTicks;
        if (this.accumulatedTicks < this.maximum) {
            this.accumulatedTicks = accumulatedTicks + 1;
        } else {
            this.accumulatedTicks = this.maximum;
        }
    }
}
