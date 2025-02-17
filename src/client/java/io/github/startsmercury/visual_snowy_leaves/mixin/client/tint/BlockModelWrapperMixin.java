package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.startsmercury.visual_snowy_leaves.impl.client.SnowableBlockColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelWrapper.class)
public abstract class BlockModelWrapperMixin {
    @Unique
    private final IdMapper<BlockColor> blockColors = ((BlockColorsAccessor) Minecraft.getInstance().getBlockColors()).getBlockColors();

    @ModifyExpressionValue(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;prepareTintLayers(I)[I"))
    private int[] captureTintLayers(
        final int[] original,
        final @Share("tintLayers") LocalRef<int[]> tintLayersRef
    ) {
        tintLayersRef.set(original);
        return original;
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void modifyTintLayers(
        final CallbackInfo ci,
        final @Local(ordinal = 0, argsOnly = true) ItemStack itemStack,
        final @Local(ordinal = 0, argsOnly = true) @Nullable ClientLevel clientLevel,
        final @Local(ordinal = 0, argsOnly = true) @Nullable LivingEntity livingEntity,
        final @Share("tintLayers") LocalRef<int[]> tintLayersRef
    ) {
        final var config = Minecraft.getInstance().getVisualSnowyLeaves().getConfig();

        if (!(itemStack.getItem() instanceof final BlockItem blockItem)) {
            return;
        }

        final var block = blockItem.getBlock();

        if (!config.targetBlockKeys().contains(BuiltInRegistries.BLOCK.getKey(block))) {
            return;
        }

        final var blockColor = this.blockColors.byId(BuiltInRegistries.BLOCK.getId(block));

        if (!(blockColor instanceof final SnowableBlockColor snowableBlockColor)) {
            return;
        }

        final var tintLayers = tintLayersRef.get();

        tintLayers[0] = ARGB.multiply(tintLayers[0], snowableBlockColor.correctionMultiplier());
    }
}
