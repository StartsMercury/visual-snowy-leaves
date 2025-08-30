package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.options;

import com.google.common.collect.ImmutableList;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.Config;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.RebuildInterval;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.SnowyMode;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.Tick32;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.TickUtil;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.TransitionDuration;
import io.github.startsmercury.visual_snowy_leaves.impl.client.gui.screens.TargetBlocksScreen;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public class OptionsList extends ContainerObjectSelectionList<OptionEntry> {
    @FunctionalInterface
    private interface EntryFactory<T> {
        LabeledOptionEntry create(
            OptionEntry.Context context,
            Component label,
            @Nullable List<FormattedCharSequence> tooltip,
            String narrateDefaults,
            Supplier<? extends T> getter,
            Consumer<? super T> setter
        );
    }

    public static final int ITEM_INSET = 2;
    public static final int ITEM_HEIGHT = OptionEntry.PREFERRED_HEIGHT + 2 * ITEM_INSET;

    private final OptionEntry.Context context;

    @Nullable
    private OptionEntry relevant;

    private boolean disabled;
    private RebuildInterval rebuildInterval;
    private boolean requireSnowyBiomes;
    private boolean requireSnowyWeather;
    private final SnowyMode snowyMode;
    private Set<ResourceLocation> targetBlockKeys;
    private TransitionDuration transitionDuration;

    public OptionsList(final OptionEntry.Context context, final Config initialConfig, final int width, final int height, final int y) {
        super(Minecraft.getInstance(), width, height, y, ITEM_HEIGHT);

        this.context = context;

        this.disabled = initialConfig.disabled();
        this.rebuildInterval = initialConfig.rebuildInterval();
        this.requireSnowyBiomes = initialConfig.requireSnowyBiomes();
        this.requireSnowyWeather = initialConfig.requireSnowyWeather();
        this.snowyMode = initialConfig.snowyMode();
        this.targetBlockKeys = initialConfig.targetBlockKeys();
        this.transitionDuration = initialConfig.transitionDuration();

        this.addCategoryEntry("toggles", null);
        this.addEntry("disabled", Config.DEFAULT.disabled(), () -> this.disabled, b -> this.disabled = b);
        this.addEntry("requireSnowyBiomes", Config.DEFAULT.requireSnowyBiomes(), () -> this.requireSnowyBiomes, b -> this.requireSnowyBiomes = b);
        this.addEntry("requireSnowyWeather", Config.DEFAULT.requireSnowyWeather(), () -> this.requireSnowyWeather, b -> this.requireSnowyWeather = b);

        this.addCategoryEntry("time", Component.literal("h:mm:ss+tt").withStyle(ChatFormatting.GRAY));
        this.addEntry("rebuildInterval", Config.DEFAULT.rebuildInterval(), () -> this.rebuildInterval, t -> this.rebuildInterval = t);
        this.addEntry("transitionDuration", Config.DEFAULT.transitionDuration(), () -> this.transitionDuration, t -> this.transitionDuration = t);

        this.addCategoryEntry("advance", null);
        this.addEntry(
            "targetBlockKeys",
            "",
            () -> this.targetBlockKeys,
            c -> this.targetBlockKeys = c,
            (c, label, tooltip, narrateDefaults, getter, setter) -> new CustomizeOptionEntry(
                c,
                label,
                tooltip,
                narrateDefaults,
                screen -> new TargetBlocksScreen(screen, getter.get(), setter)
            )
        );
    }

    public Config build() {
        return new Config(
            Config.CURRENT_VERSION,
            this.disabled,
            this.rebuildInterval,
            this.requireSnowyBiomes,
            this.requireSnowyWeather,
            this.snowyMode,
            this.targetBlockKeys,
            this.transitionDuration
        );
    }

    @Override
    public void setSelected(final @Nullable OptionEntry entry) {
        super.setSelected(entry);

        this.relevant = this.minecraft.getLastInputType().isKeyboard() ? entry : null;
    }

    public void renderWidget(
        final GuiGraphics guiGraphics,
        final int mouseX,
        final int mouseY,
        final float deltaTicks
    ) {
        super.renderWidget(guiGraphics, mouseX, mouseY, deltaTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderTooltip(
        final GuiGraphics guiGraphics,
        final int mouseX,
        final int mouseY
    ) {
        final List<FormattedCharSequence> tooltip;
        final ClientTooltipPositioner positioner;

        final var relevant = this.relevant;
        if (relevant != null) {
            if ((tooltip = relevant.tooltip) == null) {
                return;
            }

            positioner = new BelowOrAboveWidgetTooltipPositioner(new ScreenRectangle(
                this.getRowLeft(),
                getRowTop(this.children().indexOf(this.relevant)),
                this.getWidth(),
                this.defaultEntryHeight
            ));
        } else {
            final var hovered = this.getHovered();

            if (hovered == null || (tooltip = hovered.tooltip) == null) {
                return;
            }

            positioner = DefaultTooltipPositioner.INSTANCE;
        }

        guiGraphics.setTooltipForNextFrame(
            this.minecraft.font,
            tooltip,
            positioner,
            mouseX,
            mouseY,
            false
        );
    }

    @Override
    public void setFocused(@Nullable final GuiEventListener guiEventListener) {
        final var previous = this.getSelected();
        super.setFocused(guiEventListener);
        if (guiEventListener != null && this.getSelected() == null) {
            this.setSelected(previous);
        }
        System.out.println(this.getSelected());
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        this.relevant = null;
    }

    @Override
    protected boolean entriesCanBeSelected() {
        return true;
    }

    private void addCategoryEntry(final String id, final @Nullable Component details) {
        final var labelId = "visual-snowy-leaves.config.option.category." + id;
        final var longId = labelId + ".long";
        final var descriptionId = labelId + ".description";

        this.addEntry(new OptionCategoryEntry(
            context,
            Component.translatable(labelId).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW),
            Component.translatable(longId),
            I18n.exists(descriptionId) ? Component.translatable(descriptionId) : null,
            details
        ));
    }

    private void addEntry(
        final String id,
        final boolean defaultValue,
        final BooleanSupplier getter,
        final BooleanConsumer setter
    ) {
        this.<Boolean>addEntry(id, Boolean.toString(defaultValue), getter::getAsBoolean, setter, BooleanOptionEntry::new);
    }

    private <T extends Tick32<T>> void addEntry(
        final String id,
        final T defaultValue,
        final Supplier<T> getter,
        final Consumer<T> setter
    ) {
        this.addEntry(id, TickUtil.format(defaultValue.asTicks(), true), getter, setter, Tick32OptionEntry::new);
    }

    private <T> void addEntry(
        final String id,
        final String defaultString,
        final Supplier<T> getter,
        final Consumer<? super T> setter,
        final EntryFactory<T> factory
    ) {
        final var labelId = "visual-snowy-leaves.config.option." + id;
        final var label = Component.translatable(labelId);
        final var internalName = Component.literal(id).withStyle(ChatFormatting.YELLOW);
        final var defaultComponent = defaultString.isEmpty() ? null :
            Component.translatable("editGamerule.default", Component.literal(defaultString))
                .withStyle(ChatFormatting.GRAY);
        final var descriptionId = labelId + ".description";

        final List<FormattedCharSequence> tooltip;
        final String narrateDefaults;

        if (I18n.exists(descriptionId)) {
            final var builder = ImmutableList.<FormattedCharSequence>builder()
                .add(internalName.getVisualOrderText());

            final var description = Component.translatable(descriptionId);
            context.font().split(description, 150).forEach(builder::add);

            if (defaultComponent != null) {
                tooltip = builder.add(defaultComponent.getVisualOrderText()).build();
                narrateDefaults = description.getString() + "\n" + defaultComponent.getString();
            } else {
                tooltip = builder.build();
                narrateDefaults = description.getString();
            }
        } else if (defaultComponent != null) {
            tooltip = ImmutableList.of(
                internalName.getVisualOrderText(),
                defaultComponent.getVisualOrderText()
            );
            narrateDefaults = defaultComponent.getString();
        } else {
            tooltip = ImmutableList.of();
            narrateDefaults = "";
        }

        this.addEntry(factory.create(context, label, tooltip, narrateDefaults, getter, setter));
    }
}
