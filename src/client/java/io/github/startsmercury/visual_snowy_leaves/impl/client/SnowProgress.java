package io.github.startsmercury.visual_snowy_leaves.impl.client;

import io.github.startsmercury.visual_snowy_leaves.impl.client.config.Config;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import net.minecraft.client.multiplayer.ClientLevel;

public class SnowProgress {
    private int accumulatedTicks;

    public int getAccumulatedTicks() {
        return this.accumulatedTicks;
    }

    public void onTransitionDurationChange(final int oldTicks, final int newTicks) {
        if (oldTicks == 0 || newTicks == 0) {
            this.accumulatedTicks = 0;
            return;
        }
        this.accumulatedTicks = (int) (
            Integer.toUnsignedLong(this.accumulatedTicks)
                * Integer.toUnsignedLong(newTicks)
                / Integer.toUnsignedLong(oldTicks)
        );
    }

    public void tick(final ClientLevel level) {
        final var config = ((VisualSnowyLeavesAware) level).getVisualSnowyLeaves().getConfig();

        if (level.isRaining()) {
            this.tickSnowinessIncrement(config);
        } else {
            this.tickSnowinessDecrement(config);
        }
    }

    public void tickSnowinessDecrement(final Config config) {
        final var accumulatedTicks = this.accumulatedTicks;
        if (accumulatedTicks == 0) {
            return;
        }
        this.accumulatedTicks = accumulatedTicks - 1;
    }

    public void tickSnowinessIncrement(final Config config) {
        final var accumulatedTicks = this.accumulatedTicks;
        if (Integer.compareUnsigned(accumulatedTicks, config.transitionDuration().asTicks()) >= 0) {
            return;
        }
        this.accumulatedTicks = accumulatedTicks + 1;
    }
}
