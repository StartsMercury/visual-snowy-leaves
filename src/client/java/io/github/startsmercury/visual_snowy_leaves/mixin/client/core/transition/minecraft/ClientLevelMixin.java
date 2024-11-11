package io.github.startsmercury.visual_snowy_leaves.mixin.client.core.transition.minecraft;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.VisualSnowyLeavesAware;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements VisualSnowyLeavesAware {
    @Unique
    private final VisualSnowyLeavesImpl visualSnowyLeaves = new VisualSnowyLeavesImpl();

    @Override
    public VisualSnowyLeavesImpl getVisualSnowyLeaves() {
        return this.visualSnowyLeaves;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fastForwardOnCreate(
        final CallbackInfo callback,
        final @Local(ordinal = 0, argsOnly = true) ClientLevel.ClientLevelData clientLevelData
    ) {
        if (clientLevelData.isRaining()) {
            this.visualSnowyLeaves.setSnowiness(this.visualSnowyLeaves.getMaxSnowiness());
        }
    }

    @Inject(
        method = "tick(Ljava/util/function/BooleanSupplier;)V",
        at = @At(
            value = "INVOKE",
            target = """
                Lnet/minecraft/client/multiplayer/ClientLevel;  \
                getProfiler(                                    \
                                                                \
                ) Lnet/minecraft/util/profiling/ProfilerFiller; \
            """,
            ordinal = 0
        )
    )
    private void updateSnowiness(final CallbackInfo callback) {
        this.visualSnowyLeaves.tick((ClientLevel) (Object) this);
    }
}
