package io.github.startsmercury.visual_snowy_leaves.impl.client.vertices;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class VslVertexFormats {
    public static final VertexFormat TERRAIN = VertexFormat.builder()
        .add("Position", VertexFormatElement.POSITION)
        .add("Color", VertexFormatElement.COLOR)
        .add("UV0", VertexFormatElement.UV0)
        .add("UV2", VertexFormatElement.UV2)
        .add("Normal", VertexFormatElement.NORMAL)
        .padding(1)
        .add(VslVertexFormatElement.VslIndex.name(), VslVertexFormatElement.VslIndex.value())
        .build();

    public static final VertexFormat ENTITY = VertexFormat.builder()
        .add("Position", VertexFormatElement.POSITION)
        .add("Color", VertexFormatElement.COLOR)
        .add("UV0", VertexFormatElement.UV0)
        .add("UV1", VertexFormatElement.UV1)
        .add("UV2", VertexFormatElement.UV2)
        .add("Normal", VertexFormatElement.NORMAL)
        .padding(1)
        .add(VslVertexFormatElement.VslIndex.name(), VslVertexFormatElement.VslIndex.value())
        .build();
}
