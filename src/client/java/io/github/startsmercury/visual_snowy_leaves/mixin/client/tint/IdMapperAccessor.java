package io.github.startsmercury.visual_snowy_leaves.mixin.client.tint;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.IdMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(IdMapper.class)
public interface IdMapperAccessor<T> {
    @Accessor
    Reference2IntMap<T> getTToId();

    @Accessor
    List<T> getIdToT();

    @Accessor
    void setNextId(int nextId);
}
