package me.xmrvizzy.skyblocker.skyblock.itemlist;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@Environment(value= EnvType.CLIENT)
public class ItemListWidget extends AbstractParentElement implements Drawable, Selectable {
    private static final List<Entry> entries = new ArrayList<>();
    private static String searchString = "";
    private static double scroll = 0.0;

    private final int gridX;
    private final int gridY;
    private final int rows;
    private final int cols;

    private double maxScroll;
    private boolean isHovered;

    private final HandledScreen screen;
    private final MinecraftClient client;
    private final List<Element> children = Lists.newArrayList();
    private final List<Selectable> selectables = Lists.newArrayList();
    private final List<Drawable> drawables = Lists.newArrayList();

    public ItemListWidget(HandledScreen screen) {
        this.screen = screen;
        this.client = MinecraftClient.getInstance();

        this.cols = (screen.width - 200) / 2 / 16;
        this.rows = (screen.height - 40) / 16;
        this.gridX = 8;
        this.gridY = 40;

        this.maxScroll = Math.max(0, entries.size() / this.cols - this.rows + 1);

        SearchWidget search = new SearchWidget(this.client.textRenderer, 8, 8, this.cols * 16);
        search.setText(searchString);
        search.setChangedListener(this::setSearch);
        this.addDrawableChild(search);

        this.setSearch(searchString);
    }

    private void setSearch(String search) {
        searchString = search;
        entries.clear();
        search = search.toLowerCase();
        for (Entry entry : ItemRegistry.registry) {
            String name = entry.itemStack.getName().toString().toLowerCase();
            String lore = entry.itemStack.getNbt().toString().toLowerCase();
            if (name.contains(search) || lore.contains(search))
                entries.add(entry);
        }
        this.maxScroll = Math.max(0, entries.size() / this.cols - this.rows + 1);
        scroll = Math.min(scroll, this.maxScroll);
    }

    protected <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
        this.drawables.add(drawableElement);
        return this.addSelectableChild(drawableElement);
    }

    protected <T extends Drawable> T addDrawable(T drawable) {
        this.drawables.add(drawable);
        return drawable;
    }

    protected <T extends Element & Selectable> T addSelectableChild(T child) {
        this.children.add(child);
        this.selectables.add(child);
        return child;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.isHovered = this.isMouseOver(mouseX, mouseY);
        ItemRenderer itemRenderer = client.getItemRenderer();
        RenderSystem.disableDepthTest();
        // slot hover
        int mouseOverIndex = getMouseOverIndex(mouseX, mouseY);
        if (mouseOverIndex != -1) {
            int x = this.gridX + mouseOverIndex % this.cols * 16;
            int y = this.gridY + (mouseOverIndex / this.cols - (int)scroll) * 16;
            fill(matrices, x, y, x + 16, y + 16, 0x20ffffff);
        }
        // item list
        for (int i = 0; i < rows; ++i)
            for (int j = 0; j < cols; ++j) {
                int index = (i + (int)scroll) * cols + j;
                if (index < entries.size()) {
                    int x = gridX + j * 16;
                    int y = gridY + i * 16;
                    itemRenderer.renderInGui(entries.get(index).itemStack, x, y);
                }
            }
        // item tooltip
        if (mouseOverIndex != -1) {
            ItemStack stack = entries.get(mouseOverIndex).itemStack;
            List<Text> tooltip = this.screen.getTooltipFromItem(stack);
            this.screen.renderTooltip(matrices, tooltip, mouseX, mouseY);
        }
        RenderSystem.enableDepthTest();
        // render children
        for (Drawable drawable : this.drawables) {
            drawable.render(matrices, mouseX, mouseY, delta);
        }
    }

    private boolean isMouseOverList(double mouseX, double mouseY) {
        return gridX <= mouseX && mouseX < gridX + cols * 16 && gridY <= mouseY && mouseY < gridY + rows * 16;
    }

    private int getMouseOverIndex(double mouseX, double mouseY) {
        if (isMouseOverList(mouseX, mouseY)) {
            int i = (int)(mouseY - this.gridY) / 16;
            int j = (int)(mouseX - this.gridX) / 16;
            int index = (i + (int)scroll) * this.cols + j;
            if (index < entries.size()) return index;
        }
        return -1;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        for (Element child : this.children) {
            if (child.isMouseOver(mouseX, mouseY)) return true;
        }
        return this.isMouseOverList(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (super.mouseScrolled(mouseX, mouseY, amount)) {
            return true;
        } else if (this.isMouseOverList(mouseX, mouseY)) {
            scroll = Math.min(this.maxScroll, Math.max(0.0, scroll - amount));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        return this.getFocused() instanceof TextFieldWidget && ((TextFieldWidget) this.getFocused()).isFocused() && keyCode != 256;
    }

    @Override
    public List<? extends Element> children() {
        return this.children;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        int index = getMouseOverIndex(mouseX, mouseY);
        if (index != -1 && entries.get(index).clickCommand != null) {
            this.client.player.sendChatMessage(entries.get(index).clickCommand);
            return true;
        }
        return false;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public SelectionType getType() {
        for (Selectable selectable : selectables)
            if (selectable.getType().isFocused()) return SelectionType.FOCUSED;
        if (this.isHovered) return SelectionType.HOVERED;
        return SelectionType.NONE;
    }
}
