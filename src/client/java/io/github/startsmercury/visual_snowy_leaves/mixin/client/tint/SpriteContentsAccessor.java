package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteContents.class)
public interface SpriteContentsAccessor {
    @Accessor
    NativeImage[] getByMipLevel();

    @Accessor
    NativeImage getOriginalImage();
}
