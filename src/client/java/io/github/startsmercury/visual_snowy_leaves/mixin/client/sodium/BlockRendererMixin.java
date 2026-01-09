//TODO Re-eneble when Sodium is available in non-obfuscated
//package io.github.startsmercury.visual_snowy_leaves.mixin.client.sodium;
//
//import com.llamalad7.mixinextras.sugar.Local;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.VertexEncoderInterface;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.VslMaterialParameters;
//import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
//import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.ModifyArg;
//
//@Mixin(BlockRenderer.class)
//public class BlockRendererMixin implements VertexEncoderInterface {
//    @Unique
//    private boolean snowMarked;
//
//    @Override
//    public void visual_snowy_leaves$beginSnowMarked() {
//        this.snowMarked = true;
//    }
//
//    @Override
//    public void visual_snowy_leaves$endSnowMarked() {
//        this.snowMarked = false;
//    }
//
//    @ModifyArg(
//        method = "bufferQuad",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/builder/ChunkMeshBufferBuilder;push([Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;I)V"
//        ),
//        index = 1
//    )
//    private int visual_snowy_leaves$writeVertex(
//        final int materialBits,
//        final @Local(name = "vertices") ChunkVertexEncoder.Vertex[] vertices
//    ) {
//        return VslMaterialParameters.packInto(materialBits, this.snowMarked);
//    }
//}
