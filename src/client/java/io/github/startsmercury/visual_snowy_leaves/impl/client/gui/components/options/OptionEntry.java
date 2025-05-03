package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.options;

import com.google.common.collect.Lists;
import io.github.startsmercury.visual_snowy_leaves.impl.client.gui.screens.ConfigScreen;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public abstract class OptionEntry extends ContainerObjectSelectionList.Entry<OptionEntry> {
    public record Context(ConfigScreen screen, Minecraft minecraft, Font font) {}

    protected static final int PREFERRED_WIDTH = 216, PREFERRED_HEIGHT = 20;

    protected static final int
        LINE_HEIGHT = PREFERRED_HEIGHT / 2,
        HALF_LINE_H = LINE_HEIGHT / 2;

    protected final List<AbstractWidget> children = Lists.newArrayList();

    protected final Context context;

    @Nullable
    protected final List<FormattedCharSequence> tooltip;

    protected OptionEntry(
        final Context context,
        final @Nullable List<FormattedCharSequence> tooltip
    ) {
        this.context = context;
        this.tooltip = tooltip;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return this.children;
    }
}
