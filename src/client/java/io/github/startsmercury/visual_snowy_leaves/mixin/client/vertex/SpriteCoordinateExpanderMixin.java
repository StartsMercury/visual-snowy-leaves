package io.github.startsmercury.visual_snowy_leaves.mixin.client.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SpriteCoordinateExpander.class)
public class SpriteCoordinateExpanderMixin implements VertexConsumerExtension {
    @Shadow
    @Final
    private VertexConsumer delegate;

    @Override
    public boolean visual_snowy_leaves$isExtended() {
        return this.delegate.visual_snowy_leaves$isExtended();
    }
}
