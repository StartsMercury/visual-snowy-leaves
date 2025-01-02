package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TextureAtlas.Preparations.class)
public interface TextureAtlas$PreparationsAccessor {
    @Accessor
    List<TextureAtlasSprite> getRegions();
}
