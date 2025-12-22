package io.github.startsmercury.visual_snowy_leaves.mixin.client.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

@Mixin(targets = "com.mojang.blaze3d.vertex.VertexMultiConsumer$Multiple")
public abstract class VertexMultiConsumer$MultipleMixin implements VertexConsumer, VertexConsumerExtension {
    @Shadow
    private void forEach(final Consumer<VertexConsumer> out) {
        throw new AssertionError();
    }

    @Override
    public VertexConsumer visual_snowy_leaves$beginIndex(final int index) {
        this.forEach(x -> x.visual_snowy_leaves$beginIndex(index));
        return this;
    }

    @Override
    public VertexConsumer visual_snowy_leaves$endIndex() {
        this.forEach(VertexConsumerExtension::visual_snowy_leaves$endIndex);
        return this;
    }
}
