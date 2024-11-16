package io.github.startsmercury.visual_snowy_leaves.impl.client.util;

import io.github.startsmercury.visual_snowy_leaves.mixin.client.transition.minecraft.ClientChunkCache$StorageAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.transition.minecraft.ClientChunkCacheAccessor;
import net.minecraft.client.multiplayer.ClientLevel;

public final class Chunks {
    public static void requestRebuildAll(final ClientLevel level) {
        final var chunkSource = (ClientChunkCacheAccessor) level.getChunkSource();
        final var storage = (ClientChunkCache$StorageAccessor) (Object) chunkSource.getStorage();
        if (storage == null) {
            return;
        }

        final var chunks = storage.getChunks();
        final var chunkCount = chunks.length();

        for (var i = 0; i < chunkCount; i++) {
            final var chunk = chunks.getPlain(i);
            if (chunk == null) {
                continue;
            }

            final var pos = chunk.getPos();
            final var levelChunkSections = chunk.getSections();

            for (var k = 0; k < levelChunkSections.length; ++k) {
                final var l = level.getSectionYFromSectionIndex(k);
                level.setSectionDirtyWithNeighbors(pos.x, l, pos.z);
            }
        }
    }

    private Chunks() {}
}
