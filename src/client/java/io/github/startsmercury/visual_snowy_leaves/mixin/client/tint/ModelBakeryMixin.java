package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.startsmercury.visual_snowy_leaves.impl.client.SpriteWhitener;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SpriteWhitenerAware;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelBakery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin implements SpriteWhitenerAware {
    @Unique
    private SpriteWhitener spriteWhitener = new SpriteWhitener();

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/model/BlockStateModelLoader;loadAllBlockStates()V"
        )
    )
    private void moveSpriteWhitener(
        final BlockStateModelLoader instance,
        final Operation<Void> original
    ) {
        this.spriteWhitener =
            ((SpriteWhitenerAware) instance).visual_snowy_leaves$getSpriteWhitener();
        original.call(instance);
    }

    @Override
    public SpriteWhitener visual_snowy_leaves$getSpriteWhitener() {
        return this.spriteWhitener;
    }
}
