package io.github.startsmercury.visual_snowy_leaves.impl.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryStack;

/**
 * Used for shader data that applies to all chunks, such as snow transition.
 */
public class VslGlobalsUniform implements AutoCloseable {
    private static final int UBO_SIZE = new Std140SizeCalculator().putFloat().get();

    private final GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
        () -> "Visual Snowy Leaves Globals UBO",
        GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM,
        UBO_SIZE
    );

    public void update(final float snowProgress) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = Std140Builder.onStack(stack, UBO_SIZE)
                .putFloat(snowProgress)
                .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.buffer.slice(), data);
        }
        VisualSnowyLeavesImpl.setSnowProgress(this.buffer);
    }

    @Override
    public void close() {
        this.buffer.close();
    }
}
