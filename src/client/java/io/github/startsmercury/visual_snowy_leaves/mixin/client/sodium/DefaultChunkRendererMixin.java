//TODO Re-eneble when Sodium is available in non-obfuscated
//package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;
//
//import com.llamalad7.mixinextras.sugar.Local;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.ChunkShaderInterfaceExtension;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.SodiumSnowProgress;
//import net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer;
//import net.caffeinemc.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(DefaultChunkRenderer.class)
//public abstract class DefaultChunkRendererMixin {
//    @Inject(
//        method = "render",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/DefaultChunkRenderer;setModelMatrixUniforms(" +
//                "Lnet/caffeinemc/mods/sodium/client/render/chunk/shader/ChunkShaderInterface;" +
//                "Lnet/caffeinemc/mods/sodium/client/render/chunk/region/RenderRegion;" +
//                "Lnet/caffeinemc/mods/sodium/client/render/viewport/CameraTransform;" +
//                "Lnet/caffeinemc/mods/sodium/client/gl/buffer/GlBuffer;" +
//            ")V"))
//    private void updateSnowProgress(
//        final CallbackInfo callback,
//        final @Local(name = "shader") ChunkShaderInterface shader
//    ) {
//        ((ChunkShaderInterfaceExtension) shader)
//            .visual_snowy_leaves$setSnowProgress(SodiumSnowProgress.get());
//    }
//}
