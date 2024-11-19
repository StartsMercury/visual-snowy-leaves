package io.github.startsmercury.visual_snowy_leaves.impl.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.ColorComponent;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.BlockColorsAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.SpriteContentsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class SpriteWhitener {
    private static final SpriteWhitener EMPTY = new SpriteWhitener(Multimaps.forMap(Map.of()), Set.of());

    public static SpriteWhitener createDefault() {
        return new SpriteWhitener(
            HashMultimap.create(),
            Set.copyOf(
                Minecraft.getInstance()
                    .getVisualSnowyLeaves()
                    .getConfig()
                    .targetBlockKeys()
            )
        );
    }

    public static SpriteWhitener getEmpty() {
        return SpriteWhitener.EMPTY;
    }

    private final Multimap<ResourceLocation, ResourceLocation> models;

    private final Set<? extends ResourceLocation> targetBlockKeys;

    private SpriteWhitener(
        final Multimap<ResourceLocation, ResourceLocation> models,
        final Set<? extends ResourceLocation> targetBlockKeys
    ) {
        this.models = models;
        this.targetBlockKeys = targetBlockKeys;
    }

    public void analyzeModels(
        final ResourceLocation blockKey,
        final BlockModelDefinition blockModelDefinition
    ) {
        if (!this.targetBlockKeys.contains(blockKey)) {
            return;
        }

        final var multiPart = blockModelDefinition.getMultiPart();
        final Stream<MultiVariant> multiPartVariants;
        if (multiPart != null) {
            multiPartVariants = multiPart.selectors().stream().map(Selector::getVariant);
        } else {
            multiPartVariants = Stream.empty();
        }

        final var variants = blockModelDefinition.getMultiVariants().stream();

        Stream.concat(multiPartVariants, variants)
            .flatMap((variant) -> variant.variants().stream())
            .map(Variant::getModelLocation)
            .forEach(model -> this.models.put(blockKey, model));
    }

    public void modifySprites(
        final BlockColors blockColors,
        final Map<ResourceLocation, UnbakedModel> modelResources,
        final AtlasSet.StitchResult atlas
    ) {
        final var blockColorsAccessor = (BlockColorsAccessor) blockColors;
        final var logger = Minecraft.getInstance().getVisualSnowyLeaves().getLogger();

        for (final var blockColor : blockColorsAccessor.getBlockColors()) {
            if (blockColor instanceof final SnowableBlockColor snowable) {
                final var id = blockColorsAccessor.getBlockColors().getId(blockColor);
                // It is possible for changes to persist without this:
                blockColors.register(snowable.blockColor(), BuiltInRegistries.BLOCK.byId(id));
            }
        }

        for (final var block : this.models.keySet()) {
            modifySpritesOf(blockColors, modelResources, atlas, logger, block);
        }
    }

    private void modifySpritesOf(
        final BlockColors blockColors,
        final Map<ResourceLocation, UnbakedModel> modelResources,
        final AtlasSet.StitchResult atlas,
        final Logger logger,
        final ResourceLocation blockKey
    ) {
        final var contentsCollection = Set.copyOf(this.models.get(blockKey))
            .stream()
            .map(modelResources::get)
            .map(SpriteWhitener::asBlockModelOrElseNull)
            .filter(Objects::nonNull)
            .flatMap(blockModel -> blockModel
                .getElements()
                .stream()
                .flatMap(element -> element.faces.values().stream())
                .filter(face -> face.tintIndex() == 0)
                .map(BlockElementFace::texture)
                .collect(Collectors.toSet())
                .stream()
                .map(blockModel::getMaterial)
                .map(Material::texture)
                .collect(Collectors.toSet())
                .stream()
                .map(atlas::getSprite)
                .filter(Objects::nonNull)
                .map(TextureAtlasSprite::contents)
            )
            .map(contents -> (SpriteContentsAccessor) contents)
            .collect(Collectors.toSet());

        final var allArgbPixels = contentsCollection
            .stream()
            .map(SpriteContentsAccessor::getOriginalImage)
            .map(NativeImage::getPixels)
            .flatMapToInt(IntStream::of)
            .toArray();

        final var _rgbMultiplier = 0xFF_00_00_00 | getArgbOfMaxLightness(allArgbPixels);

        contentsCollection
            .stream()
            .map(SpriteContentsAccessor::getByMipLevel)
            .flatMap(Stream::of)
            .forEach(
                image -> image.applyToAllPixels(rgba -> normalize(rgba, _rgbMultiplier))
            );

        final var optionalBlockHolder = BuiltInRegistries.BLOCK.get(blockKey);
        if (optionalBlockHolder.isEmpty()) {
            logger.warn("[{}] Registry holder of block {} is missing from the registry", VslConstants.NAME, blockKey);
            return;
        }

        final var blockHolder = optionalBlockHolder.get();
        if (!blockHolder.isBound()) {
            logger.warn("[{}] Registry holder of block {} is not yet bound", VslConstants.NAME, blockKey);
            return;
        }

        final var block = blockHolder.value();
        final var id = BuiltInRegistries.BLOCK.getId(block);
        final var blockColor = ((BlockColorsAccessor) blockColors).getBlockColors().byId(id);
        if (blockColor == null) {
            return;
        }

        blockColors.register(SnowableBlockColor.setMultiplier(blockColor, _rgbMultiplier), block);
    }

    private static BlockModel asBlockModelOrElseNull(final UnbakedModel unbakedModel) {
        return unbakedModel instanceof final BlockModel blockModel ? blockModel : null;
    }

    private static int getArgbOfMaxLightness(final int[] argbArray) {
        var maxLightness2x = -1;
        var minSaturation = 256;
        var result = 0xFF_FF_FF_FF;

        for (final var argb : argbArray) {
            final var a = argb >>> 24 & 0xFF;
            if (a == 0) {
                continue;
            }

            final var r = argb >>> 16 & 0xFF;
            final var g = argb >>>  8 & 0xFF;
            final var b = argb        & 0xFF;

            final var max = Math.max(Math.max(r, g), b);
            final var min = Math.min(Math.min(r, g), b);

            final int saturation;
            {
                @SuppressWarnings("UnnecessaryLocalVariable")
                final var value = max;
                final var chroma = max - min;
                saturation = value == 0 ? 0 : 255 * chroma / value;
            }
            if (saturation - 2 > minSaturation) {
                continue;
            }

            final var lightness2x = max + min;
            if (lightness2x <= maxLightness2x) {
                continue;
            }

            maxLightness2x = lightness2x;
            minSaturation = saturation;
            result = argb;
        }

        return result;
    }

    private int normalize(final int argb, final int _xyz) {
        final var a = ARGB.alpha(argb);
        final var r = ARGB.red(argb);
        final var g = ARGB.green(argb);
        final var b = ARGB.blue(argb);

        final var x = ARGB.red(_xyz);
        final var y = ARGB.green(_xyz);
        final var z = ARGB.blue(_xyz);

        final var sr = ColorComponent.div(r, x);
        final var sg = ColorComponent.div(g, y);
        final var sb = ColorComponent.div(b, z);

        return ARGB.color(a, sr, sg, sb);
    }
}
