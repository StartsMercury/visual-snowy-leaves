package io.github.startsmercury.visual_snowy_leaves.impl.client.sodium;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.startsmercury.visual_snowy_leaves.impl.client.vertices.VslVertexFormatElement;

public class ColorVertexExtension {
    public static final VertexFormat FORMAT = VertexFormat.builder()
        .add("Position", VertexFormatElement.POSITION)
        .add("Color", VertexFormatElement.COLOR)
        .add(VslVertexFormatElement.VslIndex.name(), VslVertexFormatElement.VslIndex.value())
        .build();

    private ColorVertexExtension() {
    }
}
