package io.github.startsmercury.visual_snowy_leaves.impl.client.sodium;

public class SodiumSnowProgress {
    private static float value;

    public static float get() {
        return SodiumSnowProgress.value;
    }

    public static void set(final float value) {
        SodiumSnowProgress.value = value;
    }

    private SodiumSnowProgress() {}
}
