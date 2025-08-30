package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import static net.minecraft.client.resources.model.BlockStateModelLoader.BLOCKSTATE_LISTER;

import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import io.github.startsmercury.visual_snowy_leaves.impl.client.SpriteWhitener;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslConstants;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.SequencedCompletableFuture;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.UnknownBlockStateDefinitionException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.resources.model.BlockStateDefinitions;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {
    @Shadow
    @Final
    private BlockColors blockColors;

    @ModifyVariable(method = "reload", at = @At("STORE"), ordinal = 7)
    private CompletableFuture<SpriteLoader.Preparations> modifySprites(
        final CompletableFuture<SpriteLoader.Preparations> spritePreparationsFuture,
        final @Local(ordinal = 0, argsOnly = true) Executor executor,
        final @Local(ordinal = 0) ResourceManager resourceManager,
        final @Local(ordinal = 2) CompletableFuture<Map<ResourceLocation, UnbakedModel>> unbakedModelsFuture,
        final @Local(ordinal = 5) CompletableFuture<ModelManager.ResolvedModels> modelDiscoveryFuture
    ) {
        final var blockstateResourcesFuture = CompletableFuture.supplyAsync(
            () -> BLOCKSTATE_LISTER.listMatchingResourceStacks(resourceManager),
            executor
        );

        final var function = BlockStateDefinitions.definitionLocationToBlockStateMapper();
        final var visualSnowyLeaves = Minecraft.getInstance().getVisualSnowyLeaves();
        final var logger = visualSnowyLeaves.getLogger();

        final var blockModelDefinitionsFuture = blockstateResourcesFuture.thenCompose(map -> {
            final var list = new ArrayList<
                CompletableFuture<Map.Entry<ResourceLocation, ArrayList<BlockModelDefinition>>>
            >(map.size());

            for (final var entry : map.entrySet()) {
                list.add(CompletableFuture.supplyAsync(() -> {
                    final var resourceLocation = BLOCKSTATE_LISTER.fileToId(entry.getKey());
                    final var stateDefinition = function.apply(resourceLocation);

                    if (stateDefinition == null) {
                        throw new UnknownBlockStateDefinitionException(resourceLocation);
                    }

                    final var resources = entry.getValue();
                    final var blockModelDefinitions =
                        new ArrayList<BlockModelDefinition>(resources.size());

                    for (final var resource : resources) {
                        try (final var reader = resource.openAsReader()) {
                            final var jsonObject = GsonHelper.parse(reader);
                            final var blockModelDefinition =
                                BlockModelDefinition.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow(JsonParseException::new);
                            blockModelDefinitions.add(blockModelDefinition);
                        } catch (final Exception exception) {
                            logger.error(
                                "Failed to load blockstate definition {} from pack {}",
                                resourceLocation,
                                resource.sourcePackId(),
                                exception
                            );
                        }
                    }

                    return Map.entry(resourceLocation, blockModelDefinitions);
                }, executor));
            }

            return SequencedCompletableFuture.tryFilter(list, throwable -> {
                if (throwable instanceof CompletionException) {
                    throwable = throwable.getCause();
                }
                if (throwable instanceof final Error error) {
                    throw error;
                } else if (throwable instanceof final UnknownBlockStateDefinitionException cause) {
                    logger.debug(
                        "[{}] Discovered unknown block state definition {}, ignoring",
                        VslConstants.NAME,
                        cause.getResourceLocation()
                    );
                } else {
                    logger.error("[{}] Uncaught exception", VslConstants.NAME, throwable);
                }
            });
        });

        final var spriteWhitenerFuture = modelDiscoveryFuture.thenCompose(modelDiscovery -> {
            return blockModelDefinitionsFuture.thenApplyAsync(
                entries -> {
                    final var spriteWhitener = SpriteWhitener.create(visualSnowyLeaves);
                    for (final var entry : entries) {
                        for (final var blockModelDefinition : entry.getValue()) {
                            spriteWhitener.analyzeModels(entry.getKey(), blockModelDefinition);
                        }
                    }
                    return spriteWhitener;
                },
                executor
            );
        });

        final var waitForAllFuture = CompletableFuture.allOf(
            // Redundancy: already done in first `allOf`
            spriteWhitenerFuture,
            // No comment.
            // afterPreparationBarrierFuture,
            spritePreparationsFuture,
            // Redundancy: already done in second `allOf`
            unbakedModelsFuture
        );

        return waitForAllFuture.thenApplyAsync(_void -> {
            final var spriteWhitener = spriteWhitenerFuture.join();
            final var spritePreparations = spritePreparationsFuture.join();
            spriteWhitener.modifySprites(
                this.blockColors,
                unbakedModelsFuture.join(),
                spritePreparations::getSprite
            );
            visualSnowyLeaves.collectReports(spriteWhitener);
            final var ignored = visualSnowyLeaves.sendReportNotice();
            return spritePreparations;
        }, executor);
    }
}
