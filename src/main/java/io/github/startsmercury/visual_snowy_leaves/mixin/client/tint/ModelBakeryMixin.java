package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import io.github.startsmercury.visual_snowy_leaves.impl.client.SpriteWhitener;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SpriteWhitenerAware;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin implements SpriteWhitenerAware {
    @Final
    @Shadow
    private Map<ResourceLocation, Pair<TextureAtlas, TextureAtlas.Preparations>> atlasPreparations;

    @Final
    @Shadow
    private BlockColors blockColors;

    @Unique
    private final SpriteWhitener spriteWhitener = new SpriteWhitener();

    @Final
    @Shadow
    private Map<ResourceLocation, UnbakedModel> unbakedCache;

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

    @Inject(
        method = """
            <init> (                                                   \
                Lnet/minecraft/server/packs/resources/ResourceManager; \
                Lnet/minecraft/client/color/block/BlockColors;         \
                Lnet/minecraft/util/profiling/ProfilerFiller;          \
                I                                                      \
            ) V                                                        \
        """,
        at = @At("RETURN")
    )
    private void modifySprites(CallbackInfo ci) {
        final var atlasPreparation = (TextureAtlas$PreparationsAccessor) this
            .atlasPreparations
            .get(InventoryMenu.BLOCK_ATLAS)
            .getSecond();
        final var sprites = atlasPreparation
            .getRegions()
            .stream()
            .collect(Collectors.toMap(TextureAtlasSprite::getName, Function.identity()));
        this.visual_snowy_leaves$getSpriteWhitener().modifySprites(
            this.blockColors,
            this.unbakedCache,
            sprites
        );
    }
}
