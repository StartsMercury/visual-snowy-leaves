package io.github.startsmercury.visual_snowy_leaves.mixin.client.progress;

import io.github.startsmercury.visual_snowy_leaves.impl.client.SnowProgress;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowProgressAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// NOTE: `ClientLevel` is an implementation of `LevelReader` and thus implements
//        our `SnowAware` interface. See: `LevelReaderMixin`
@Mixin(ClientLevel.class)
public class ClientLevelMixin implements SnowProgressAware, VisualSnowyLeavesAware {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Unique
    private final SnowProgress snowProgress = new SnowProgress();

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public VisualSnowyLeavesImpl getVisualSnowyLeaves() {
        return this.minecraft.getVisualSnowyLeaves();
    }

    @Override
    public SnowProgress visual_snowy_leaves$getSnowProgress() {
        return this.snowProgress;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initSnowProgress(final CallbackInfo callback) {
        this.snowProgress.update(this.getVisualSnowyLeaves().getConfig());
    }

    @Inject(
        method = "tick(Ljava/util/function/BooleanSupplier;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;tickTime()V",
            ordinal = 0
        )
    )
    private void updateSnowiness(final CallbackInfo callback) {
        this.snowProgress.tick((ClientLevel) (Object) this);
    }
}
