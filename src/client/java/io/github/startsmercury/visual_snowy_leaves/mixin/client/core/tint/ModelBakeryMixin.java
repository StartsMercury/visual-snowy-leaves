package io.github.startsmercury.visual_snowy_leaves.mixin.client.core.tint;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.SpriteWhitener;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SpriteWhitenerAware;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin implements SpriteWhitenerAware {
    @Unique
    private @NotNull SpriteWhitener spriteWhitener;

    @Inject(
        method = """
            <init>(                                            \
                Lnet/minecraft/client/color/block/BlockColors; \
                Lnet/minecraft/util/profiling/ProfilerFiller;  \
                Ljava/util/Map;                                \
                Ljava/util/Map;                                \
            )V                                                 \
        """,
        at = @At(
            value = "FIELD",
            target = """
                Lnet/minecraft/client/resources/model/ModelBakery; \
                modelResources: Ljava/util/Map;                    \
            """,
            opcode = Opcodes.PUTFIELD
        )
    )
    private void onInit(
        final CallbackInfo callback,
        final @Local(ordinal = 0, argsOnly = true) BlockColors blockColors,
        final @Local(ordinal = 0, argsOnly = true) Map<ResourceLocation, BlockModel> modelResources
    ) {
        this.spriteWhitener = new SpriteWhitener(blockColors, modelResources);
    }

    @Override
    public SpriteWhitener getSpriteWhitener() {
        return this.spriteWhitener;
    }

    @Inject(
        method = "loadModel",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/collect/Maps;newIdentityHashMap()Ljava/util/IdentityHashMap;",
            remap = false
        )
    )
    private void analyzeModels(
        final CallbackInfo callback,
        final @Local(ordinal = 1) ResourceLocation resourceLocation2,
        final @Local(ordinal = 0) BlockModelDefinition blockModelDefinition
    ) {
        this.spriteWhitener.analyzeModels(resourceLocation2, blockModelDefinition);
    }
}
