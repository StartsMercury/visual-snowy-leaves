package io.github.startsmercury.visual_snowy_leaves.mixin.client.core.transition.minecraft;

import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.CompileVisualSnowyLeavesAware;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Minecraft.class)
@SuppressWarnings("deprecation")
public class MinecraftMixin implements CompileVisualSnowyLeavesAware {
    @Unique
    private final VisualSnowyLeavesImpl visualSnowyLeaves =
        new VisualSnowyLeavesImpl((Minecraft) (Object) this);

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public VisualSnowyLeavesImpl getVisualSnowyLeaves() {
        return this.visualSnowyLeaves;
    }
}
