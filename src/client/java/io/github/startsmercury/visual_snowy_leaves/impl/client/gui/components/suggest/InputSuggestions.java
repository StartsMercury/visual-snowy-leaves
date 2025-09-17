package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.suggest;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.resource.ResourceLocationParseException;
import io.github.startsmercury.visual_snowy_leaves.impl.client.util.resource.ResourceLocationParser;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class InputSuggestions {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");

    @Nullable
    private FormattedCharSequence commandUsage;
    private final Screen screen;
    private int commandUsageWidth;

    final boolean anchorToBottom;
    final Font font;
    final int fillColor;
    final EditBox input;
    final Minecraft minecraft;
    final int lineStartOffset;
    final int suggestionLineLimit;

    private String currentValue = "";
    private @Nullable ResourceLocationParseException currentException;
    private @Nullable CompletableFuture<Suggestions> pendingSuggestions;
    private @Nullable SuggestionsList suggestions;
    private boolean allowSuggestions;
    boolean keepSuggestions;
    private boolean allowHiding = true;

    public InputSuggestions(
        final Minecraft minecraft,
        final Screen screen,
        final EditBox input,
        final Font font,
        final int lineStartOffset,
        final int suggestionLineLimit,
        final boolean anchorToBottom,
        final int fillColor
    ) {
        this.anchorToBottom = anchorToBottom;
        this.fillColor = fillColor;
        this.font = font;
        this.input = input;
        this.lineStartOffset = lineStartOffset;
        this.minecraft = minecraft;
        this.screen = screen;
        this.suggestionLineLimit = suggestionLineLimit;
    }

    public void setAllowSuggestions(final boolean allowSuggestions) {
        this.allowSuggestions = allowSuggestions;

        if (!allowSuggestions) {
            this.suggestions = null;
        }
    }

    public void setAllowHiding(final boolean allowHiding) {
        this.allowHiding = allowHiding;
    }

    public boolean keyPressed(final KeyEvent event) {
        if (this.suggestions != null && this.suggestions.keyPressed(event)) {
            return true;
        } else if (this.screen.getFocused() != this.input
            || !event.isCycleFocus()
            || this.allowHiding && this.suggestions == null
        ) {
            return false;
        } else {
            this.showSuggestions(true);
            return true;
        }
    }

    public boolean mouseScrolled(final double scrollAmount) {
        return this.suggestions != null
            && this.suggestions.mouseScrolled(Mth.clamp(scrollAmount, -1.0D, 1.0D));
    }

    public boolean mouseClicked(final MouseButtonEvent event) {
        return this.suggestions != null
            && this.suggestions.mouseClicked((int) event.x(), (int) event.y());
    }

    public void showSuggestions(final boolean bl) {
        if (!this.input.isFocused()) return;
        if (this.pendingSuggestions == null) return;
        if (!this.pendingSuggestions.isDone()) return;

        final var suggestions = this.pendingSuggestions.join();
        if (suggestions.isEmpty()) return;

        int width = 0;
        for(final var suggestion : suggestions.getList()) {
            width = Math.max(width, this.font.width(suggestion.getText()));
        }

        final int x = Mth.clamp(
            this.input.getScreenX(suggestions.getRange().getStart()),
            0,
            this.input.getScreenX(0) + this.input.getInnerWidth() - width
        );
        final int y = this.anchorToBottom
            ? this.input.getY()
            : this.input.getY() + this.input.getHeight();

        this.suggestions =
            new SuggestionsList(this, x, y, width, this.sortSuggestions(suggestions), bl);
    }

    public boolean isVisible() {
        return this.suggestions != null;
    }

    public Component getUsageNarration() {
        if (this.suggestions != null && this.suggestions.tabCycles) {
            return this.allowHiding
                ? Component.translatable("narration.suggestion.usage.cycle.hidable")
                : Component.translatable("narration.suggestion.usage.cycle.fixed");
        } else {
            return this.allowHiding
                ? Component.translatable("narration.suggestion.usage.fill.hidable")
                : Component.translatable("narration.suggestion.usage.fill.fixed");
        }
    }

    public void hide() {
        this.suggestions = null;
    }

    private List<Suggestion> sortSuggestions(final Suggestions suggestions) {
        final var cursorsLeft = this.input.getValue().substring(0, this.input.getCursorPosition());
        final var lastWord = cursorsLeft
            .substring(getLastWordIndex(cursorsLeft))
            .toLowerCase(Locale.ROOT);

        final var commonPrefix = Lists.<Suggestion>newArrayList();
        final var misc = Lists.<Suggestion>newArrayList();

        for (final var suggestion : suggestions.getList()) {
            if (suggestion.getText().startsWith(lastWord)
                || suggestion.getText().startsWith("minecraft:" + lastWord)
            ) {
                commonPrefix.add(suggestion);
            } else {
                misc.add(suggestion);
            }
        }

        commonPrefix.addAll(misc);
        return commonPrefix;
    }

    public boolean updateSuggestInfo() {
        final var value = this.input.getValue();
        if (!this.currentValue.equals(value)) {
            this.currentException = null;
        }

        if (!this.keepSuggestions) {
            this.input.setSuggestion(null);
            this.suggestions = null;
        }

        this.commandUsage = null;

        if (this.suggestions == null || !this.keepSuggestions) {
            this.currentValue = value;

            try {
                ResourceLocationParser.parse(value);
                this.input.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
            } catch (final ResourceLocationParseException cause) {
                this.currentException = cause;
                this.input.setTextColor(CommonColors.RED);
            }

            final var builder = new SuggestionsBuilder(value, 0);

            this.pendingSuggestions =
                SharedSuggestionProvider.suggestResource(BuiltInRegistries.BLOCK.keySet(), builder);

            this.pendingSuggestions.thenAccept(this::updateUsageInfo);
        }

        return this.currentException == null;
    }

    private static int getLastWordIndex(final String string) {
        if (Strings.isNullOrEmpty(string)) return 0;

        final var matcher = WHITESPACE_PATTERN.matcher(string);
        int i = 0;

        while (matcher.find()) {
            i = matcher.end();
        }

        return i;
    }

    private void updateUsageInfo(final Suggestions suggestions) {
        if (this.currentException != null && suggestions.isEmpty()) {
            this.commandUsage = Component.literal(this.currentException.getMessage()).getVisualOrderText();
        }

        this.commandUsageWidth = this.screen.width;

        this.suggestions = null;
        if (this.allowSuggestions && this.minecraft.options.autoSuggestions().get()) {
            this.showSuggestions(false);
        }
    }

    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        if (!this.renderSuggestions(guiGraphics, mouseX, mouseY)) {
            this.renderUsage(guiGraphics);
        }
    }

    public boolean renderSuggestions(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        if (this.suggestions != null) {
            this.suggestions.render(guiGraphics, mouseX, mouseY);
            return true;
        } else {
            return false;
        }
    }

    public void renderUsage(final GuiGraphics guiGraphics) {
        final var commandUsage = this.commandUsage;
        if (commandUsage == null) return;
        final var x = 0;
        final var y = this.anchorToBottom ? this.input.getY() + this.input.getHeight() : this.input.getY() - SuggestionsList.ITEM_HEIGHT;

        guiGraphics.fill(
            x - 1,
            y,
            x + this.commandUsageWidth + 1,
            y + SuggestionsList.ITEM_HEIGHT,
            this.fillColor
        );
        guiGraphics.drawString(this.font, commandUsage, x, y + 2, CommonColors.WHITE);
    }

    public Component getNarrationMessage() {
        return this.suggestions != null
            ? CommonComponents.NEW_LINE.copy().append(this.suggestions.getNarrationMessage())
            : CommonComponents.EMPTY;
    }
}
