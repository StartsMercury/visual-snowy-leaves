package io.github.startsmercury.visual_snowy_leaves.mixin.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.startsmercury.visual_snowy_leaves.impl.client.ColorComponent;
import io.github.startsmercury.visual_snowy_leaves.impl.client.ModelBakeryExtension;
import io.github.startsmercury.visual_snowy_leaves.impl.client.MultipliedBlockColor;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VisualSnowyLeavesImpl;
import net.minecraft.client.color.block.BlockColor;
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
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin implements ModelBakeryExtension {
    @Final
    @Shadow
    private BlockColors blockColors;

    @Final
    @Shadow
    private Map<ResourceLocation, BlockModel> modelResources;

    @Unique
    private final Multimap<ResourceLocation, ResourceLocation> visual_snowy_leaves$models = HashMultimap.create();

    @Inject(
        method = "loadModel",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/collect/Maps;newIdentityHashMap()Ljava/util/IdentityHashMap;",
            remap = false
        )
    )
    private void captureModels(
        final CallbackInfo callback,
        final @Local(ordinal = 1) ResourceLocation resourceLocation2,
        final @Local(ordinal = 0) BlockModelDefinition blockModelDefinition
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
            .forEach(model -> this.visual_snowy_leaves$models.put(resourceLocation2, model));
    }

    @Override
    public void visual_snowy_leaves$modifySprites(final AtlasSet.StitchResult atlas) {
        final var blockColors = ((BlockColorsAccessor) this.blockColors).getBlockColors();

        for (final var block : this.visual_snowy_leaves$models.keySet()) {
            visual_snowy_leaves$modifySpritesOf(atlas, blockColors, block);
        }
    }

    @Unique
    private void visual_snowy_leaves$modifySpritesOf(
        final AtlasSet.StitchResult atlas,
        final IdMapper<BlockColor> blockColors,
        final ResourceLocation block
    ) {
        final var contentsCollection = Set.copyOf(this.visual_snowy_leaves$models.get(block))
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
        final var blockColor = blockColors.byId(id);
        if (blockColor == null) {
            return;
        }

        blockColors.addMapping(MultipliedBlockColor.setMultiplier(blockColor, _rgbMultiplier), id);
    }

    @Unique
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

    @Unique
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
