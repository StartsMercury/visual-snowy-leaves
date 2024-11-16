package io.github.startsmercury.visual_snowy_leaves.mixin.client.core.tint;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.SpriteWhitener;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SpriteWhitenerAware;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin implements SpriteWhitenerAware {
    @Unique
    private final SpriteWhitener spriteWhitener = new SpriteWhitener();

    @Override
    public SpriteWhitener visual_snowy_leaves$getSpriteWhitener() {
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
