package io.github.startsmercury.visual_snowy_leaves.impl.client.sodium;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslVertexFormatElement;

public class ParticleVertexExtension {
    public static final VertexFormat FORMAT = VertexFormat.builder()
        .add("Position", VertexFormatElement.POSITION)
        .add("UV0", VertexFormatElement.UV0)
        .add("Color", VertexFormatElement.COLOR)
        .add("UV2", VertexFormatElement.UV2)
        .add(VslVertexFormatElement.VslIndex.name(), VslVertexFormatElement.VslIndex.value())
        .build();

    private ParticleVertexExtension() {
    }
}
