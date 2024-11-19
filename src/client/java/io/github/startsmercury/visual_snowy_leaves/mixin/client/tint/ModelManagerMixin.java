package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.SpriteWhitener;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SpriteWhitenerAware;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
    @Shadow
    @Final
    private BlockColors blockColors;

    @Unique
    private SpriteWhitener spriteWhitener = SpriteWhitener.getEmpty();

    @Inject(method = "reload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelManager;loadBlockModels(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private void referenceSpriteWhitener(
        final CallbackInfoReturnable<CompletableFuture<Void>> callback,
        final @Local(ordinal = 0) BlockStateModelLoader blockStateModelLoader
    ) {
        this.spriteWhitener =
            ((SpriteWhitenerAware) blockStateModelLoader).visual_snowy_leaves$getSpriteWhitener();
    }

    @Inject(method = "apply", at = @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelManager$ReloadState;atlasPreparations:Ljava/util/Map;"))
    private void modifySprites(
        final CallbackInfo callback,
        final @Local(ordinal = 0, argsOnly = true) ModelManager.ReloadState reloadState
    ) {
        final var modelBakery = reloadState.modelBakery();

        this.spriteWhitener.modifySprites(
            this.blockColors,
            ((ModelBakeryAccessor) modelBakery).getUnbakedModels(),
            reloadState.atlasPreparations().get(InventoryMenu.BLOCK_ATLAS)
        );
    }
}
