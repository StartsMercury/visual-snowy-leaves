package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkVertexConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ChunkVertexConsumer.class, remap = false)
public abstract class ChunkVertexConsumerMixin implements VertexConsumer, VertexConsumerExtension {
    @Final
    @Shadow
    private ChunkModelBuilder modelBuilder;

    @Override
    public boolean visual_snowy_leaves$isExtended () {
        return ((VertexConsumerExtension) this.modelBuilder).visual_snowy_leaves$isExtended();
    }

    @Override
    public VertexConsumer visual_snowy_leaves$beginIndex(final int index) {
        return ((VertexConsumerExtension) this.modelBuilder).visual_snowy_leaves$beginIndex(index);
    }

    @Override
    public VertexConsumer visual_snowy_leaves$endIndex() {
        return ((VertexConsumerExtension) this.modelBuilder).visual_snowy_leaves$endIndex();
    }
}
