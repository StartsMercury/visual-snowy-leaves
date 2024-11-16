package io.github.startsmercury.visual_snowy_leaves.impl.client.entrypoint;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.startsmercury.visual_snowy_leaves.impl.client.VslConstants;
import io.github.startsmercury.visual_snowy_leaves.impl.client.extension.compile.CompileVisualSnowyLeavesAware;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VisualSnowyLeavesModMenu implements ModMenuApi {
    private static void openConfigFile(
        final @SuppressWarnings("deprecation") CompileVisualSnowyLeavesAware aware
    ) {
        final var visualSnowyLeaves = aware.getVisualSnowyLeaves();

        visualSnowyLeaves.getLogger().debug("[{}] Opening config...", VslConstants.NAME);
        Util.getPlatform().openFile(visualSnowyLeaves.getConfigFile());

        visualSnowyLeaves.reloadConfig();
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> new Screen(Component.empty()) {
            @Override
            protected void init() {
                final var minecraft = this.minecraft;
                if (minecraft != null) {
                    openConfigFile(minecraft);
                    minecraft.setScreen(screen);
                }
            }
        };
    }
}
