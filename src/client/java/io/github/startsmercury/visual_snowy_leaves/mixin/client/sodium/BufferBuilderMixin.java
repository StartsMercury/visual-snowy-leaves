//TODO Re-eneble when Sodium is available in non-obfuscated
//package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;
//
//import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
//import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//import com.mojang.blaze3d.vertex.BufferBuilder;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import com.mojang.blaze3d.vertex.VertexFormat;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.ColorVertexExtension;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.EntityVertexExtension;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.GlyphVertexExtension;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.LineVertexExtension;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.ParticleVertexExtension;
//import net.caffeinemc.mods.sodium.api.vertex.format.common.ColorVertex;
//import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
//import net.caffeinemc.mods.sodium.api.vertex.format.common.GlyphVertex;
//import net.caffeinemc.mods.sodium.api.vertex.format.common.LineVertex;
//import net.caffeinemc.mods.sodium.api.vertex.format.common.ParticleVertex;
//import org.lwjgl.system.MemoryStack;
//import org.spongepowered.asm.mixin.Dynamic;
//import org.spongepowered.asm.mixin.Mixin;
//
//@Mixin(BufferBuilder.class)
//public abstract class BufferBuilderMixin implements VertexConsumer {
//    @Dynamic("net.caffeinemc.mods.sodium.mixin.core.render.immediate.consumer.BufferBuilderMixin")
//    @WrapMethod(
//        method = "push(Lorg/lwjgl/system/MemoryStack;JILcom/mojang/blaze3d/vertex/VertexFormat;)V",
//        require = 0
//    )
//    private void replaceVertexFormat(
//        final MemoryStack stack,
//        final long src,
//        final int count,
//        VertexFormat format,
//        final Operation<Void> original
//    ) {
//        if (this.visual_snowy_leaves$mainVertexFormat()) {
//            if (format == ColorVertex.FORMAT) {
//                format = ColorVertexExtension.FORMAT;
//            } else if (format == EntityVertex.FORMAT) {
//                format = EntityVertexExtension.FORMAT;
//            } else if (format == GlyphVertex.FORMAT) {
//                format = GlyphVertexExtension.FORMAT;
//            } else if (format == LineVertex.FORMAT) {
//                format = LineVertexExtension.FORMAT;
//            } else if (format == ParticleVertex.FORMAT) {
//                format = ParticleVertexExtension.FORMAT;
//            }
//        }
//        original.call(stack, src, count, format);
//    }
//}
