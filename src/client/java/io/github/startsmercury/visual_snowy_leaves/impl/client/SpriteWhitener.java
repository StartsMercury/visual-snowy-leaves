package io.github.startsmercury.visual_snowy_leaves.impl.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.ColorComponent;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.Reporter;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.BlockColorsAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.tint.SpriteContentsAccessor;
import it.unimi.dsi.fastutil.ints.AbstractInt2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.WeightedVariants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.random.Weighted;
import org.apache.commons.io.function.IOSupplier;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class SpriteWhitener {
    private static final SpriteWhitener EMPTY;

    static {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final var recRep = new Reporter(Set.of());

        @SuppressWarnings("unchecked")
        final var empty = new SpriteWhitener(
            LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME),
            Multimaps.forMap(Map.of()),
            Set.of(),
            recRep,
            recRep
        );

        EMPTY = empty;
    }

    public static SpriteWhitener createDefault() {
        return create(Minecraft.getInstance().getVisualSnowyLeaves());
    }

    public static SpriteWhitener create(final VisualSnowyLeavesImpl visualSnowyLeaves) {
        return new SpriteWhitener(
            visualSnowyLeaves.getLogger(),
            HashMultimap.create(),
            Set.copyOf(visualSnowyLeaves.getConfig().targetBlockKeys()),
            visualSnowyLeaves.getBlockStateModelRecRep(),
            visualSnowyLeaves.getGeometryRecRep()
        );
    }

    public static SpriteWhitener getEmpty() {
        return SpriteWhitener.EMPTY;
    }

    private final Logger logger;

    private final Multimap<ResourceLocation, ResourceLocation> models;

    private final Set<? extends ResourceLocation> targetBlockKeys;

    private final Reporter<Class<? extends BlockStateModel.Unbaked>> blockStateModelReporter;

    private final Reporter<Class<? extends UnbakedGeometry>> geometryReporter;

    private SpriteWhitener(
        final Logger logger,
        final Multimap<ResourceLocation, ResourceLocation> models,
        final Set<? extends ResourceLocation> targetBlockKeys,
        final Reporter<Class<? extends BlockStateModel.Unbaked>> blockStateModelReporter,
        final Reporter<Class<? extends UnbakedGeometry>> geometryReporter
    ) {
        this.logger = logger;
        this.models = models;
        this.targetBlockKeys = targetBlockKeys;
        this.blockStateModelReporter = blockStateModelReporter;
        this.geometryReporter = geometryReporter;
    }

    public void analyzeModels(
        final ResourceLocation blockKey,
        final BlockModelDefinition blockModelDefinition
    ) {
        if (!this.targetBlockKeys.contains(blockKey)) {
            return;
        }

        final var multiPartVariants = blockModelDefinition
            .multiPart()
            .stream()
            .flatMap(it -> it.selectors().stream().map(Selector::variant));

        final var variants = blockModelDefinition
            .simpleModels()
            .stream()
            .map(BlockModelDefinition.SimpleModelSelectors::models)
            .map(Map::values)
            .flatMap(Collection::stream);

        Stream.concat(multiPartVariants, variants)
            .flatMap(this::flattenToVariants)
            .map(Variant::modelLocation)
            .forEach(model -> this.models.put(blockKey, model));
    }

    public Stream<Variant> flattenToVariants(final BlockStateModel.Unbaked unbaked) {
        return switch (unbaked) {
            case SingleVariant.Unbaked(final var variant) -> Stream.of(variant);
            case WeightedVariants.Unbaked(final var entries) -> entries
                .unwrap()
                .stream()
                .map(Weighted::value)
                .flatMap(this::flattenToVariants);
            default -> {
                blockStateModelReporter.report(unbaked.getClass());
                yield Stream.empty();
            }
        };
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
                    (UnbakedModel) blockModel,
                    it -> it instanceof BlockModel && it.parent() != null,
                    it -> modelResources.get(it.parent())
                ).toList();

                final var textureSlots = new HashMap<String, TextureSlots.SlotContents>();
                for (final var model : models.reversed()) {
                    textureSlots.putAll(model.textureSlots().values());
                }

                return models
                    .stream()
                    .map(BlockModel::geometry)
                    .filter(Objects::nonNull)
                    .flatMap(this::flattenToElements)
                    .flatMap(element -> element.faces().values().stream())
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
                .forEach(image -> {
                    final var format = image.format();

                    if (format != NativeImage.Format.RGBA) {
                        final var template = "function application only works on RGBA images; have %s";
                        final var message = String.format(Locale.ROOT, template, format);
                        throw new IllegalArgumentException(message);
                    }

                    final var pointer = image.getPointer();

                    if (pointer == 0L) {
                        throw new IllegalStateException("Image is not allocated.");
                    }

                    final var pixelCount = image.getWidth() * image.getHeight();
                    final var buffer = MemoryUtil.memIntBuffer(pointer, pixelCount);

                    for (var i = 0; i < pixelCount; ++i) {
                        final var original = ARGB.fromABGR(buffer.get(i));
                        final var modified = normalize(original, _rgbMultiplier);
                        buffer.put(i, ARGB.toABGR(modified));
                    }
                });

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

    private Stream<BlockElement> flattenToElements(final UnbakedGeometry geometry) {
        return switch (geometry) {
            case SimpleUnbakedGeometry(final var elements) -> elements.stream();
            default -> {
                this.geometryReporter.report(geometry.getClass());
                yield Stream.empty();
            }
        };
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
        var value = 0;

        for (final var argb : argbArray) {
            final var a = ARGB.alpha(argb);
            final var r = ARGB.red(argb);
            final var g = ARGB.green(argb);
            final var b = ARGB.blue(argb);

            if (a < best) {
                continue;
            }

            final var key = Math.max(Math.max(r, g), b);

            if (a > best || key > value) {
                value = key;
            }

            best = a;
        }

        return ARGB.color(best, value, value, value);
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

    public PrintWriter collectReports(final IOSupplier<PrintWriter> writerProvider) throws IOException {
        if (!(this.blockStateModelReporter.consumeChanged() | this.geometryReporter.consumeChanged())) {
            return null;
        }

        final var writer = writerProvider.get();

        if (!(
            this.geometryReporter.collectReport(writer, "Unrecognized Unbaked Geometry classes:")
                | this.blockStateModelReporter.collectReport(writer, "Unrecognized Unbaked BlockStateModel classes:"))
        ) {
            return null;
        }

        return writer;
    }
}
