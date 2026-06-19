package io.github.startsmercury.visual_snowy_leaves.impl.client.vertices.sodium;

import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.SodiumBlockFeatureContext;
import io.github.startsmercury.visual_snowy_leaves.impl.client.vertices.VslVertexFormats;
import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import org.lwjgl.system.MemoryUtil;

public class ModelToEntityVertexSerializer implements VertexSerializer {
    @Override
    public void serialize(long src, long dst, final int vertexCount) {
        for (var vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
            //  0..12=> 0..12: Position    3 x Float         = 3 x 4 = 12
            // 12..16=>12..16: Color       4 x Unsigned Byte = 4 x 1 =  4
            // 16..24=>16..24: UV0         2 x Float         = 2 x 4 =  8
            // 24..28=>24..28: UV1         2 x Short         = 2 x 2 =  4
            // 28..32=>28..32: UV2         2 x Short         = 2 x 2 =  4
            // 32..36=>32..36: Normal      3 x Byte          = 3 x 1 =  3 -> 4 (after padding)
            MemoryIntrinsics.copyMemory(src, dst, 36);
            // (data)=>36..40: VslIndex    1 x Int           = 1 x 4 =  4
            MemoryUtil.memPutInt(dst + 36, SodiumBlockFeatureContext.isSnowMarked() ? 1 : 0);

            src += EntityVertex.STRIDE;
            dst += VslVertexFormats.ENTITY.getVertexSize();
        }
    }
}
