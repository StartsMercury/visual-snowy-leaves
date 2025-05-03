package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.options;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public final class BooleanOptionEntry extends LabeledOptionEntry {
    private static final int CHECKBOX_WIDTH = 45;

    private final CycleButton<Boolean> checkbox;

    public BooleanOptionEntry(
        final Context context,
        final Component label,
        @Nullable final List<FormattedCharSequence> tooltip,
        final String narrateDefaults,
        final Supplier<? extends Boolean> getter,
        final Consumer<? super Boolean> setter
    ) {
        super(context, label, tooltip);
        this.checkbox = CycleButton.onOffBuilder(getter.get())
            .displayOnlyValue()
            .withCustomNarration(cycleButton -> cycleButton
                .createDefaultNarrationMessage()
                .append("\n")
                .append(narrateDefaults)
            )
            .create(0, 0, CHECKBOX_WIDTH, PREFERRED_HEIGHT, label, (button, b) -> setter.accept(b));
        this.children.add(this.checkbox);
    }

    @Override
    protected int getRightWidth() {
        return CHECKBOX_WIDTH;
    }

    @Override
    public void render(
        final GuiGraphics guiGraphics,
        final int index,
        final int rowTop,
        final int rowLeft,
        final int rowWidth,
        final int itemHeight,
        final int mouseX,
        final int mouseY,
        final boolean hovered,
        final float deltaTicks
    ) {
        this.renderLabel(guiGraphics, rowTop, rowLeft);
        this.checkbox.setX(rowLeft + rowWidth - CHECKBOX_WIDTH - 2 * OptionsList.ITEM_INSET);
        this.checkbox.setY(rowTop);
        this.checkbox.render(guiGraphics, mouseX, mouseY, deltaTicks);
    }
}
