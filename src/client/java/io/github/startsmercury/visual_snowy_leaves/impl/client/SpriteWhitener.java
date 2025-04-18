package io.github.startsmercury.visual_snowy_leaves.impl.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.ColorComponent;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.BlockColorsAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.BlockModelInvoker;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.SpriteContentsAccessor;
import it.unimi.dsi.fastutil.ints.AbstractInt2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.Selector;
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
        final var layers = Set.copyOf(this.models.get(blockKey))
            .stream()
            .map(modelResources::get)
            .map(SpriteWhitener::asBlockModelOrElseNull)
            .filter(Objects::nonNull)
            .map(blockModel -> {
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
                    .collect(Collectors.groupingBy(
                        BlockElementFace::tintIndex,
                        Int2ReferenceOpenHashMap::new,
                        Collectors.mapping(
                            face -> {
                                final var material =
                                    resolveSlotContent(textureSlots, textureSlots.get(face.texture().substring(1)));
                                if (material == null) {
                                    return null;
                                }

                                final var sprite = atlas.getSprite(material.texture());
                                if (sprite == null) {
                                    return null;
                                }

                                return (SpriteContentsAccessor) sprite.contents();
                            },
                            Collectors.filtering(Objects::nonNull, Collectors.toCollection(ReferenceOpenHashSet::new))
                        )
                    ));
            })
            .map(Int2ReferenceMap::int2ReferenceEntrySet)
            .flatMap(Set::stream)
            .collect(Collectors.groupingBy(
                Int2ReferenceMap.Entry::getIntKey,
                Int2ReferenceOpenHashMap::new,
                Collectors.flatMapping(
                    entry -> entry.getValue().stream(),
                    Collectors.toCollection(ReferenceOpenHashSet::new)
                )
            ));

        final var multipliers = layers.int2ReferenceEntrySet().stream().map(entry -> {
            final var index = entry.getIntKey();
            final var contentsCollection = entry.getValue();

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

            return new AbstractInt2IntMap.BasicEntry(index, _rgbMultiplier);
        }).collect(Collectors.toMap(Int2IntMap.Entry::getIntKey, Map.Entry::getValue, (x, y) -> x, Int2IntOpenHashMap::new));
        multipliers.defaultReturnValue(0xFFFFFFFF);

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
        final var blockColor = Objects.requireNonNullElse(
            ((BlockColorsAccessor) blockColors).getBlockColors().byId(id),
            ConstantBlockColor.WHITE
        );
        blockColors.register(SnowableBlockColor.setMultiplier(blockColor, multipliers), block);
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
        var best = 0;
        var result = 0xFFFFFFFF;

        for (final var argb : argbArray) {
            final var a = ARGB.alpha(argb);
            final var r = ARGB.red(argb);
            final var g = ARGB.green(argb);
            final var b = ARGB.blue(argb);

            final var key = a * a + r * r + g * g + b * b;

            if (key > best) {
                best = key;
                result = argb;
            }
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
