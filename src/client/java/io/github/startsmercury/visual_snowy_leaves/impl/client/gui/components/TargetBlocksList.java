package io.github.startsmercury.visual_snowy_leaves.impl.client.gui.components;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.startsmercury.visual_snowy_leaves.impl.client.gui.screens.TargetBlocksScreen;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class TargetBlocksList extends ObjectSelectionList<TargetBlocksList.Entry> {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");

    public class Entry extends ObjectSelectionList.Entry<Entry> {
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
            final var id = TargetBlocksList.this.targets.get(index);

            BuiltInRegistries.BLOCK.getOptional(id).ifPresentOrElse(block -> {
                final var item = block.asItem();
                final var stack = new ItemStack(item);
                this.blitSlot(guiGraphics, rowLeft, rowTop, stack);

                final var name = item != Items.AIR ? stack.getHoverName() : block.getName();
                final var color = stack.getRarity().color().getColor();
                guiGraphics.drawString(TargetBlocksList.this.font, name, rowLeft + 18 + 5, rowTop + 1, color == null ? CommonColors.WHITE : 0xFF000000 | color);
                guiGraphics.drawString(TargetBlocksList.this.font, id.toString(), rowLeft + 18 + 5, rowTop + 11, CommonColors.GRAY);
            }, () -> {
                this.blitSlotBg(guiGraphics, rowLeft + 1, rowTop + 1);
                final var name = new ItemStack(Items.AIR).getHoverName();
                guiGraphics.drawString(TargetBlocksList.this.font, name, rowLeft + 18 + 5, rowTop + 1, CommonColors.DARK_GRAY);
                guiGraphics.drawString(TargetBlocksList.this.font, id.toString(), rowLeft + 18 + 5, rowTop + 11, CommonColors.GRAY);
            });
        }

        @Override
        public Component getNarration() {
            final var id = TargetBlocksList.this.targets.get(TargetBlocksList.this.children().indexOf(this));

            final var name = BuiltInRegistries.BLOCK.getOptional(id).map(block -> {
                final var item = block.asItem();
                final var stack = new ItemStack(item);

                return item != Items.AIR ? stack.getHoverName() : block.getName();
            }).orElseGet(() -> Component.literal(id.toString()));

            return Component.translatable("narrator.select", name);
        }

        @Override
        public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
            if (keyCode != InputConstants.KEY_DELETE) return false;
            return TargetBlocksList.this.removeKeyAt(this);
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            TargetBlocksList.this.setSelected(this);
            return super.mouseClicked(mouseX, mouseY, button);
        }

        private void blitSlot(final GuiGraphics guiGraphics, final int x, final int y, final ItemStack itemStack) {
            this.blitSlotBg(guiGraphics, x + 1, y + 1);
            if (itemStack.isEmpty()) return;
            guiGraphics.renderFakeItem(itemStack, x + 2, y + 2);
        }

        private void blitSlotBg(final GuiGraphics guiGraphics, final int x, final int y) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, x, y, 18, 18);
        }
    }

    private final ObjectArrayList<ResourceLocation> targets = new ObjectArrayList<>();

    private final Font font;

    private final TargetBlocksScreen screen;

    public TargetBlocksList(
        final TargetBlocksScreen screen,
        final Minecraft minecraft,
        final Font font,
        final int width,
        final int height,
        final int y,
        final int itemHeight,
        final int headerHeight
    ) {
        super(minecraft, width, height, y, itemHeight, headerHeight);

        this.screen = screen;
        this.font = font;
    }

    public Set<ResourceLocation> build() {
        return Set.copyOf(this.targets);
    }

    public boolean addKey(final ResourceLocation key) {
        if (this.targets.add(key)) {
            this.addEntry(new Entry());
            return true;
        } else {
            return false;
        }
    }

    public boolean removeSelectedKey() {
        final var selected = this.getSelected();
        return selected != null && this.removeKeyAt(selected);
    }

    private boolean removeKeyAt(final Entry entry) {
        entry.setFocused(false);
        return this.removeKeyAt(this.children().indexOf(entry));
    }

    private boolean removeKeyAt(final int index) {
        if (index < 0) {
            return false;
        }
        this.targets.remove(index);
        this.children().removeLast();

        final var n = this.children().size();
        if (n == 0) {
            this.setSelected(null);
        } else {
            final var entry = getEntry(index < n ? index : index - 1);
            centerScrollOn(entry);
            this.setSelected(entry);
        }

        return true;
    }

    @Override
    public void setSelected(final @Nullable Entry entry) {
        super.setSelected(entry);
        this.screen.updateButtonValidity();
    }
}
