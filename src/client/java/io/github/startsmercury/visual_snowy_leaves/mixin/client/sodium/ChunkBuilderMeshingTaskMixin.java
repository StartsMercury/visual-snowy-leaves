//TODO Re-eneble when Sodium is available in non-obfuscated
//package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;
//
//import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//import com.llamalad7.mixinextras.sugar.Local;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.SnowAware;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.VertexEncoderInterface;
//import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
//import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
//import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.block.model.BlockStateModel;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.world.level.block.state.BlockState;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//
//@Mixin(ChunkBuilderMeshingTask.class)
//public class ChunkBuilderMeshingTaskMixin {
//    @WrapOperation(
//        method = "execute(" +
//            "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;" +
//            "Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;" +
//        ")Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
//        at = @At(
//            value = "INVOKE",
//            target =
//                "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;" +
//                "renderModel(" +
//                    "Lnet/minecraft/client/renderer/block/model/BlockStateModel;" +
//                    "Lnet/minecraft/world/level/block/state/BlockState;" +
//                    "Lnet/minecraft/core/BlockPos;" +
//                    "Lnet/minecraft/core/BlockPos;" +
//                ")V"
//        )
//    )
//    private void visual_snowy_leaves$onRenderModel(
//        final BlockRenderer instance,
//        final BlockStateModel model,
//        final BlockState state,
//        final BlockPos pos,
//        final BlockPos origin,
//        final Operation<Void> original,
//        final @Local(ordinal = 0, argsOnly = true) ChunkBuildContext context
//    ) {
//        final var vslConfig = Minecraft.getInstance().getVisualSnowyLeaves().getConfig();
//
//        final var snowAware = ((SnowAware) (Object) context.cache.getWorldSlice());
//        assert snowAware != null;
//
//        final var correctBiome = !vslConfig.requireSnowyBiomes()
//            || snowAware.visual_snowy_leaves$coldEnoughToSnow(pos);
//
//        if (correctBiome && vslConfig
//            .targetBlockKeys()
//            .contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()))
//        ) {
//            final var encoder = (VertexEncoderInterface) instance;
//            encoder.visual_snowy_leaves$beginSnowMarked();
//            original.call(instance, model, state, pos, origin);
//            encoder.visual_snowy_leaves$endSnowMarked();
//        } else {
//            original.call(instance, model, state, pos, origin);
//        }
//    }
//}
