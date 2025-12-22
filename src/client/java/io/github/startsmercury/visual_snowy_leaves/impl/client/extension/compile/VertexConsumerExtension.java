package io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile;

import com.mojang.blaze3d.vertex.VertexConsumer;

public interface VertexConsumerExtension {
    int SNOW_MARKED = 1;

    /**
     * Should vertex brightness be stored in the alpha channel.
     * <p>
     * Instead of premultiplying the vertex brightness into the RGB color
     * channels, the color would be assumed fully opaque and the brightness is
     * stored in that channel instead. This moves the multiplication to the
     * vertex shader along with enforcing full opacity.
     *
     * @return Should vertex brightness be stored in the alpha channel.
     */
    default boolean visual_snowy_leaves$alphaAsAo() {
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
