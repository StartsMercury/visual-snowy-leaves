package io.github.startsmercury.visual_snowy_leaves.impl.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.ColorComponent;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.BlockColorsAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.BlockModelInvoker;
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
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class SpriteWhitener {
    private static final SpriteWhitener EMPTY = new SpriteWhitener(
        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME),
        Multimaps.forMap(Map.of()),
        Set.of()
    );

    public static SpriteWhitener createDefault() {
        return create(Minecraft.getInstance().getVisualSnowyLeaves());
    }

    public static SpriteWhitener create(final VisualSnowyLeavesImpl visualSnowyLeaves) {
        return new SpriteWhitener(
            visualSnowyLeaves.getLogger(),
            HashMultimap.create(),
            Set.copyOf(visualSnowyLeaves.getConfig().targetBlockKeys())
        );
    }

    public static SpriteWhitener getEmpty() {
        return SpriteWhitener.EMPTY;
    }

    private final Logger logger;

    private final Multimap<ResourceLocation, ResourceLocation> models;

    private final Set<? extends ResourceLocation> targetBlockKeys;

    private SpriteWhitener(
        final Logger logger,
        final Multimap<ResourceLocation, ResourceLocation> models,
        final Set<? extends ResourceLocation> targetBlockKeys
    ) {
        this.logger = logger;
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
            .map(Variant::modelLocation)
            .forEach(model -> this.models.put(blockKey, model));
    }

    public void modifySprites(
        final BlockColors blockColors,
        final Map<ResourceLocation, UnbakedModel> modelResources,
        final AtlasSet.StitchResult atlas
    ) {
        final var blockColorsAccessor = (BlockColorsAccessor) blockColors;

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
            .flatMap(blockModel -> {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                final var models = (List<BlockModel>) (List) Stream.iterate(
                    blockModel,
                    it -> it instanceof BlockModel,
                    UnbakedModel::getParent
                ).toList();

                final var textureSlots = new HashMap<String, TextureSlots.SlotContents>();
                for (final var model : models.reversed()) {
                    textureSlots.putAll(model.getTextureSlots().values());
                }

                return models
                    .stream()
                    .flatMap(it -> ((BlockModelInvoker) it).callGetElements().stream())
                    .flatMap(element -> element.faces.values().stream())
                    .filter(face -> face.tintIndex() == 0)
                    .map(BlockElementFace::texture)
                    .map(texture -> texture.substring(1))
                    .collect(Collectors.toSet())
                    .stream()
                    .map(textureSlots::get)
                    .map(slotContents -> resolveSlotContent(textureSlots, slotContents))
                    .filter(Objects::nonNull)
                    .map(Material::texture)
                    .collect(Collectors.toSet())
                    .stream()
                    .map(atlas::getSprite)
                    .filter(Objects::nonNull)
                    .map(TextureAtlasSprite::contents);
            })
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

    private static Material resolveSlotContent(
        final Map<? super String, ? extends TextureSlots.SlotContents> textureSlots,
        TextureSlots.SlotContents slotContents
    ) {
        while (true) {
            switch (slotContents) {
                case null:
                    return null;
                case TextureSlots.Value(final var material):
                    return material;
                case TextureSlots.Reference(final var target):
                    slotContents = textureSlots.get(target);
                    break;
            }
        }
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
