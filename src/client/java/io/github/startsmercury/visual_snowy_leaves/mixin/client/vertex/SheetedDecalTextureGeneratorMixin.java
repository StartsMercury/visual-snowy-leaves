package io.github.startsmercury.visual_snowy_leaves.mixin.client.vertex;

import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import net.minecraft.util.CommonColors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SheetedDecalTextureGenerator.class)
public abstract class SheetedDecalTextureGeneratorMixin implements VertexConsumer, VertexConsumerExtension {
//    @Final
//    @Shadow
//    private VertexConsumer delegate;
//
//    @Override
//    public VertexConsumer visual_snowy_leaves$setTint(final int r, final int g, final int b, final int a) {
//        this.delegate.visual_snowy_leaves$setTint(CommonColors.WHITE);
//        return this;
//    }
//
//    @Override
//    public VertexConsumer visual_snowy_leaves$setTint(final int color) {
//        this.delegate.visual_snowy_leaves$setTint(CommonColors.WHITE);
//        return this;
//    }
}
