package io.github.startsmercury.visual_snowy_leaves.impl.client.util;

import net.minecraft.resources.ResourceLocation;

public class UnknownBlockStateDefinitionException extends RuntimeException {
    private final ResourceLocation resourceLocation;

    public UnknownBlockStateDefinitionException(final ResourceLocation resourceLocation) {
        super(resourceLocation.toString());

        this.resourceLocation = resourceLocation;
    }

    public ResourceLocation getResourceLocation() {
        return this.resourceLocation;
    }
}
