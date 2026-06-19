package io.github.startsmercury.visual_snowy_leaves.impl.client.entrypoint;

import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.startsmercury.visual_snowy_leaves.impl.client.vertices.VslVertexFormats;
import io.github.startsmercury.visual_snowy_leaves.impl.client.vertices.sodium.EntityToTerrainVertexSerializer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.vertices.sodium.ModelToEntityVertexSerializer;
import io.github.startsmercury.visual_snowy_leaves.impl.client.vertices.sodium.VslEntityToTerrainVertexSerializer;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializerRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;

public class VisualSnowyLeavesFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        final var visualSnowyLeaves = Minecraft.getInstance().getVisualSnowyLeaves();
        visualSnowyLeaves.reloadConfig();
        visualSnowyLeaves.registerKeyMappings();

        registerSerializer(EntityVertex.FORMAT, VslVertexFormats.TERRAIN, new EntityToTerrainVertexSerializer());
        registerSerializer(VslVertexFormats.ENTITY, VslVertexFormats.TERRAIN, new VslEntityToTerrainVertexSerializer());
        registerSerializer(EntityVertex.FORMAT, VslVertexFormats.ENTITY, new ModelToEntityVertexSerializer());
    }

    private static void registerSerializer(final VertexFormat src, final VertexFormat dest, final
        VertexSerializer serializer) {
        VertexSerializerRegistry.instance().registerSerializer(src, dest, serializer);
    }
}
