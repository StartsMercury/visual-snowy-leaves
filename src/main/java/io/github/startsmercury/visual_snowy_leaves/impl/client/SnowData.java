package io.github.startsmercury.visual_snowy_leaves.impl.client;

import io.github.startsmercury.visual_snowy_leaves.impl.client.config.Config;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.Chunks;
import net.minecraft.client.multiplayer.ClientLevel;

public class SnowData {
    private int accumulatedTicks;

    private int ticksSinceBuildRequest;

    public int getAccumulatedTicks() {
        return this.accumulatedTicks;
    }

    public void onTransitionDurationChange(final int oldTicks, final int newTicks) {
        if (newTicks == 0) {
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
        final var ticksSinceBuildRequest = this.ticksSinceBuildRequest;
        final var config = ((VisualSnowyLeavesAware) level).getVisualSnowyLeaves().getConfig();

        final var changed = level.isRaining()
            ? this.tickSnowinessIncrement(config)
            : this.tickSnowinessDecrement(config);

        if (changed) {
            if (ticksSinceBuildRequest < config.rebuildInterval().asTicks()) {
                this.ticksSinceBuildRequest = ticksSinceBuildRequest + 1;
                return;
            }
        } else {
            if (ticksSinceBuildRequest <= 0) {
                return;
            }
        }

        Chunks.requestRebuildAll(level);

        this.ticksSinceBuildRequest = 0;
    }

    public boolean tickSnowinessDecrement(final Config config) {
        final var accumulatedTicks = this.accumulatedTicks;
        if (accumulatedTicks == 0) {
            return false;
        }
        this.accumulatedTicks = accumulatedTicks - 1;
        return true;
    }

    public boolean tickSnowinessIncrement(final Config config) {
        final var accumulatedTicks = this.accumulatedTicks;
        if (Integer.compareUnsigned(accumulatedTicks, config.transitionDuration().asTicks()) >= 0) {
            return false;
        }
        this.accumulatedTicks = accumulatedTicks + 1;
        return true;
    }
}
