package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.startsmercury.visual_snowy_leaves.impl.client.SnowableBlockColor;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelWrapper.class)
public abstract class BlockModelWrapperMixin {
    @Final
    @Mutable
    @Shadow
    private List<ItemTintSource> tints;

    @Unique
    private final IdMapper<BlockColor> blockColors = ((BlockColorsAccessor) Minecraft.getInstance().getBlockColors()).getBlockColors();

    @Inject(
        method = "update",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/item/BlockModelWrapper;tints:Ljava/util/List;",
            ordinal = 0,
            opcode = Opcodes.GETFIELD
        )
    )
    private void captureTintLayers(
        final CallbackInfo callback,
        final @Local(ordinal = 0, argsOnly = true) ItemStack itemStack,
        final @Share("snowableBlockColor") LocalRef<SnowableBlockColor> snowableBlockColorRef
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

        if ((!(blockColor instanceof final SnowableBlockColor snowableBlockColor))) {
            return;
        }

        snowableBlockColorRef.set(snowableBlockColor);

        if (this.tints.isEmpty()) {
            this.tints = List.of(new Constant(CommonColors.WHITE));
        }
    }

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
        final @Share("tintLayers") LocalRef<int[]> tintLayersRef,
        final @Share("snowableBlockColor") LocalRef<SnowableBlockColor> snowableBlockColorRef
    ) {
        final var snowableBlockColor = snowableBlockColorRef.get();
        if (snowableBlockColor == null) {
            return;
        }

        final var tintLayers = tintLayersRef.get();
        final var multipliers = snowableBlockColor.correctionMultipliers();

        for (var i = 0; i < tintLayers.length; i++) {
            tintLayers[i] = ARGB.multiply(tintLayers[0], multipliers.get(i));
        }
    }
}
