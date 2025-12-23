package io.github.startsmercury.visual_snowy_leaves.impl.client.config.time;

public abstract class Tick18<Self extends Tick18<Self>> extends Tick32<Self> {
    public static final int MAX_VALUE = (0b1 << 18 - 1) - 0b1;

    protected Tick18(final int ticks) {
        super(ticks);
        assert Integer.compareUnsigned(ticks, MAX_VALUE) < 0;
    }
}
