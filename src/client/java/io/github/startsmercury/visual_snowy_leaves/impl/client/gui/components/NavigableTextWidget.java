package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class NavigableTextWidget extends MultiLineTextWidget {
    public NavigableTextWidget(final Component message, final Font font) {
        super(message, font);
        this.active = true;
    }

    protected void updateWidgetNarration(final NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }

    public void playDownSound(final SoundManager soundManager) {
    }
}
