package io.github.startsmercury.visual_snowy_leaves.impl.client.sodium;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslVertexFormatElement;

public final class EntityVertexExtension {
    public static final VertexFormat FORMAT = VertexFormat.builder()
        .add("Position", VertexFormatElement.POSITION)
        .add("Color", VertexFormatElement.COLOR)
        .add("UV0", VertexFormatElement.UV0)
        .add("UV1", VertexFormatElement.UV1)
        .add("UV2", VertexFormatElement.UV2)
        .add("Normal", VertexFormatElement.NORMAL)
        .add(VslVertexFormatElement.VslIndex.name(), VslVertexFormatElement.VslIndex.value())
        .padding(1).build();

    public static void write(final long ptr, final int index) {
        VslIndexAttribute.set(ptr + 36L, index);
    }

    private EntityVertexExtension() {}
}
