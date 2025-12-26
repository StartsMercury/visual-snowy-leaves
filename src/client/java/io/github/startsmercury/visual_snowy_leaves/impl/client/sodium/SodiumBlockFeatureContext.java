package io.github.startsmercury.visual_snowy_leaves.impl.client.sodium;

public class SodiumBlockFeatureContext {
    private static boolean snowMarked;

    public static boolean isSnowMarked() {
        return SodiumBlockFeatureContext.snowMarked;
    }

    public static void setSnowMarked(final boolean snowMarked) {
        SodiumBlockFeatureContext.snowMarked = snowMarked;
    }

    private SodiumBlockFeatureContext() {}
}
