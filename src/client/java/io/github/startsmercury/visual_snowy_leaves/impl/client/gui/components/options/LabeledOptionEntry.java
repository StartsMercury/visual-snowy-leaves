package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.options;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

public abstract class LabeledOptionEntry extends OptionEntry {
    protected static final int SPACING = 1;

    private final List<FormattedCharSequence> label;

    public LabeledOptionEntry(
        final Context context,
        final Component component,
        @Nullable final List<FormattedCharSequence> tooltip
    ) {
        super(context, tooltip);

        this.label = this.context.font().split(component, this.getLeftWidth());
    }

    protected final int getLeftWidth() {
        return PREFERRED_WIDTH - SPACING - this.getRightWidth();
    }

    protected abstract int getRightWidth();

    protected void renderLabel(final GuiGraphics guiGraphics, final int rowTop, final int rowLeft) {
        final var font = this.context.font();

        if (this.label.size() == 1) {
            guiGraphics.drawString(font, this.label.getFirst(), rowLeft, rowTop + HALF_LINE_H, CommonColors.WHITE);
        } else if (this.label.size() >= 2) {
            guiGraphics.drawString(font, this.label.get(0), rowLeft, rowTop, CommonColors.WHITE);
            guiGraphics.drawString(font, this.label.get(1), rowLeft, rowTop + LINE_HEIGHT, CommonColors.WHITE);
        }
    }
}
