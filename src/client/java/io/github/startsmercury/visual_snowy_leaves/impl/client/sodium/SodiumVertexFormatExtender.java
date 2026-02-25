package io.github.startsmercury.visual_snowy_leaves.impl.client.sodium;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ColorVertex;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.caffeinemc.mods.sodium.api.vertex.format.common.GlyphVertex;
import net.caffeinemc.mods.sodium.api.vertex.format.common.LineVertex;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ParticleVertex;

public enum SodiumVertexFormatExtender {
    DISABLED,
    ENABLED {
        @Override
        public VertexFormat toExtension(final VertexFormat format) {
            if (format == ColorVertex.FORMAT) {
                return ColorVertexExtension.FORMAT;
            } else if (format == EntityVertex.FORMAT) {
                return EntityVertexExtension.FORMAT;
            } else if (format == GlyphVertex.FORMAT) {
                return GlyphVertexExtension.FORMAT;
            } else if (format == LineVertex.FORMAT) {
                return LineVertexExtension.FORMAT;
            } else if (format == ParticleVertex.FORMAT) {
                return ParticleVertexExtension.FORMAT;
            } else {
                return format;
            }
        }
    };

    public VertexFormat toExtension(final VertexFormat format) {
        return format;
    }
}
