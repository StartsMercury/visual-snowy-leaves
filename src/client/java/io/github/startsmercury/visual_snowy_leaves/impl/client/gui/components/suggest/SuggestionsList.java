package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components.suggest;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.suggestion.Suggestion;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.util.CommonColors;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

public class SuggestionsList {
    static final int ITEM_HEIGHT = 12;

    private final InputSuggestions suggestions;

    private final Rect2i rect;
    private final String originalContents;
    private final List<Suggestion> suggestionList;
    private int offset;
    private int current;
    private Vec2 lastMouse;
    boolean tabCycles;
    private int lastNarratedEntry;

    public SuggestionsList(
        final InputSuggestions suggestions,
        final int x,
        final int y,
        final int width,
        final List<Suggestion> suggestionList,
        final boolean bl
    ) {
        this.lastMouse = Vec2.ZERO;
        this.lastNarratedEntry = bl ? -1 : 0;
        this.originalContents = suggestions.input.getValue();

        final var inputBordered = suggestions.input.isBordered();
        final var height = ITEM_HEIGHT
            * Math.min(suggestionList.size(), suggestions.suggestionLineLimit);
        this.rect = new Rect2i(
            inputBordered ? x : x - 1,
            suggestions.anchorToBottom ? y - 3 - height : (inputBordered ? y - 1 : y),
            width + 1,
            height
        );

        this.suggestions = suggestions;
        this.suggestionList = suggestionList;
        this.select(0);
    }

    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        final var visibleItems =
            Math.min(this.suggestionList.size(), this.suggestions.suggestionLineLimit);

        final var mouseMoved =
            this.lastMouse.x != (float) mouseX || this.lastMouse.y != (float) mouseY;
        if (mouseMoved) {
            this.lastMouse = new Vec2((float) mouseX, (float) mouseY);
        }

        final var upperDashedLines = this.offset > 0;
        final var bottomDashedLines = this.suggestionList.size() > this.offset + visibleItems;
        if (upperDashedLines || bottomDashedLines) {
            guiGraphics.fill(
                this.rect.getX(),
                this.rect.getY() - 1,
                this.rect.getX() + this.rect.getWidth(),
                this.rect.getY(),
                this.suggestions.fillColor
            );
            guiGraphics.fill(
                this.rect.getX(),
                this.rect.getY() + this.rect.getHeight(),
                this.rect.getX() + this.rect.getWidth(),
                this.rect.getY() + this.rect.getHeight() + 1,
                this.suggestions.fillColor
            );

            if (upperDashedLines) {
                for (var i = 0; i < this.rect.getWidth(); ++i) {
                    if (i % 2 != 0) continue;
                    guiGraphics.fill(
                        this.rect.getX() + i,
                        this.rect.getY() - 1,
                        this.rect.getX() + i + 1,
                        this.rect.getY(),
                        CommonColors.WHITE
                    );
                }
            }

            if (bottomDashedLines) {
                for (var i = 0; i < this.rect.getWidth(); ++i) {
                    if (i % 2 != 0) continue;
                    guiGraphics.fill(
                        this.rect.getX() + i,
                        this.rect.getY() + this.rect.getHeight(),
                        this.rect.getX() + i + 1,
                        this.rect.getY() + this.rect.getHeight() + 1,
                        CommonColors.WHITE
                    );
                }
            }
        }

        var containsMouse = false;
        for (var i = 0; i < visibleItems; ++i) {
            final var suggestion = this.suggestionList.get(i + this.offset);

            final var x1 = this.rect.getX();
            final var y1 = this.rect.getY() + i * ITEM_HEIGHT;
            final var x2 = this.rect.getX() + this.rect.getWidth();
            final var y2 = this.rect.getY() + (i + 1) * ITEM_HEIGHT;

            guiGraphics.fill(x1, y1, x2, y2, this.suggestions.fillColor);

            if (x1 < mouseX && mouseX < x2 && y1 < mouseY && mouseY < y2) {
                if (mouseMoved) {
                    this.select(i + this.offset);
                }

                containsMouse = true;
            }

            guiGraphics.drawString(
                this.suggestions.font,
                suggestion.getText(),
                this.rect.getX() + 1,
                this.rect.getY() + 2 + i * ITEM_HEIGHT,
                i + this.offset == this.current ? CommonColors.YELLOW : 0xffaaaaaa // = ChatFormatting.GRAY | 0xff000000
            );
        }

