package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.screens;

import com.google.common.collect.Sets;
import io.github.startsmercury.visual_snowy_leaves.impl.client.config.Config;
import io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.options.OptionEntry;
import io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.options.OptionsList;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class ConfigScreen extends Screen {
    private static final Component TITLE = Component.translatable("visual-snowy-leaves.config.title");

    private final Set<OptionEntry> invalidEntries = Sets.newHashSet();

    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    private final Consumer<? super Config> configCallback;

    private final Config initialConfig;

    protected final @Nullable Screen lastScreen;

    @Nullable
    private Button doneButton;

    @Nullable
    private OptionsList list;

    public ConfigScreen(
        final @Nullable Screen lastScreen,
        final Config initialConfig,
        final Consumer<? super Config> configCallback
    ) {
        super(TITLE);
        this.initialConfig = initialConfig;
        this.lastScreen = lastScreen;
        this.configCallback = configCallback;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        assert this.minecraft != null;
        this.list = this.layout.addToContents(new OptionsList(
            new OptionEntry.Context(this, this.minecraft, this.font),
            this.initialConfig,
            this.width,
            this.layout.getContentHeight(),
            this.layout.getHeaderHeight()
        ));
        final var linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        this.doneButton = linearLayout.addChild(
            Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build()
        );
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.list != null) {
            this.list.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        if (!this.invalidEntries.isEmpty()) return;

        assert this.list != null;
        this.configCallback.accept(this.list.build());

        final var minecraft = this.minecraft;
        assert minecraft != null;
        minecraft.setScreen(this.lastScreen);
    }

    public void markInvalid(final OptionEntry ruleEntry) {
        this.invalidEntries.add(ruleEntry);
        this.updateDoneButton();
    }

    public void clearInvalid(final OptionEntry ruleEntry) {
        this.invalidEntries.remove(ruleEntry);
        this.updateDoneButton();
    }

    private void updateDoneButton() {
        if (this.doneButton != null) {
            this.doneButton.active = this.invalidEntries.isEmpty();
        }
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        assert this.list != null;
        this.list.mouseMoved(mouseX, mouseY);
    }
}
