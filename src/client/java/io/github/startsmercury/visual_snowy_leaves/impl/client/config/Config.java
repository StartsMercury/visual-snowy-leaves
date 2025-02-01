package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static io.github.startsmercury.visual_snowy_leaves.impl.client.config.Leaves.*;

public record Config(
    int version,
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

    public static final int MINIMUM_VERSION = 0;

    public static final int CURRENT_VERSION = 1;

    public static final RebuildInterval DEFAULT_REBUILD_INTERVAL = RebuildInterval.fromTicks(20);

    public static final SnowyMode DEFAULT_SNOWY_MODE = SnowyMode.SNOWING;

    public static final Set<ResourceLocation> DEFAULT_TARGET_BLOCK_KEYS = Set.of(
        OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK, MANGROVE, PALE_OAK
    );

    @SuppressWarnings("deprecation")
    public static final TransitionDuration DEFAULT_TRANSITION_DURATION =
        TransitionDuration.fromTicksUnchecked(400);

    @FunctionalInterface
    private interface CodecFieldBuilder {
        CodecFieldBuilder DEFAULT = CodecFieldBuilder::fieldOfImpl;
        CodecFieldBuilder OPTIONAL = Codec::optionalFieldOf;

        private static <A> MapCodec<A> fieldOfImpl(
            final Codec<A> self,
            final String name,
            final A defaultValue
        ) {
            return self.fieldOf(name);
        }

        <A> MapCodec<A> create(Codec<A> self, String name, A defaultValue);

        default <O, A> RecordCodecBuilder<O, A> create(
            final Codec<A> self,
            final String name,
            final A defaultValue,
            final Function<O, A> getter
        ) {
            return create(self, name, defaultValue).forGetter(getter);
        }
    }

    private static Codec<Config> newCodec(final CodecFieldBuilder fieldBuilder) {
        return RecordCodecBuilder.create(instance ->
            instance.group(
                fieldBuilder.create(
                    Codec.INT,
                    // Defaults may be partial, assume minimum version
                    "version",
                    MINIMUM_VERSION,
                    Config::version
                ),
                fieldBuilder.create(
                    RebuildInterval.CODEC,
                    "rebuildInterval",
                    DEFAULT_REBUILD_INTERVAL,
                    Config::rebuildInterval
                ),
                fieldBuilder.create(
                    SnowyMode.CODEC,
                    "snowyMode",
                    DEFAULT_SNOWY_MODE,
                    Config::snowyMode
                ),
                fieldBuilder.create(
                    TARGET_BLOCK_KEYS_CODEC,
                    "targetBlockKeys",
                    DEFAULT_TARGET_BLOCK_KEYS,
                    Config::targetBlockKeys
                ),
                fieldBuilder.create(
                    TransitionDuration.CODEC,
                    "transitionDuration",
                    DEFAULT_TRANSITION_DURATION,
                    Config::transitionDuration
                )
            ).apply(instance, Config::new));
    }

    public static final Codec<Config> LENIENT_CODEC = newCodec(CodecFieldBuilder.OPTIONAL);

    public static final Codec<Config> CODEC = newCodec(CodecFieldBuilder.DEFAULT);

    public static final Config DEFAULT = new Config(
        CURRENT_VERSION,
        DEFAULT_REBUILD_INTERVAL,
        DEFAULT_SNOWY_MODE,
        DEFAULT_TARGET_BLOCK_KEYS,
        DEFAULT_TRANSITION_DURATION
    );

    public Config upgrade() {
        if (version >= CURRENT_VERSION) {
            return this;
        }

        final var version = Math.max(this.version, MINIMUM_VERSION);
        var rebuildInterval = this.rebuildInterval;
        var snowyMode = this.snowyMode;
        var targetBlockKeys = new HashSet<>(this.targetBlockKeys);
        var transitionDuration = this.transitionDuration;

        switch (version) {
            case 0 -> targetBlockKeys.add(PALE_OAK);
            default -> {
                final var message = "Upgrade is not yet implemented for config version "
                    + version
                    + ". Immediately report this issue";
                throw new InternalError(message);
            }
        }

        return new Config(
            CURRENT_VERSION,
            rebuildInterval,
            snowyMode,
            Set.copyOf(targetBlockKeys),
            transitionDuration
        );
    }
}
