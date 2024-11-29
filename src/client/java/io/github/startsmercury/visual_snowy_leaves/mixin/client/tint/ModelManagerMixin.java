package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.startsmercury.visual_snowy_leaves.impl.client.SpriteWhitener;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static net.minecraft.client.resources.model.BlockStateModelLoader.BLOCKSTATE_LISTER;

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {
    @Shadow
    @Final
    private BlockColors blockColors;

    @WrapOperation(
        method = "reload",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;thenAcceptAsync(Ljava/util/function/Consumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
            ordinal = 0
        )
    )
    private CompletableFuture<Void> modifySprites(
        final CompletableFuture<ModelManager.ReloadState> afterPreparationBarrierFuture,
        final Consumer<? super ModelManager.ReloadState> apply,
        final Executor applyExecutor,
        final Operation<CompletableFuture<Void>> op,
        final @Local(ordinal = 0, argsOnly = true) ResourceManager resourceManager,
        final @Local(ordinal = 0, argsOnly = true) Executor executor,
        final @Local(ordinal = 2) CompletableFuture<Map<ResourceLocation, UnbakedModel>> unbakedModelsFuture,
        final @Local(ordinal = 5) CompletableFuture<ModelDiscovery> modelDiscoveryFuture
    ) {
        final var blockstateResourcesFuture = CompletableFuture.supplyAsync(
            () -> BLOCKSTATE_LISTER.listMatchingResourceStacks(resourceManager),
            executor
        );

        final var function = BlockStateModelLoader.definitionLocationToBlockMapper();
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
                        logger.debug(
                            "[{}] Discovered unknown block state definition {}, ignoring",
                            VslConstants.NAME,
                            resourceLocation
                        );
                        return null;
                    }

                    final var resources = entry.getValue();
                    final var blockModelDefinitions =
                        new ArrayList<BlockModelDefinition>(resources.size());

                    for (final var resource : resources) {
                        try (final var reader = resource.openAsReader()) {
                            final var jsonObject = GsonHelper.parse(reader);
                            final var blockModelDefinition =
                                BlockModelDefinition.fromJsonElement(jsonObject);
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

            return Util.sequence(list);
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
            afterPreparationBarrierFuture,
            // Redundancy: already done in second `allOf`
            unbakedModelsFuture
        );

        @SuppressWarnings("deprecation")
        final var atlasKey = TextureAtlas.LOCATION_BLOCKS;

        final var modifySpritesFuture = waitForAllFuture.thenRunAsync(() -> {
            spriteWhitenerFuture.join().modifySprites(
                this.blockColors,
                unbakedModelsFuture.join(),
                afterPreparationBarrierFuture
                    .join()
                    .atlasPreparations()
                    .get(atlasKey)
            );
        }, applyExecutor);

        return CompletableFuture.allOf(
            modifySpritesFuture,
            op.call(afterPreparationBarrierFuture, apply, applyExecutor)
        );
    }
}
