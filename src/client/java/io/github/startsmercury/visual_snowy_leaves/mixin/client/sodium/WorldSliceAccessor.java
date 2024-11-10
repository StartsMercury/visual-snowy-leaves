package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldSlice.class)
public interface WorldSliceAccessor {
    @Accessor
    ClientLevel getWorld();
}
