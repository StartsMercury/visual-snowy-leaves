package io.github.startsmercury.visual_snowy_leaves.mixin.client.vertex;

import com.mojang.blaze3d.vertex.*;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslVertexFormatElement;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements VertexConsumer, VertexConsumerExtension {
    @Shadow
    @Final
    private boolean fastFormat;

    @Redirect(
        method = "addVertex(FFFIFFIIFFF)V",
        at = @At(
            value = "FIELD",
            target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;fastFormat:Z"
        )
    )
    private boolean fastFormat(BufferBuilder instance) {
        // Fast-format uses an all-in-one method instead of separate vertex builder methods.
        // This approach was inspired from IrisShaders but may overkill in the long run.
        // TODO: perhaps we can just tap into fast formatting as well...
        return false;
    }

    @Shadow
    @Final
    private VertexFormat format;

    @Override
    public boolean visual_snowy_leaves$alphaAsAo() {
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

    //    @Override
//    public void putBulkData(
//        final PoseStack.Pose pose,
//        final BakedQuad quad,
//        final float[] brightness,
//        final float r,
//        final float g,
//        final float b,
//        final float a,
//        final int[] lightmapCoord,
//        final int overlayCoords
//    ) {
//        final var alpha = a * brightness[0];
//        final var noShades = new float[brightness.length];
//        Arrays.fill(noShades, 1.0f);
//
//        VertexConsumer.super.putBulkData(pose, quad, noShades, r, g, b, alpha, lightmapCoord, overlayCoords);
//    }
//    @Shadow
//    private static void putRgba(final long pointer, final int argb) {
//        throw new AssertionError();
//    }
//

//
//    @Override
//    public VertexConsumer visual_snowy_leaves$setTint(final int r, final int g, final int b, final int a) {
//        long pointer = this.beginElement(VisualSnowyLeavesImpl.VSL_TINT);
//        if (pointer != -1L) {
////            MemoryUtil.memPutByte(pointer, (byte)r);
////            MemoryUtil.memPutByte(pointer + 1L, (byte)g);
////            MemoryUtil.memPutByte(pointer + 2L, (byte)b);
////            MemoryUtil.memPutByte(pointer + 3L, (byte)a);
////            MemoryUtil.memPutFloat(pointer, r / 255.0f);
////            MemoryUtil.memPutFloat(pointer + 4L, g / 255.0f);
////            MemoryUtil.memPutFloat(pointer + 8L, b / 255.0f);
//            MemoryUtil.memPutFloat(pointer, 1.0f);
//            MemoryUtil.memPutFloat(pointer + 4L, 1.0f);
//            MemoryUtil.memPutFloat(pointer + 8L, 1.0f);
////            MemoryUtil.memPutFloat(pointer + 3L, a);
//        }
//
//        return this;
//    }
//
////    @Override
////    public VertexConsumer visual_snowy_leaves$setTint(final int color) {
////        long pointer = this.beginElement(VisualSnowyLeavesImpl.VSL_TINT);
////        if (pointer != -1L) {
////            putRgba(pointer, color);
////        }
////
////        return this;
////    }
}
