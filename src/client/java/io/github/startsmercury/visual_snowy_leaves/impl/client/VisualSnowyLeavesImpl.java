package io.github.startsmercury.visual_snowy_leaves.impl.client;

import io.github.startsmercury.visual_snowy_leaves.mixin.client.core.transition.minecraft.ClientChunkCache$StorageAccessor;
import io.github.startsmercury.visual_snowy_leaves.mixin.client.core.transition.minecraft.ClientChunkCacheAccessor;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VisualSnowyLeavesImpl {
    public static final String MODID = "visual-snowy-leaves";

    private static int tryIntoTicksOrMax(final Duration duration) {
        try {
            return (int) Math.min(duration.dividedBy(ONE_TICK), Integer.MAX_VALUE);
        } catch (final ArithmeticException ignored) {
            return Integer.MAX_VALUE;
        }
    }

    public static final Set<? extends ResourceLocation> TARGET_BLOCKS = Stream.of(
        "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove"
    )
        .map(base -> base + "_leaves")
        .map(ResourceLocation::new)
        .collect(Collectors.toSet());

    public static void init() {
        final var fabricLoader = FabricLoader.getInstance();

        if (!fabricLoader.isModLoaded("fabric-resource-loader-v0")) {
            return;
        }

        ResourceManagerHelper.registerBuiltinResourcePack(
            new ResourceLocation(MODID, "vsl-jlf"),
            fabricLoader.getModContainer(MODID).orElseThrow(() -> new AssertionError(
                "Expected this mod (" + MODID + ") be loaded and recognized by Fabric"
            )),
            Component.literal("Jungle Leaves Fix"),
            ResourcePackActivationType.NORMAL
        );
    }

    private static final Duration ONE_SECOND = Duration.ofSeconds(1L);

    private static final Duration ONE_TICK = ONE_SECOND.dividedBy(SharedConstants.TICKS_PER_SECOND);

    public static final Duration MAX_DURATION = ONE_TICK.multipliedBy(Integer.MAX_VALUE).dividedBy(255 * 255);

    public static final Duration DEFAULT_DURATION = Duration.ofSeconds(20);

    public static final Duration DEFAULT_INTERVAL = ONE_SECOND;

    private int elapsed;

    private final int interval;

    private final int maxSnowiness;

    private int snowiness;

    public VisualSnowyLeavesImpl() {
        this(DEFAULT_DURATION, DEFAULT_INTERVAL);
    }

    public VisualSnowyLeavesImpl(final Duration fullTransition, final Duration interval) {
        if (fullTransition.compareTo(MAX_DURATION) > 0) {
            final var message = "Expected a duration not greater than "
                + MAX_DURATION
                + " instead got "
                + fullTransition;
            throw new IllegalArgumentException(message);
        }

        this.interval = tryIntoTicksOrMax(interval);
        this.maxSnowiness = (int) fullTransition.dividedBy(ONE_TICK);
    }

    public int getInterval() {
        return this.interval;
    }

    public int getMaxSnowiness() {
        return this.maxSnowiness;
    }

    public int getSnowiness() {
        return this.snowiness;
    }

    public void setSnowiness(final int snowiness) {
        this.snowiness = Mth.clamp(snowiness, 0, this.maxSnowiness);
    }

    public void tick(final ClientLevel level) {
        final var elapsed = this.elapsed;
        final var changed = level.isRaining()
            ? this.tickSnowinessIncrement()
            : this.tickSnowinessDecrement();

        if (changed) {
            if (elapsed < this.getInterval()) {
                this.elapsed = elapsed + 1;
                return;
            }
        } else {
            if (elapsed <= 0) {
                return;
            }
        }

        final var chunkSource = (ClientChunkCacheAccessor) level.getChunkSource();
        final var storage = (ClientChunkCache$StorageAccessor) (Object) chunkSource.getStorage();
        if (storage == null) {
            return;
        }

        final var chunks = storage.getChunks();
        final var chunkCount = chunks.length();

        for (var i = 0; i < chunkCount; i++) {
            final var chunk = chunks.getPlain(i);
            if (chunk == null) {
                continue;
            }

            final var pos = chunk.getPos();
            final var levelChunkSections = chunk.getSections();

            for (var k = 0; k < levelChunkSections.length; ++k) {
                final var l = level.getSectionYFromSectionIndex(k);
                level.setSectionDirtyWithNeighbors(pos.x, l, pos.z);
            }
        }

        this.elapsed = 0;
    }

    public boolean tickSnowinessDecrement() {
        final var snowiness = this.snowiness;
        if (snowiness <= 0) {
            return false;
        }
        this.snowiness = snowiness - 1;
        return true;
    }

    public boolean tickSnowinessIncrement() {
        final var snowiness = this.snowiness;
        if (snowiness >= this.maxSnowiness) {
            return false;
        }
        this.snowiness = snowiness + 1;
        return true;
    }
}
