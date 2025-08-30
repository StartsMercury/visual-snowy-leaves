package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.TargetBlocksList;
import io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.suggest.InputSuggestions;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class TargetBlocksScreen extends Screen {
    private static final int BUTTON_ROW_WIDTH = 308;
    private static final int BUTTON_WIDTH = 74;

    private static final Component TITLE = Component.translatable("visual-snowy-leaves.config.targetBlocks.title");
    private static final Component ADD = Component.translatable("visual-snowy-leaves.config.targetBlocks.add");
    private static final Component REMOVE = Component.translatable("visual-snowy-leaves.config.targetBlocks.remove");
    private static final Component INPUT = Component.translatable("visual-snowy-leaves.config.targetBlocks.input");

    private static final Comparator<? super ResourceLocation> BLOCKS_BY_NAME_OR_ID = Comparator.comparing(ResourceLocation::getNamespace)
        .thenComparing(
            id -> BuiltInRegistries.BLOCK
                .getOptional(id)
                .map(Block::asItem)
                .map(ItemStack::new)
                .map(ItemStack::getHoverName)
                .map(Component::getString),
            (lhs, rhs) -> lhs.isEmpty() && rhs.isEmpty() ? 0
                : lhs.isEmpty() ? 1
                : rhs.isEmpty() ? -1
                : lhs.get().compareTo(rhs.get())
        )
        .thenComparing(ResourceLocation::getPath);

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 78);

    private final Consumer<? super Set<ResourceLocation>> callback;

    private final ConfigScreen lastScreen;

    private final ResourceLocation[] targets;

    @Nullable
    private Button addButton;

    @Nullable
    private EditBox inputEdit;

    @Nullable
    private TargetBlocksList list;

    @Nullable
    private Button removeButton;

    @Nullable
    private InputSuggestions suggestions;

    public TargetBlocksScreen(
        final ConfigScreen lastScreen,
        final Set<? extends ResourceLocation> targets,
        final Consumer<? super Set<ResourceLocation>> callback
    ) {
        super(TITLE);

        this.callback = callback;
        this.lastScreen = lastScreen;
        this.targets = targets.toArray(ResourceLocation[]::new);

        Arrays.sort(this.targets, BLOCKS_BY_NAME_OR_ID);
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);
        assert this.minecraft != null;
        this.list = this.layout.addToContents(new TargetBlocksList(this, this.minecraft, this.font, this.width, this.height,43, 24));

        for (final var target : this.targets) {
            this.list.addKey(target);
        }

        this.inputEdit = new EditBox(this.font, BUTTON_ROW_WIDTH, Button.DEFAULT_HEIGHT, INPUT) {
            protected MutableComponent createNarrationMessage() {
                final var suggestions = TargetBlocksScreen.this.suggestions;
                assert suggestions != null;
                return super.createNarrationMessage().append(suggestions.getNarrationMessage());
            }
        };
        this.addButton = Button.builder(ADD, button -> {
            final var id = ResourceLocation.tryParse(inputEdit.getValue());
            if (id == null) return;
            this.list.addKey(id);
            button.active = false;
            inputEdit.setValue("");
        }).width(BUTTON_WIDTH).build();

        addButton.active = false;

        inputEdit.setResponder(this::onEdited);

        final var buttonsRow = new EqualSpacingLayout(BUTTON_ROW_WIDTH, Button.DEFAULT_HEIGHT, EqualSpacingLayout.Orientation.HORIZONTAL);
        buttonsRow.addChild(addButton);
        this.removeButton = buttonsRow.addChild(Button.builder(REMOVE, button -> this.list.removeSelectedKey()).width(BUTTON_WIDTH).build());
        buttonsRow.addChild(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onDone()).width(BUTTON_WIDTH).build());
        buttonsRow.addChild(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.onClose()).width(BUTTON_WIDTH).build());

        final var rows = this.layout.addToFooter(LinearLayout.vertical().spacing(4));
        rows.defaultCellSetting().alignVerticallyMiddle();
        rows.addChild(new StringWidget(BUTTON_ROW_WIDTH, 10, INPUT, this.font))
            .setColor(CommonColors.LIGHT_GRAY);
        rows.addChild(inputEdit);
        rows.addChild(buttonsRow);

        this.updateButtonValidity();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();

        this.suggestions = new InputSuggestions(
            this.minecraft,
            this,
            this.inputEdit,
            this.font,
                0,
            7,
            true,
            0xd0000000
        );
        this.suggestions.setAllowSuggestions(true);
        this.suggestions.updateSuggestInfo();
    }

    protected Component getUsageNarration() {
        final var suggestions = this.suggestions;
        assert suggestions != null;
        return this.suggestions.isVisible()
            ? this.suggestions.getUsageNarration()
            : super.getUsageNarration();
    }

    public void resize(final Minecraft minecraft, final int width, final int height) {
        assert this.inputEdit != null;
        final String string = this.inputEdit.getValue();
        this.init(minecraft, width, height);
        this.inputEdit.setValue(string);

        final var suggestions = this.suggestions;
        assert suggestions != null;
        suggestions.updateSuggestInfo();
    }

    private void onDone() {
        assert this.list != null;
        this.callback.accept(this.list.build());
        this.onClose();
    }

    private void onEdited(final String value) {
        final var addButton = this.addButton;
        final var inputEdit = this.inputEdit;

        assert addButton != null;
        assert inputEdit != null;

        final var suggestions = this.suggestions;
        assert suggestions != null;
        addButton.active = suggestions.updateSuggestInfo();
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        final var suggestions = this.suggestions;
        assert suggestions != null;

        if (suggestions.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
            final var addButton = this.addButton;
            assert addButton != null;
            if (addButton.isActive()) {
                addButton.onPress();
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(double d, double e, double f, double g) {
        final var suggestions = this.suggestions;
        assert suggestions != null;
        return suggestions.mouseScrolled(g) || super.mouseScrolled(d, e, f, g);
    }

    public boolean mouseClicked(double d, double e, int i, boolean bl) {
        final var suggestions = this.suggestions;
        assert suggestions != null;
        return suggestions.mouseClicked(d, e, i) || super.mouseClicked(d, e, i, bl);
    }

    @Override
    public void onClose() {
        final var minecraft = this.minecraft;
        assert minecraft != null;
        minecraft.setScreen(this.lastScreen);
    }

    @Override
    protected void repositionElements() {
        if (this.list != null) {
            this.list.updateSize(this.width, this.layout);
        }

        this.layout.arrangeElements();
    }

    public void updateButtonValidity() {
        if (this.removeButton != null) {
            this.removeButton.active = this.hasValidSelection();
        }
    }

    private boolean hasValidSelection() {
        return this.list != null && this.list.getSelected() != null;
    }

    public void render(
        final GuiGraphics guiGraphics,
        final int mouseX,
        final int mouseY,
        final float deltaTicks
    ) {
        super.render(guiGraphics, mouseX, mouseY, deltaTicks);

        final var inputEdit = this.inputEdit;
        assert inputEdit != null;
        inputEdit.render(guiGraphics, mouseX, mouseY, deltaTicks);

        final var suggestions = this.suggestions;
        assert suggestions != null;
        suggestions.render(guiGraphics, mouseX, mouseY);
    }
}
