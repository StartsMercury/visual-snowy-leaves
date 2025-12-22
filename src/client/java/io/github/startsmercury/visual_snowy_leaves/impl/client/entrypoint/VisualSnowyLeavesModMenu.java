//TODO Re-enable when ModMenu is available in non-obfuscated
//package io.github.startsmercury.visual_snowy_leaves.impl.client.entrypoint;
//
//import com.terraformersmc.modmenu.api.ConfigScreenFactory;
//import com.terraformersmc.modmenu.api.ModMenuApi;
//import io.github.startsmercury.visual_snowy_leaves.impl.client.gui.screens.ConfigScreen;
//import net.minecraft.client.Minecraft;
//
//public class VisualSnowyLeavesModMenu implements ModMenuApi {
//    @Override
//    public ConfigScreenFactory<?> getModConfigScreenFactory() {
//        return lastScreen -> {
//            final var minecraft = Minecraft.getInstance();
//            final var visualSnowyLeaves = minecraft.getVisualSnowyLeaves();
//            return new ConfigScreen(
//                lastScreen,
//                visualSnowyLeaves.getConfig(),
//                config -> {
//                    visualSnowyLeaves.setConfig(config);
//                    visualSnowyLeaves.saveConfig();
//                }
//            );
//        };
//    }
//}
