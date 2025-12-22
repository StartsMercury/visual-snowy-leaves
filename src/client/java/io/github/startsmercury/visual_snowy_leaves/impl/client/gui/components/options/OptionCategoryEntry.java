package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.options;

import com.google.common.collect.ImmutableList;
import io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.NavigableTextWidget;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

public class OptionCategoryEntry extends OptionEntry {
    private static @Nullable List<FormattedCharSequence> createTooltip(
        final Context context,
        final Component name,
        final @Nullable Component description,
        final @Nullable Component details
    ) {
        if (description == null) {
            return null;
        }

        final var builder = ImmutableList.<FormattedCharSequence>builder();

        builder.add(name.plainCopy().withStyle(ChatFormatting.YELLOW).getVisualOrderText());

        context.font().split(description, 150).forEach(builder::add);

        if (details != null) {
            builder.add(details.getVisualOrderText());
        }

        return builder.build();
    }

    private final NavigableTextWidget label;

    private final Component fullName;

    private final @Nullable Component description;

    public OptionCategoryEntry(
        final Context context,
        final Component label,
        final Component fullName,
        final @Nullable Component description,
        final @Nullable Component details
    ) {
        super(context, createTooltip(context, fullName, description, details));

        this.label = new NavigableTextWidget(label, this.context.font());
        this.label.setHeight(OptionsList.ITEM_HEIGHT);
        this.children.add(this.label);

        this.fullName = fullName;
        this.description = description;
    }

    @Override
    public void renderContent(
        final GuiGraphics guiGraphics,
        final int mouseX,
        final int mouseY,
        final boolean hovered,
        final float deltaTicks
    ) {
        this.label.setX(this.getContentX() + (this.getContentWidth() - this.label.getWidth()) / 2);
        this.label.setY(this.getContentY() + HALF_LINE_H);

        this.label.render(guiGraphics, mouseX, mouseY, deltaTicks);
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return ImmutableList.of(new NarratableEntry() {
            public NarrationPriority narrationPriority() {
                return OptionCategoryEntry.this.label.narrationPriority();
            }

            public void updateNarration(final NarrationElementOutput output) {
                final var description = OptionCategoryEntry.this.description;
                output.add(
                    NarratedElementType.TITLE,
                    description == null ? fullName : OptionCategoryEntry.this
                        .fullName
                        .copy()
                        .append("\n")
                        .append(description)
                );
            }
        });
    }
}
