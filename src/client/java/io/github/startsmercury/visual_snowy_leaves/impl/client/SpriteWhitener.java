package io.github.startsmercury.visual_snowy_leaves.impl.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.ColorComponent;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.core.tint.BlockColorsAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.core.tint.SpriteContentsAccessor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class SpriteWhitener {
    private final BlockColors blockColors;

    private final Map<? super ResourceLocation, ? extends BlockModel> modelResources;

    private final Multimap<ResourceLocation, ResourceLocation> models;

    public SpriteWhitener(
        final BlockColors blockColors,
        final Map<? super ResourceLocation, ? extends BlockModel> modelResources
    ) {
        Objects.requireNonNull(blockColors, "Parameter blockColors is null");
        Objects.requireNonNull(modelResources, "Parameter modelResources is null");

        this.blockColors = blockColors;
        this.modelResources = modelResources;
        this.models = HashMultimap.create();
    }

    public void analyzeModels(
        final ResourceLocation resourceLocation2,
        final BlockModelDefinition blockModelDefinition
    ) {
        if (!VisualSnowyLeavesImpl.TARGET_BLOCKS.contains(resourceLocation2)) {
            return;
        }

        final Stream<MultiVariant> multiPartVariants;
        if (blockModelDefinition.isMultiPart()) {
            multiPartVariants = blockModelDefinition
                .getMultiPart()
                .getSelectors()
                .stream()
                .map(Selector::getVariant);
        } else {
            multiPartVariants = Stream.empty();
        }

        final var variants = blockModelDefinition
            .getVariants()
            .values()
            .stream();

        Stream.concat(multiPartVariants, variants)
            .flatMap((variant) -> variant.getVariants().stream())
            .map(Variant::getModelLocation)
            .forEach(model -> this.models.put(resourceLocation2, model));
    }

    public void modifySprites(final AtlasSet.StitchResult atlas) {
        for (final var block : this.models.keySet()) {
            modifySpritesOf(atlas, block);
        }
    }

    private void modifySpritesOf(
        final AtlasSet.StitchResult atlas,
        final ResourceLocation block
    ) {
        final var contentsCollection = Set.copyOf(this.models.get(block))
            .stream()
            .map(ModelBakery.MODEL_LISTER::idToFile)
            .map(this.modelResources::get)
            .flatMap(blockModel -> blockModel
                .getElements()
                .stream()
                .flatMap(element -> element.faces.values().stream())
                .filter(face -> face.tintIndex == 0)
                .map(face -> face.texture)
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
            .map(NativeImage::getPixelsRGBA)
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

        final var id = BuiltInRegistries.BLOCK.getId(BuiltInRegistries.BLOCK.get(block));
        final var blockColor = ((BlockColorsAccessor) this.blockColors).getBlockColors().byId(id);
        if (blockColor == null) {
            return;
        }

        this.blockColors.register(
            SnowableBlockColor.setMultiplier(blockColor, _rgbMultiplier),
            BuiltInRegistries.BLOCK.get(block)
        );
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
        final var a = FastColor.ARGB32.alpha(argb);
        final var r = FastColor.ARGB32.red(argb);
        final var g = FastColor.ARGB32.green(argb);
        final var b = FastColor.ARGB32.blue(argb);

        final var x = FastColor.ARGB32.red(_xyz);
        final var y = FastColor.ARGB32.green(_xyz);
        final var z = FastColor.ARGB32.blue(_xyz);

        final var sr = ColorComponent.div(r, x);
        final var sg = ColorComponent.div(g, y);
        final var sb = ColorComponent.div(b, z);

        return FastColor.ARGB32.color(a, sr, sg, sb);
    }
}
