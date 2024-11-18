package io.github.startsmercury.visual_snowy_leaves.impl.client.entrypoint;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static io.github.startsmercury.visual_snowy_leaves.impl.client.VslConstants.MODID;

public class VisualSnowyLeavesFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        final var fabricLoader = FabricLoader.getInstance();
        final var visualSnowyLeaves = Minecraft.getInstance().getVisualSnowyLeaves();

        visualSnowyLeaves.reloadConfig();

        if (!fabricLoader.isModLoaded("fabric-resource-loader-v0")) {
            return;
        }

        ResourceManagerHelper.registerBuiltinResourcePack(
            ResourceLocation.fromNamespaceAndPath(MODID, "vsl-jlf"),
            fabricLoader.getModContainer(MODID).orElseThrow(() -> new AssertionError(
                "Expected this mod ("
                    + MODID
                    + ") be loaded and recognized by VisualSnowyLeavesFabric"
            )),
            Component.literal("Jungle Leaves Fix"),
            ResourcePackActivationType.NORMAL
        );
    }
}
