package io.github.startsmercury.visual_snowy_leaves.mixin.client.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SpriteCoordinateExpander.class)
public abstract class SpriteCoordinateExpanderMixin implements VertexConsumer, VertexConsumerExtension {
//    @Final
//    @Shadow
//    private VertexConsumer delegate;
//
//    @Override
//    public VertexConsumer visual_snowy_leaves$setTint(final int r, final int g, final int b, final int a) {
//        return this.delegate.visual_snowy_leaves$setTint(r, g, b, a);
//    }

//    @Override
//    public VertexConsumer visual_snowy_leaves$setTint(final int color) {
//        return this.delegate.visual_snowy_leaves$setTint(color);
//    }
}
