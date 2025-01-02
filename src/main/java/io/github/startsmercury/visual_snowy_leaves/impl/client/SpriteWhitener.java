package io.github.startsmercury.visual_snowy_leaves.impl.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.ColorComponent;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.BlockColorsAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.TextureAtlasSpriteAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class SpriteWhitener {
    private final Multimap<ResourceLocation, ResourceLocation> models;

    private final Set<? extends ResourceLocation> targetBlockKeys;

    public SpriteWhitener() {
        this.models = HashMultimap.create();
        this.targetBlockKeys = Set.copyOf(
            Minecraft.getInstance()
                .getVisualSnowyLeaves()
                .getConfig()
                .targetBlockKeys()
        );
    }

    public void analyzeModels(
        final ResourceLocation blockKey,
        final BlockModelDefinition blockModelDefinition
    ) {
        if (!this.targetBlockKeys.contains(blockKey)) {
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
            .forEach(model -> this.models.put(blockKey, model));
    }

    public void modifySprites(
        final BlockColors blockColors,
        final Map<? super ResourceLocation, ? extends UnbakedModel> modelResources,
        final Map<? super ResourceLocation, ? extends TextureAtlasSprite> sprites
    ) {
        final var blockColorsAccessor = (BlockColorsAccessor) blockColors;

        for (final var blockColor : blockColorsAccessor.getBlockColors()) {
            if (blockColor instanceof final SnowableBlockColor snowable) {
                final var id = blockColorsAccessor.getBlockColors().getId(blockColor);
                // It is possible for changes to persist without this:
                blockColors.register(snowable.blockColor(), Registry.BLOCK.byId(id));
            }
        }

        for (final var block : this.models.keySet()) {
            modifySpritesOf(blockColors, modelResources, sprites, block);
        }
    }

    private void modifySpritesOf(
        final BlockColors blockColors,
        final Map<? super ResourceLocation, ? extends UnbakedModel> modelResources,
        final Map<? super ResourceLocation, ? extends TextureAtlasSprite> sprites,
        final ResourceLocation block
    ) {
        final var contentsCollection = Set.copyOf(this.models.get(block))
            .stream()
            .map(modelResources::get)
            .filter(it -> it instanceof BlockModel)
            .map(it -> (BlockModel) it)
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
                .map(sprites::get)
                .map(it -> (TextureAtlasSpriteAccessor) it)
                .map(TextureAtlasSpriteAccessor::getMainImage)
            )
            .collect(Collectors.toSet());

        final var allArgbPixels = contentsCollection
            .stream()
            .map(it -> it[0])
            .flatMapToInt(nativeImage -> {
                return IntStream.range(0, nativeImage.getHeight()).flatMap(y -> {
                    return IntStream.range(0, nativeImage.getWidth())
                        .map(x -> nativeImage.getPixelRGBA(x, y));
                });
            })
            .toArray();

        final var _rgbMultiplier = 0xFF_00_00_00 | getArgbOfMaxLightness(allArgbPixels);

        contentsCollection
            .stream()
            .flatMap(Stream::of)
            .forEach(image -> {
                final int width = image.getWidth();
                final int height = image.getHeight();
                for (var y = 0; y < height; y++) {
                    for (var x = 0; x < width; x++) {
                        final var rgba = image.getPixelRGBA(x, y);
                        image.setPixelRGBA(x, y, normalize(rgba, _rgbMultiplier));
                    }
                }
            });

        final var id = Registry.BLOCK.getId(Registry.BLOCK.get(block));
        final var blockColor = ((BlockColorsAccessor) blockColors).getBlockColors().byId(id);
        if (blockColor == null) {
            return;
        }

        blockColors.register(
            SnowableBlockColor.setMultiplier(blockColor, _rgbMultiplier),
            Registry.BLOCK.get(block)
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
