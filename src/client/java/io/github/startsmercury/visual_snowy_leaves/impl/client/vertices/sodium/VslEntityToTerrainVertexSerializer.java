package io.github.startsmercury.visual_snowy_leaves.impl.client.vertices.sodium;

import io.github.startsmercury.visual_snowy_leaves.impl.client.vertices.VslVertexFormats;
import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;

public class VslEntityToTerrainVertexSerializer implements VertexSerializer {
    @Override
    public void serialize(long src, long dst, final int vertexCount) {
        for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex) {
            //  0..12=> 0..12: Position    3 x Float         = 3 x 4 = 12
            // 12..16=>12..16: Color       4 x Unsigned Byte = 4 x 1 =  4
            // 16..24=>16..24: UV0         2 x Float         = 2 x 4 =  8
            MemoryIntrinsics.copyMemory(src, dst, 24);
            // 24..28=>(null): UV1         2 x Short         = 2 x 2 =  4
            // 28..32=>24..28: UV2         2 x Short         = 2 x 2 =  4
            // 32..36=>28..32: Normal      3 x Byte          = 3 x 1 =  3 -> 4 (after padding)
            // 36..40=>32..36: VslIndex    1 x Int           = 1 x 4 =  4
            MemoryIntrinsics.copyMemory(src + 28, dst + 24, 12);

            src += VslVertexFormats.ENTITY.getVertexSize();
            dst += VslVertexFormats.TERRAIN.getVertexSize();
        }
    }
}
