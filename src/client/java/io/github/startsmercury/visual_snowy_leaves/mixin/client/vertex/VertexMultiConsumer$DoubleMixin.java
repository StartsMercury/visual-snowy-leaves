package io.github.startsmercury.visual_snowy_leaves.mixin.client.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "com.mojang.blaze3d.vertex.VertexMultiConsumer$Double")
public abstract class VertexMultiConsumer$DoubleMixin implements VertexConsumer, VertexConsumerExtension {
    @Final
    @Shadow
    private VertexConsumer first;

    @Final
    @Shadow
    private VertexConsumer second;

    @Override
    public VertexConsumer visual_snowy_leaves$beginIndex(final int index) {
        this.first.visual_snowy_leaves$beginIndex(index);
        this.second.visual_snowy_leaves$beginIndex(index);
        return this;
    }

    @Override
    public VertexConsumer visual_snowy_leaves$endIndex() {
        this.first.visual_snowy_leaves$endIndex();
        this.second.visual_snowy_leaves$endIndex();
        return this;
    }
}
