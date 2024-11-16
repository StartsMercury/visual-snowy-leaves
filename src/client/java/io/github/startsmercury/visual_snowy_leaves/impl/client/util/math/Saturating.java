package io.github.startsmercury.visual_snowy_leaves.impl.client.util.math;

public final class Saturating {
    public static int uAdd(final int x, final int y) {
        return Integer.compareUnsigned(x, Integer.MIN_VALUE - y) > 0 ? -1 : x + y;
    }

    public static int uFma(final int a, final int b, final int c) {
        if (b == 0 || c == 0) {
            return a;
        } else if (Integer.compareUnsigned(b, Integer.divideUnsigned(-1 - a, c)) > 0) {
            return -1;
        } else {
            return a + b * c;
        }
    }

    private Saturating() {}
}
