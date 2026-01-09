//TODO Re-eneble when Sodium+Iris is available in non-obfuscated
//package io.github.startsmercury.visual_snowy_leaves.mixin.client.iris;
//
//import com.llamalad7.mixinextras.sugar.Local;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.sodium.ChunkShaderInterfaceExtension;
//import net.caffeinemc.mods.sodium.client.gl.shader.uniform.GlUniformFloat;
//import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
//import net.irisshaders.iris.pipeline.programs.SodiumShader;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(SodiumShader.class)
//public class SodiumShaderMixin implements ChunkShaderInterfaceExtension {
//    @Unique
//    private GlUniformFloat uniformSnowProgress;
//
//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void onInit(
//        final CallbackInfo callback,
//        final @Local(ordinal = 0, argsOnly = true) ShaderBindingContext context
//    ) {
//        this.uniformSnowProgress = context.bindUniformOptional("u_VslSnowProgress", GlUniformFloat::new);
//    }
//
//    @Override
//    public void visual_snowy_leaves$setSnowProgress(final float snowProgress) {
//        if (this.uniformSnowProgress != null) {
//            this.uniformSnowProgress.setFloat(snowProgress);
//        }
//    }
//}
