package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.options;

import io.github.startsmercury.visual_snowy_leaves.impl.client.gui.screens.ConfigScreen;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public class CustomizeOptionEntry extends LabeledOptionEntry {
    private static final int BUTTON_WIDTH = 70;

    private static final Component CUSTOMIZE =
        Component.translatable("visual-snowy-leaves.config.option.advance.customize");

    private static final Component BASE_NARRATION =
        AbstractWidget.wrapDefaultNarrationMessage(CUSTOMIZE);

    private final Button button;

    public CustomizeOptionEntry(
        final Context context,
        final Component label,
        @Nullable final List<FormattedCharSequence> tooltip,
        final String narrateDefaults,
        final Function<ConfigScreen, ? extends Screen> screen
    ) {
        super(context, label, tooltip);

        this.button = Button.builder(CUSTOMIZE, button ->
            context.minecraft().setScreen(screen.apply(context.screen()))
        ).width(BUTTON_WIDTH)
            .createNarration(supplier -> label
                .copy()
                .append("\n")
                .append(supplier.get())
                .append("\n")
                .append(narrateDefaults)
            )
            .build();
        this.children.add(this.button);
    }


    @Override
    protected int getRightWidth() {
        return BUTTON_WIDTH;
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
        this.button.setX(rowLeft + rowWidth - BUTTON_WIDTH - 2 * OptionsList.ITEM_INSET);
        this.button.setY(rowTop);
        this.button.render(guiGraphics, mouseX, mouseY, deltaTicks);
    }
}
