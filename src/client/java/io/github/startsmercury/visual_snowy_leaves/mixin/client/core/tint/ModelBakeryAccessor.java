package io.github.startsmercury.visual_snowy_leaves.mixin.client.core.tint;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelBakery.class)
public interface ModelBakeryAccessor {
    @Accessor
    Map<ResourceLocation, BlockModel> getModelResources();
}
