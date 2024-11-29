package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(BlockModel.class)
public interface BlockModelInvoker {
    @Invoker
    List<BlockElement> callGetElements();
}
