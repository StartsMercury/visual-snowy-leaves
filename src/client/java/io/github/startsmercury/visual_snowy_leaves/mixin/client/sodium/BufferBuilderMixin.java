package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.SodiumVertexFormatExtender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(
    value = {
        BufferBuilder.class,
        SheetedDecalTextureGenerator.class,
        SpriteCoordinateExpander.class
    },
    targets = { "net/minecraft/client/renderer/OutlineBufferSource$EntityOutlineGenerator" }
)
public abstract class BufferBuilderMixin implements VertexConsumer {
    @Unique
    private final SodiumVertexFormatExtender extender =
        FabricLoader.getInstance().isModLoaded("sodium")
            ? SodiumVertexFormatExtender.ENABLED
            : SodiumVertexFormatExtender.DISABLED;

    @Dynamic("net.caffeinemc.mods.sodium.mixin.core.render.immediate.consumer.BufferBuilderMixin")
    @WrapMethod(
        method = "push(Lorg/lwjgl/system/MemoryStack;JILcom/mojang/blaze3d/vertex/VertexFormat;)V",
        require = 0
    )
    private void replaceVertexFormat(
        final MemoryStack stack,
        final long src,
        final int count,
        final VertexFormat format,
        final Operation<Void> original
    ) {
        original.call(
            stack,
            src,
            count,
            this.visual_snowy_leaves$mainVertexFormat()
                ? this.extender.toExtension(format)
                : format
        );
    }
}
