package io.github.startsmercury.visual_snowy_leaves.impl.client.entrypoint;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;

public class VisualSnowyLeavesFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        final var visualSnowyLeaves = Minecraft.getInstance().getVisualSnowyLeaves();

        visualSnowyLeaves.reloadConfig();
    }
}
