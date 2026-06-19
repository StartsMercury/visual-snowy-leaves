package io.github.startsmercury.visual_snowy_leaves.mixin.client.fabric.indigo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.VertexConsumerExtension;
import io.github.startsmercury.visual_snowy_leaves.impl.client.fabric.indigo.AbstractRenderContextExtension;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractRenderContext.class)
public class AbstractRenderContextMixin implements AbstractRenderContextExtension {
    @Override
    public @Nullable BlockRenderInfo visual_snowy_leaves$blockInfo() {
        return null;
    }

    @WrapOperation(
        method = "bufferQuad(" +
            "Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;" +
            "Lcom/mojang/blaze3d/vertex/VertexConsumer;" +
        ")V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(FFFIFFIIFFF)V"
        )
    )
    private void distinguishSnowyableQuads(
        final VertexConsumer instance,
        final float f,
        final float g,
        final float h,
        final int i,
        final float j,
        final float k,
        final int l,
        final int m,
        final float n,
        final float o,
        final float p,
        final Operation<Void> original
    ) {
        final var vslConfig = Minecraft.getInstance().getVisualSnowyLeaves().getConfig();
        final var blockInfo = this.visual_snowy_leaves$blockInfo();
        if (blockInfo == null) {
            original.call(instance, f, g, h, i, j, k, l, m, n, o, p);
            return;
        }

        final var correctBiome = !vslConfig.requireSnowyBiomes()
            || blockInfo.blockView instanceof final SnowAware snowAware
            && snowAware.visual_snowy_leaves$coldEnoughToSnow(blockInfo.blockPos);
        if (correctBiome && vslConfig
            .targetBlockKeys()
            .contains(BuiltInRegistries.BLOCK.getKey(blockInfo.blockState.getBlock()))
        ) {
            instance.visual_snowy_leaves$beginIndex(VertexConsumerExtension.SNOW_MARKED);
            original.call(instance, f, g, h, i, j, k, l, m, n, o, p);
            instance.visual_snowy_leaves$endIndex();
        } else {
            original.call(instance, f, g, h, i, j, k, l, m, n, o, p);
        }
    }
}