        if (containsMouse) {
            final var message = this.suggestionList.get(this.current).getTooltip();

            if (message != null) {
                guiGraphics.setTooltipForNextFrame(
                    this.suggestions.font,
                    ComponentUtils.fromMessage(message),
                    mouseX,
                    mouseY
                );
            }
        }
    }

    public boolean mouseClicked(final int mouseX, final int mouseY, final int button) {
        if (!this.rect.contains(mouseX, mouseY)) return false;

        final var i = (mouseY - this.rect.getY()) / ITEM_HEIGHT + this.offset;
        if (i >= 0 && i < this.suggestionList.size()) {
            this.select(i);
            this.useSuggestion();
        }

        return true;
    }

    public boolean mouseScrolled(final double scrollAmount) {
        final var minecraft = this.suggestions.minecraft;
        final var mouseHandler = minecraft.mouseHandler;
        final var window = minecraft.getWindow();

        final var mouseX = (int) mouseHandler.getScaledXPos(window);
        final var mouseY = (int) mouseHandler.getScaledYPos(window);

        if (this.rect.contains(mouseX, mouseY)) {
            this.setOffset((int) (this.offset - scrollAmount));
            return true;
        } else {
            return false;
        }
    }

    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        switch (keyCode) {
            case InputConstants.KEY_UP -> {
                this.cycle(-1);
                this.tabCycles = false;
            }
            case InputConstants.KEY_DOWN -> {
                this.cycle(1);
                this.tabCycles = false;
            }
            case InputConstants.KEY_TAB -> {
                if (this.tabCycles) {
                    this.cycle(Screen.hasShiftDown() ? -1 : 1);
                }

                this.useSuggestion();
            }
            case InputConstants.KEY_ESCAPE -> {
                this.suggestions.hide();
                this.suggestions.input.setSuggestion(null);
            }
            default -> {
                return false;
            }
        }

        return true;
    }

    public void cycle(final int i) {
        this.select(this.current + i);

        final var j = this.offset;
        final var k = this.offset + this.suggestions.suggestionLineLimit - 1;

        if (this.current - 1 < j) {
            this.setOffset(this.current - 1);
        } else if (this.current + 1 > k) {
            this.setOffset(
                this.current
                    + this.suggestions.lineStartOffset
                    - this.suggestions.suggestionLineLimit
                    + 2
            );
        }
    }

    private void setOffset(final int offset) {
        this.offset = Math.clamp(
            offset,
            0,
            Math.max(this.suggestionList.size() - this.suggestions.suggestionLineLimit, 0)
        );
    }

    public void select(final int i) {
        this.current = i;

        if (this.current < 0) {
            this.current += this.suggestionList.size();
        }

        if (this.current >= this.suggestionList.size()) {
            this.current -= this.suggestionList.size();
        }

        final var suggestion = this.suggestionList.get(this.current);

        this.suggestions.input.setSuggestion(calculateSuggestionSuffix(
            this.suggestions.input.getValue(),
            suggestion.apply(this.originalContents)
        ));

        if (this.lastNarratedEntry != this.current) {
            this.suggestions.minecraft.getNarrator().saySystemNow(this.getNarrationMessage());
        }
    }

    @Nullable
    private static String calculateSuggestionSuffix(final String prefix, final String suggestion) {
        return suggestion.startsWith(prefix) ? suggestion.substring(prefix.length()) : null;
    }

    public void useSuggestion() {
        final var suggestion = this.suggestionList.get(this.current);
        this.suggestions.keepSuggestions = true;
        this.suggestions.input.setValue(suggestion.apply(this.originalContents));
        final var i = suggestion.getRange().getStart() + suggestion.getText().length();
        this.suggestions.input.setCursorPosition(i);
        this.suggestions.input.setHighlightPos(i);
        this.select(this.current);
        this.suggestions.keepSuggestions = false;
        this.tabCycles = true;
    }

    Component getNarrationMessage() {
        this.lastNarratedEntry = this.current;
        final var suggestion = this.suggestionList.get(this.current);
        final var message = suggestion.getTooltip();
        if (message != null) {
            return Component.translatable(
                "narration.suggestion.tooltip",
                this.current + 1,
                this.suggestionList.size(),
                suggestion.getText(),
                Component.translationArg(message)
            );
        } else {
            return Component.translatable(
                "narration.suggestion",
                this.current + 1,
                this.suggestionList.size(),
                suggestion.getText()
            );
        }
    }
}
