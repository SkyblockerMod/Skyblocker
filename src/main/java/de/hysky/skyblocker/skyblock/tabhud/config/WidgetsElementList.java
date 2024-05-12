package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.config.entries.WidgetsListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class WidgetsElementList extends ElementListWidget<WidgetsListEntry> {

    static final Identifier MOVE_UP_HIGHLIGHTED_TEXTURE = new Identifier("transferable_list/move_up_highlighted");
    static final Identifier MOVE_UP_TEXTURE = new Identifier("transferable_list/move_up");
    static final Identifier MOVE_DOWN_HIGHLIGHTED_TEXTURE = new Identifier("transferable_list/move_down_highlighted");
    static final Identifier MOVE_DOWN_TEXTURE = new Identifier("transferable_list/move_down");
    private final WidgetsOrderingTab parent;
    private boolean upArrowHovered = false;
    private boolean downArrowHovered = false;

    private int editingPosition = - 1;

    public WidgetsElementList(WidgetsOrderingTab parent, MinecraftClient minecraftClient, int width, int height, int y) {
        super(minecraftClient, width, height, y, 32);
        this.parent = parent;
    }


    @Override
    public void clearEntries() {
        super.clearEntries();
    }

    @Override
    public int addEntry(WidgetsListEntry entry) {
        return super.addEntry(entry);
    }

    private int x, y, entryWidth, entryHeight;

    @Override
    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderList(context, mouseX, mouseY, delta);
        WidgetsListEntry hoveredEntry = getHoveredEntry();
        if (hoveredEntry != null) hoveredEntry.renderTooltip(context, x, y, entryWidth, entryHeight, mouseX, mouseY);
    }

    @Override
    protected void renderEntry(DrawContext context, int mouseX, int mouseY, float delta, int index, int x, int y, int entryWidth, int entryHeight) {
        super.renderEntry(context, mouseX, mouseY, delta, index, x, y, entryWidth, entryHeight);
        if (index == editingPosition) {
            boolean xGood = mouseX >= x + entryWidth && mouseX < x + entryWidth + 15;
            upArrowHovered = xGood  && mouseY >= y && mouseY < y + entryHeight / 2;
            downArrowHovered = xGood && mouseY >= y + entryHeight / 2 && mouseY < y + entryHeight;
            context.drawGuiTexture(upArrowHovered ? MOVE_UP_HIGHLIGHTED_TEXTURE : MOVE_UP_TEXTURE, getRowRight() - 16, y, 32, 32);
            context.drawGuiTexture(downArrowHovered ? MOVE_DOWN_HIGHLIGHTED_TEXTURE : MOVE_DOWN_TEXTURE, getRowRight() - 16, y, 32, 32);
        }
        if (Objects.equals(getHoveredEntry(), getEntry(index))) {
            this.x = x;
            this.y = y;
            this.entryWidth = entryWidth;
            this.entryHeight = entryHeight;
        }
    }

    @Override
    protected int getScrollbarX() {
        return super.getScrollbarX() + (editingPosition != -1 ? 15 : 0);
    }

    @Override
    public int getRowWidth() {
        return 280;
    }

    public void setEditingPosition(int editingPosition) {
        this.editingPosition = editingPosition;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (upArrowHovered) {
            parent.shiftClickAndWaitForServer(13, 1);
            return true;
        }
        if (downArrowHovered) {
            parent.shiftClickAndWaitForServer(13, 0);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP && editingPosition != -1) {
            parent.clickAndWaitForServer(13, 1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DOWN && editingPosition != -1) {
            parent.clickAndWaitForServer(13, 0);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
