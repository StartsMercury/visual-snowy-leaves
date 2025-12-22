package io.github.startsmercury.visual_snowy_leaves.impl.client;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public enum VslVertexFormatElement {
    VslIndex(
        0,
        VertexFormatElement.Type.INT,
        VertexFormatElement.Usage.GENERIC,
        // Scalar element
        1
    );

    private final VertexFormatElement value;

    public VertexFormatElement value() {
        return this.value;
    }

    VslVertexFormatElement(
        final int index,
        final VertexFormatElement.Type type,
        final VertexFormatElement.Usage usage,
        final int count
    ) {
        // REALLY make sure...
        synchronized (VertexFormatElement.class) {
            final var occupiedIndices =
                // If we mask nothing from (1 << id), we should match all registered elements...
                VertexFormatElement.elementsFromMask(0xFFFFFFFF)
                    .mapToInt(VertexFormatElement::id)
                    .collect(IntOpenHashSet::new, IntOpenHashSet::add, IntOpenHashSet::addAll);
            for (var id = 0; id < VertexFormatElement.MAX_COUNT; id++) {
                if (!occupiedIndices.contains(id)) {
                    this.value = VertexFormatElement.register(id, index, type, usage, count);
                    return;
                }
            }
        }
        throw new RuntimeException("Too many mods are registering unto VertexFormatElement");
    }
}
