package io.github.startsmercury.visual_snowy_leaves.mixin.client.transition.minecraft;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ClientChunkCache.Storage.class)
public interface ClientChunkCache$StorageAccessor {
    @Accessor
    AtomicReferenceArray<LevelChunk> getChunks();
}
