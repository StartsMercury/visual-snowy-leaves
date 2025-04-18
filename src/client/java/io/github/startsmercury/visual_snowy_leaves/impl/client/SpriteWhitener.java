package io.github.startsmercury.visual_snowy_leaves.impl.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.ColorComponent;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.BlockColorsAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.IdMapperAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.ItemColorsAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.MinecraftAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.SpriteContentsAccessor;
import it.unimi.dsi.fastutil.ints.AbstractInt2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class SpriteWhitener {
    public record Context(
        BlockColors blockColors,
        BlockItemColor blockItemColor,
        ItemColors itemColors
    ) {
        public Context(final Minecraft minecraft) {
            this(minecraft.getBlockColors(), ((MinecraftAccessor) minecraft).getItemColors());
        }

        public Context(final BlockColors blockColors, final ItemColors itemColors) {
            this(blockColors, new BlockItemColor(blockColors), itemColors);
        }
    }

    private static final SpriteWhitener EMPTY = new SpriteWhitener(
        new Context(new BlockColors(), new ItemColors()),
        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME),
        Multimaps.forMap(Map.of()),
        Set.of()
    );

    public static SpriteWhitener createDefault() {
        return create(Minecraft.getInstance().getVisualSnowyLeaves());
    }

    public static SpriteWhitener create(final VisualSnowyLeavesImpl visualSnowyLeaves) {
        return new SpriteWhitener(
            visualSnowyLeaves.getSpriteWhitenerContext(),
            visualSnowyLeaves.getLogger(),
            HashMultimap.create(),
            Set.copyOf(visualSnowyLeaves.getConfig().targetBlockKeys())
        );
    }

    public static SpriteWhitener getEmpty() {
        return SpriteWhitener.EMPTY;
    }

    private final Context context;

    private final Logger logger;

    private final Multimap<ResourceLocation, ResourceLocation> models;

    private final Set<? extends ResourceLocation> targetBlockKeys;

    private SpriteWhitener(
        final Context context,
        final Logger logger,
        final Multimap<ResourceLocation, ResourceLocation> models,
        final Set<? extends ResourceLocation> targetBlockKeys
    ) {
        this.context = context;
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
            .map(Variant::getModelLocation)
            .forEach(model -> this.models.put(blockKey, model));
    }

    public void modifySprites(
        final Map<ResourceLocation, UnbakedModel> modelResources,
        final AtlasSet.StitchResult atlas
    ) {
        final var blockColors = this.context.blockColors;
        final var itemColors = this.context.itemColors;
        final var blockColorsAccessor = (BlockColorsAccessor) blockColors;
        final var itemColorsAccessor = (ItemColorsAccessor) itemColors;

        for (final var blockColor : blockColorsAccessor.getBlockColors()) {
            if (blockColor instanceof final SnowableBlockColor snowable) {
                final var id = blockColorsAccessor.getBlockColors().getId(blockColor);
                // It is possible for changes to persist without this:
                blockColors.register(snowable.blockColor(), BuiltInRegistries.BLOCK.byId(id));
            }
        }

        final var itemColorsItemColors = itemColorsAccessor.getItemColors();
        @SuppressWarnings("unchecked")
        final var itemColorIdMapper = (IdMapperAccessor<ItemColor>) itemColorsItemColors;
        final var idToItemColor = itemColorIdMapper.getIdToT();
        final var itemColorToId = itemColorIdMapper.getTToId();

        for (final var iterator = idToItemColor.listIterator(); iterator.hasNext(); ) {
            final var itemColor = iterator.next();
            if (itemColor instanceof final BlockItemColor ignored) {
                iterator.set(null);
                itemColorToId.removeInt(itemColor);
            }
        }

        for (final var block : this.models.keySet()) {
            modifySpritesOf(modelResources, atlas, logger, block);
        }

        var n = idToItemColor.size();
        final var iterator = idToItemColor.listIterator(n);
        if (n > 512) {
            n -= 512;
        }
        while (n > 0 && iterator.hasPrevious() && iterator.previous() == null) {
            iterator.remove();
            n--;
        }
        itemColorIdMapper.setNextId(n);
    }

    private void modifySpritesOf(
        final Map<ResourceLocation, UnbakedModel> modelResources,
        final AtlasSet.StitchResult atlas,
        final Logger logger,
        final ResourceLocation blockKey
    ) {
        final var blockColors = this.context.blockColors;
        final var blockItemColor = this.context.blockItemColor;
        final var itemColors = this.context.itemColors;

        final var layers = Set.copyOf(this.models.get(blockKey))
            .stream()
            .map(modelResources::get)
            .map(SpriteWhitener::asBlockModelOrElseNull)
            .filter(Objects::nonNull)
            .map(blockModel -> blockModel.getElements()
                .stream()
                .flatMap(element -> element.faces.values().stream())
                .collect(Collectors.groupingBy(
                    BlockElementFace::tintIndex,
                    Int2ReferenceOpenHashMap::new,
                    Collectors.mapping(
                        face -> {
                            final var material = blockModel.getMaterial(face.texture());

                            final var sprite = atlas.getSprite(material.texture());
                            if (sprite == null) {
                                return null;
                            }

                            return (SpriteContentsAccessor) sprite.contents();
                        },
                        Collectors.filtering(Objects::nonNull, Collectors.toCollection(ReferenceOpenHashSet::new))
                    )
                ))
            )
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

        if (((ItemColorsAccessor) itemColors).getItemColors().byId(id) == null) {
            itemColors.register(blockItemColor, block);
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
