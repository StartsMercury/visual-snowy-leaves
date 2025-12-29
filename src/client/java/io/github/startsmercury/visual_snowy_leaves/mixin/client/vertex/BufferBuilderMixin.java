package io.github.startsmercury.visual_snowy_leaves.mixin.client.vertex;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslVertexFormatElement;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements VertexConsumer, VertexConsumerExtension {
    @Shadow
    @Final
    private VertexFormat format;

    @ModifyExpressionValue(
        method = "addVertex(FFFIFFIIFFF)V",
        at = @At(
            value = "FIELD",
            target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;fastFormat:Z"
        )
    )
    private boolean fastFormat(final boolean original) {
        // Fast-format uses an all-in-one method instead of separate vertex builder methods.
        // This approach was inspired from IrisShaders but may overkill in the long run.
        return original && this.format != DefaultVertexFormat.BLOCK;
    }

    @Override
    public boolean visual_snowy_leaves$mainVertexFormat() {
        return this.format == DefaultVertexFormat.BLOCK;
    }

    @Override
    public boolean visual_snowy_leaves$alphaAsBrightness() {
        return this.format == DefaultVertexFormat.BLOCK;
    }

    @Unique
    private int vslIndex = 0;

    @Override
    public VertexConsumer visual_snowy_leaves$beginIndex(final int index) {
        this.vslIndex = index;
        return this;
    }

    @Override
    public VertexConsumer visual_snowy_leaves$endIndex() {
        this.vslIndex = 0;
        return this;
    }

    @Shadow
    private long beginElement(final VertexFormatElement element) {
        throw new AssertionError();
    }

    @Inject(method = "addVertex(FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;", at = @At("RETURN"))
    private void addIndex(final CallbackInfoReturnable<VertexConsumer> callback) {
        final var pointer = this.beginElement(VslVertexFormatElement.VslIndex.value());
        if (pointer != -1) {
            MemoryUtil.memPutInt(pointer, this.vslIndex);
        }
    }
}
