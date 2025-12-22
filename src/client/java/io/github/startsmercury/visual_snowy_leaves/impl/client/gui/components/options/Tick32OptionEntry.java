package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.options;

import io.github.startsmercury.visual_snowy_leaves.impl.client.config.Tick32;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.TickParseException;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.TickUtil;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

public final class Tick32OptionEntry<T extends Tick32<T>> extends LabeledOptionEntry {
    private static final int INPUT_WIDTH = 70;

    private final EditBox input;

    public Tick32OptionEntry(
        final Context context,
        final Component label,
        @Nullable final List<FormattedCharSequence> tooltip,
        final String narrateDefaults,
        final Supplier<? extends T> getter,
        final Consumer<? super T> setter
    ) {
        super(context, label, tooltip);
        final var value = getter.get();

        this.input = new EditBox(context.font(), 0, 0, INPUT_WIDTH, PREFERRED_HEIGHT, label.copy().append("\n").append(narrateDefaults).append("\n"));
        this.input.setMaxLength(11);
        this.input.setResponder(string -> {
            try {
                setter.accept(value.withTicks(TickUtil.parse(string)));
                input.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
                context.screen().clearInvalid(this);
            } catch (final TickParseException ignored) {
                input.setTextColor(CommonColors.RED);
                context.screen().markInvalid(this);
            }
        });
        this.input.setValue(TickUtil.format(value.asTicks(), true));
        this.input.moveCursorToStart(false);

        this.children.add(this.input);
    }

    @Override
    protected int getRightWidth() {
        return INPUT_WIDTH;
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
        this.input.setX(this.getContentRight() - INPUT_WIDTH);
        this.input.setY(this.getContentY());
        this.input.render(guiGraphics, mouseX, mouseY, deltaTicks);
    }
}
