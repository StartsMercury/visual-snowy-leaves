package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Config(
    RebuildInterval rebuildInterval,
    SnowyMode snowyMode,
    Set<ResourceLocation> targetBlockKeys,
    TransitionDuration transitionDuration
) {
    public static final Codec<Set<ResourceLocation>> TARGET_BLOCK_KEYS_CODEC =
        Codec.list(ResourceLocation.CODEC).comapFlatMap(
            list -> {
                try {
                    return DataResult.success(Set.copyOf(list));
                } catch (final NullPointerException cause) {
                    return DataResult.error(() -> "Immutable set cannot contain nulls");
                }
            },
            set -> {
                final var list = new ArrayList<>(set);
                list.sort(Comparator.naturalOrder());
                return list;
            }
        );

    public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            RebuildInterval.CODEC.fieldOf("rebuildInterval").forGetter(Config::rebuildInterval),
            SnowyMode.CODEC.fieldOf("snowyMode").forGetter(Config::snowyMode),
            TARGET_BLOCK_KEYS_CODEC.fieldOf("targetBlockKeys").forGetter(Config::targetBlockKeys),
            TransitionDuration.CODEC.fieldOf("transitionDuration").forGetter(Config::transitionDuration)
        ).apply(instance, Config::new));

    public static final RebuildInterval DEFAULT_REBUILD_INTERVAL = RebuildInterval.fromTicks(20);

    public static final SnowyMode DEFAULT_SNOWY_MODE = SnowyMode.SNOWING;

    public static final Set<ResourceLocation> DEFAULT_TARGET_BLOCK_KEYS = Stream.of(
        "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove"
    )
        .map(base -> base + "_leaves")
        .map(ResourceLocation::parse)
        .collect(Collectors.toUnmodifiableSet());

    @SuppressWarnings("deprecation")
    public static final TransitionDuration DEFAULT_TRANSITION_DURATION =
        TransitionDuration.fromTicksUnchecked(400);

    public static final Config DEFAULT = new Config(
        DEFAULT_REBUILD_INTERVAL,
        DEFAULT_SNOWY_MODE,
        DEFAULT_TARGET_BLOCK_KEYS,
        DEFAULT_TRANSITION_DURATION
    );
}
