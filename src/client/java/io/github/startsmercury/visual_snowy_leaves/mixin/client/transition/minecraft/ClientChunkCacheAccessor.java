package io.github.startsmercury.visual_snowy_leaves.mixin.client.transition.minecraft;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkCache.class)
public interface ClientChunkCacheAccessor {
    @Accessor
    ClientChunkCache.Storage getStorage();
}
