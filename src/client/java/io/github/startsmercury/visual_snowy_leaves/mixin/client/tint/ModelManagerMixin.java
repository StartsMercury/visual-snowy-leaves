package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SpriteWhitenerAware;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
    @Shadow
    @Final
    private BlockColors blockColors;

    @Inject(method = "apply", at = @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelManager$ReloadState;atlasPreparations:Ljava/util/Map;"))
    private void modifySprites(
        final CallbackInfo callback,
        final @Local(ordinal = 0, argsOnly = true) ModelManager.ReloadState reloadState
    ) {
        final var modelBakery = reloadState.modelBakery();

        ((SpriteWhitenerAware) modelBakery).visual_snowy_leaves$getSpriteWhitener().modifySprites(
            this.blockColors,
            ((ModelBakeryAccessor) modelBakery).getModelResources(),
            reloadState.atlasPreparations().get(InventoryMenu.BLOCK_ATLAS)
        );
    }
}
