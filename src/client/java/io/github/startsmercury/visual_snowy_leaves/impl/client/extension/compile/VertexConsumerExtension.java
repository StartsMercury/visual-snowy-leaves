package io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile;

import com.mojang.blaze3d.vertex.VertexConsumer;

public interface VertexConsumerExtension {
    int SNOW_MARKED = 1;

    default boolean visual_snowy_leaves$isExtended() {
        return false;
    }

    /**
     * Sets the index used for future added vertices.
     * <p>
     *
     * @param index  The index.
     * @return {@code this} VertexConsumer.
     */
    default VertexConsumer visual_snowy_leaves$beginIndex(final int index) {
        return (VertexConsumer) this;
    }

    default VertexConsumer visual_snowy_leaves$endIndex() {
        return (VertexConsumer) this;
    }
}
