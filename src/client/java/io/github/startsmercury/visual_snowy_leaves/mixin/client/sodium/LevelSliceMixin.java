//TODO Re-eneble when Sodium is available in non-obfuscated
//package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;
//
//import io.github.startsmercury.visual_snowy_leaves.impl.client.SnowProgress;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.BlockAndTintGetterExtension;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowProgressAware;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
//import net.caffeinemc.mods.sodium.client.world.LevelSlice;
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.core.BlockPos;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//
//@Mixin(LevelSlice.class)
//public class LevelSliceMixin implements BlockAndTintGetterExtension {
//    @Final
//    @Shadow
//    private ClientLevel level;
//
//    @Override
//    @SuppressWarnings("AddedMixinMembersNamePattern")
//    public VisualSnowyLeavesImpl getVisualSnowyLeaves() {
//        return ((VisualSnowyLeavesAware) this.level).getVisualSnowyLeaves();
//    }
//
//    @Override
//    public boolean visual_snowy_leaves$coldEnoughToSnow(final BlockPos blockPos) {
//        return ((SnowAware) this.level).visual_snowy_leaves$coldEnoughToSnow(blockPos);
//    }
//
//    @Override
//    public SnowProgress visual_snowy_leaves$getSnowProgress() {
//        return ((SnowProgressAware) this.level).visual_snowy_leaves$getSnowProgress();
//    }
//}
