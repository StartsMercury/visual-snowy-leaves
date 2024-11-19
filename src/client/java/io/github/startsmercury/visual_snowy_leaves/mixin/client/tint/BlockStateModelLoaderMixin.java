package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.SpriteWhitener;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SpriteWhitenerAware;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockStateModelLoader.class)
public abstract class BlockStateModelLoaderMixin implements SpriteWhitenerAware {
    @Unique
    private final SpriteWhitener spriteWhitener = SpriteWhitener.createDefault();

    @Override
    public SpriteWhitener visual_snowy_leaves$getSpriteWhitener() {
        return this.spriteWhitener;
    }

    @Inject(
        method = "loadBlockStateDefinitionStack",
        at = @At(
            value = "FIELD",
            target = """
                Lnet/minecraft/client/resources/model/BlockStateModelLoader$LoadedBlockModelDefinition; \
                contents : Lnet/minecraft/client/renderer/block/model/BlockModelDefinition;             \
            """
        )
    )
    private void moveSpriteWhitener(
        final CallbackInfoReturnable<BlockStateModelLoader.LoadedModels> callback,
        final @Local(ordinal = 0, argsOnly = true) ResourceLocation resourceLocation,
        final @Local(ordinal = 0) BlockStateModelLoader.LoadedBlockModelDefinition loadedBlockModelDefinition
    ) {
        this.spriteWhitener.analyzeModels(resourceLocation, loadedBlockModelDefinition.contents());
    }
}
