package io.github.startsmercury.visual_snowy_leaves.mixin.client.transition.minecraft;

import io.github.startsmercury.visual_snowy_leaves.impl.client.SnowData;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowDataAware;
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

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements SnowDataAware, VisualSnowyLeavesAware {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Unique
    private final SnowData snowData = new SnowData();

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public VisualSnowyLeavesImpl getVisualSnowyLeaves() {
        return this.minecraft.getVisualSnowyLeaves();
    }

    @Override
    public SnowData visual_snowy_leaves$getSnowData() {
        return this.snowData;
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
        this.snowData.tick((ClientLevel) (Object) this);
    }
}
