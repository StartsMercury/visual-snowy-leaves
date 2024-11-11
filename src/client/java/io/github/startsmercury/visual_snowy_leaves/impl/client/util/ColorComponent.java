package io.github.startsmercury.visual_snowy_leaves.impl.client.util;

import org.jetbrains.annotations.Range;

public final class ColorComponent {
    public static @Range(from = 0, to = 0xFF) int div(
        final @Range(from = 0, to = 0xFF) int lhs,
        final @Range(from = 0, to = 0xFF) int rhs
    ) {
        return rhs == 0 ? 0xFF : Math.min(0xFF * lhs / rhs, 0xFF);
    }

    private ColorComponent() {}
}
