package io.github.startsmercury.visual_snowy_leaves.impl.client.config;

import static io.github.startsmercury.visual_snowy_leaves.impl.client.config.Leaves.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.Identifier;

public record Config(
    int version,
    boolean disabled,
    FreshFreezeDelay freshFreezeDelay,
    FullMeltDelay fullMeltDelay,
    @Deprecated(forRemoval = true) RebuildInterval rebuildInterval,
    boolean requireSnowyBiomes,
    boolean requireSnowyWeather,
    @Deprecated(forRemoval = true) SnowyMode snowyMode,
    Set<Identifier> targetBlockKeys,
    TransitionDuration transitionDuration
) {
    public static final Codec<Set<Identifier>> TARGET_BLOCK_KEYS_CODEC =
        Codec.list(Identifier.CODEC).comapFlatMap(
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

    public static final int CURRENT_VERSION = 3;

    @SuppressWarnings("deprecation")
    public static final FreshFreezeDelay DEFAULT_FRESH_FREEZE_DELAY =
        FreshFreezeDelay.fromTicksUnchecked(100);

    @SuppressWarnings("deprecation")
    public static final FullMeltDelay DEFAULT_FULL_MELT_DELAY =
        FullMeltDelay.fromTicksUnchecked(100);

    public static final RebuildInterval DEFAULT_REBUILD_INTERVAL = RebuildInterval.fromTicks(20);

    public static final SnowyMode DEFAULT_SNOWY_MODE = SnowyMode.SNOWING;

    public static final Set<Identifier> DEFAULT_TARGET_BLOCK_KEYS = Set.of(
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
                    Codec.BOOL,
                    "disabled",
                    false,
                    Config::disabled
                ),
                fieldBuilder.create(
                    FreshFreezeDelay.CODEC,
                    "freshFreezeDelay",
                    DEFAULT_FRESH_FREEZE_DELAY,
                    Config::freshFreezeDelay
                ),
                fieldBuilder.create(
                    FullMeltDelay.CODEC,
                    "fullMeltDelay",
                    DEFAULT_FULL_MELT_DELAY,
                    Config::fullMeltDelay
                ),
                fieldBuilder.create(
                    RebuildInterval.CODEC,
                    "rebuildInterval",
                    DEFAULT_REBUILD_INTERVAL,
                    Config::rebuildInterval
                ),
                fieldBuilder.create(
                    Codec.BOOL,
                    "requireSnowyBiomes",
                    true,
                    Config::requireSnowyBiomes
                ),
                fieldBuilder.create(
                    Codec.BOOL,
                    "requireSnowyWeather",
                    true,
                    Config::requireSnowyWeather
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
        false,
        DEFAULT_FRESH_FREEZE_DELAY,
        DEFAULT_FULL_MELT_DELAY,
        DEFAULT_REBUILD_INTERVAL,
        true,
        true,
        DEFAULT_SNOWY_MODE,
        DEFAULT_TARGET_BLOCK_KEYS,
        DEFAULT_TRANSITION_DURATION
    );

    public Config upgrade() {
        if (version >= CURRENT_VERSION) {
            return this;
        }

        final var version = Math.max(this.version, MINIMUM_VERSION);
        var disabled = this.disabled;
        var freshFreezeDelay = this.freshFreezeDelay;
        var fullMeltDelay = this.fullMeltDelay;
        var rebuildInterval = this.rebuildInterval;
        var requireSnowyBiomes = this.requireSnowyBiomes;
        var requireSnowyWeather = this.requireSnowyWeather;
        var snowyMode = this.snowyMode;
        var targetBlockKeys = new HashSet<>(this.targetBlockKeys);
        var transitionDuration = this.transitionDuration;

        switch (version) {
            case 0 -> targetBlockKeys.add(PALE_OAK);
            case 1 -> {
                switch (snowyMode) {
                    case NEVER -> disabled = true;
                    case SNOWING -> {
                        disabled = false;
                        requireSnowyBiomes = true;
                        requireSnowyWeather = true;
                    }
                    case ALWAYS -> {
                        disabled = false;
                        requireSnowyBiomes = false;
                        requireSnowyWeather = false;
                    }
                }
            }
            // Do nothing, we removed rebuildInterval at v3
            case 2 -> {}
            default -> {
                final var message = "Upgrade is not yet implemented for config version "
                    + version
                    + ". Immediately report this issue";
                throw new InternalError(message);
            }
        }

        return new Config(
            CURRENT_VERSION,
            disabled,
            freshFreezeDelay,
            fullMeltDelay,
            rebuildInterval,
            requireSnowyBiomes,
            requireSnowyWeather,
            snowyMode,
            Set.copyOf(targetBlockKeys),
            transitionDuration
        );
    }

    public DataResult<JsonElement> encodeAsJson() {
        final var version = this.version;

        return Config.CODEC.encodeStart(JsonOps.INSTANCE, this).map((final var json) -> {
            if (json instanceof final JsonObject object) {
                switch (version) {
                    case 3:
                        object.remove("rebuildInterval");
                    case 2:
                        object.remove("snowyMode");
                    case 1:
                    case 0:
                        break;
                }
            }

            return json;
        });
    }
}
