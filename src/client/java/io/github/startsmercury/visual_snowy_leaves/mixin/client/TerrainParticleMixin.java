package io.github.startsmercury.visual_snowy_leaves.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(TerrainParticle.class)
public abstract class TerrainParticleMixin {
    @ModifyExpressionValue(
        method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
        at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"),
        slice = @Slice(from = @At(value = "FIELD", ordinal = 0, target = "Lnet/minecraft/world/level/block/Blocks;GRASS_BLOCK:Lnet/minecraft/world/level/block/Block;"))
    )
    private boolean overrideParticleTint(
        final boolean original,
        final @Local(ordinal = 0, argsOnly = true) ClientLevel level,
        final @Local(ordinal = 0, argsOnly = true) BlockPos blockPos
    ) {
        return original || VisualSnowyLeavesImpl.isSnowyAt(level, blockPos);
    }
}
