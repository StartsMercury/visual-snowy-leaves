package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;

import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VertexBufferWriter.class)
public interface VertexBufferWriterMixin extends VertexConsumerExtension {
    // Make sure all implementations implement our interface
    // TODO create tests/canary @FunctionalInterface(s) to detect
    //      methods that weren't default-ed in VertexConsumerExtension
}
