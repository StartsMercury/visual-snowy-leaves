package io.github.startsmercury.visual_snowy_leaves.impl.client;

import net.minecraft.SharedConstants;

import java.util.regex.Pattern;

public final class VslConstants {
    public static final class Duration {
        public static final java.time.Duration ONE_SECOND = java.time.Duration.ofSeconds(1L);

        public static final java.time.Duration ONE_TICK = ONE_SECOND.dividedBy(SharedConstants.TICKS_PER_SECOND);

        private Duration() {}
    }

    public static final Pattern COLUMN_PATTERN = Pattern.compile("column\\s+([0-9]+)");

    public static final Pattern LINE_PATTERN = Pattern.compile("line\\s+([0-9]+)");

    public static final String MODID = "visual-snowy-leaves";

    public static final String NAME = "Visual Snowy Leaves";

    public static final String CONFIG_EXT = ".json";

    public static final String CONFIG_NAME = MODID + CONFIG_EXT;

    public static final String IGNORE_TAG = "@" + MODID + "::ignored";

    public static final int NANO = 1000;

    public static final int MILLI = 1000;

    public static final int SECONDS_PER_MINUTE = 60;

    public static final int MINUTES_PER_HOUR = 60;

    public static final int NANOS_PER_TICK = NANO / SharedConstants.TICKS_PER_SECOND;

    public static final int TICKS_PER_HOUR = SharedConstants.TICKS_PER_MINUTE * MINUTES_PER_HOUR;

    public static final int MILLIS_PER_TICK = MILLI / SharedConstants.TICKS_PER_SECOND;

    private VslConstants() {}
}
