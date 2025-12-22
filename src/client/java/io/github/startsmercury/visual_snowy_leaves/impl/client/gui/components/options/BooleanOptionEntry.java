package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.options;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

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
    public void renderContent(
        final GuiGraphics guiGraphics,
        final int mouseX,
        final int mouseY,
        final boolean hovered,
        final float deltaTicks
    ) {
        this.renderLabel(guiGraphics, this.getContentY(), this.getContentX());
        this.checkbox.setX(this.getContentRight() - CHECKBOX_WIDTH);
        this.checkbox.setY(this.getContentY());
        this.checkbox.render(guiGraphics, mouseX, mouseY, deltaTicks);
    }
}
