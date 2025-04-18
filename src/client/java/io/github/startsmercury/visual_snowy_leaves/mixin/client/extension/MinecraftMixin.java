package io.github.startsmercury.visual_snowy_leaves.mixin.client.extension;

import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.CompileVisualSnowyLeavesAware;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
@SuppressWarnings("deprecation")
public class MinecraftMixin implements CompileVisualSnowyLeavesAware {
    @Unique
    private VisualSnowyLeavesImpl visualSnowyLeaves;

    @Inject(
        method = "<init>(Lnet/minecraft/client/main/GameConfig;)V",
        at = @At(
            value = "FIELD",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/Minecraft;itemColors:Lnet/minecraft/client/color/item/ItemColors;",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void onInit(final CallbackInfo callback) {
        final var visualSnowyLeaves = new VisualSnowyLeavesImpl((Minecraft) (Object) this);
        this.visualSnowyLeaves = visualSnowyLeaves;
        visualSnowyLeaves.reloadConfig();
    }

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public VisualSnowyLeavesImpl getVisualSnowyLeaves() {
        return this.visualSnowyLeaves;
    }
}
