package io.github.startsmercury.visual_snowy_leaves.impl.client.util;

import net.minecraft.resources.Identifier;

public class UnknownBlockStateDefinitionException extends RuntimeException {
    private final Identifier resourceLocation;

    public UnknownBlockStateDefinitionException(final Identifier resourceLocation) {
        super(resourceLocation.toString());

        this.resourceLocation = resourceLocation;
    }

    public Identifier getIdentifier() {
        return this.resourceLocation;
    }
}
