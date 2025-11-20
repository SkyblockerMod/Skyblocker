package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.config.entries.WidgetsListEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.slot.WidgetsListSlotEntry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WidgetsElementList extends ElementListWidget<WidgetsListEntry> {
	static final Identifier MOVE_UP_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("transferable_list/move_up_highlighted");
	static final Identifier MOVE_UP_TEXTURE = Identifier.ofVanilla("transferable_list/move_up");
	static final Identifier MOVE_DOWN_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("transferable_list/move_down_highlighted");
	static final Identifier MOVE_DOWN_TEXTURE = Identifier.ofVanilla("transferable_list/move_down");

	static final WidgetsListEntry SEPARATOR = new WidgetsListEntry() {

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.of("- Skyblocker Widgets -"), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 9) / 2, Colors.WHITE);
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {}
	};

	private final WidgetsListTab parent;
	private boolean rightUpArrowHovered = false;
	private boolean rightDownArrowHovered = false;
	private boolean leftUpArrowHovered = false;
	private boolean leftDownArrowHovered = false;

	private int editingPosition = -1;

	public WidgetsElementList(WidgetsListTab parent, MinecraftClient minecraftClient, int width, int height, int y) {
		super(minecraftClient, width, height, y, 32);
		this.parent = parent;
	}

	@Override
	public @Nullable WidgetsListEntry getSelectedOrNull() {
		if (editingPosition < 0 || editingPosition >= this.children().size()) return null;
		return this.children().get(editingPosition);
	}

	@Override
	protected boolean isEntrySelectionAllowed() {
		return true;
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if (parent.listNeedsUpdate()) {
			ArrayList<Int2ObjectMap.Entry<WidgetsListSlotEntry>> entries = new ArrayList<>(parent.getEntries());
			clearEntries();
			entries.stream()
					.sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
					.map(Map.Entry::getValue)
					.forEach(this::addEntry);
			if (!parent.getCustomWidgetEntries().isEmpty() && parent.shouldShowCustomWidgetEntries()) {
				if (!children().isEmpty()) addEntry(SEPARATOR);
				parent.getCustomWidgetEntries().forEach(this::addEntry);
			}
			refreshScroll();
		}
		super.renderWidget(context, mouseX, mouseY, delta);
		WidgetsListEntry hoveredEntry = getHoveredEntry();
		if (hoveredEntry != null) hoveredEntry.renderTooltip(context, hoveredEntry.getX(), hoveredEntry.getY(), hoveredEntry.getWidth(), hoveredEntry.getHeight(), mouseX, mouseY);
		if (rightUpArrowHovered || rightDownArrowHovered) {
			context.drawTooltip(client.textRenderer, Text.literal("Move widget"), mouseX, mouseY);
		}
		if (leftUpArrowHovered || leftDownArrowHovered) {
			context.drawTooltip(client.textRenderer, Text.literal("Change selection"), mouseX, mouseY);
		}
	}

	@Override
	protected void renderEntry(DrawContext context, int mouseX, int mouseY, float delta, WidgetsListEntry entry) {
		super.renderEntry(context, mouseX, mouseY, delta, entry);
		if (this.getSelectedOrNull() != entry) return;

		int x = entry.getX();
		int y = entry.getY();
		int entryWidth = entry.getWidth();
		int entryHeight = entry.getHeight();

		boolean rightXGood = mouseX >= x + entryWidth && mouseX < x + entryWidth + 15;
		boolean leftXGood = mouseX >= x - 16 && mouseX < x - 1;
		boolean isOnUp = mouseY >= y && mouseY < y + entryHeight / 2;
		boolean isOnDown = mouseY >= y + entryHeight / 2 && mouseY < y + entryHeight;
		rightUpArrowHovered = rightXGood && isOnUp;
		rightDownArrowHovered = rightXGood && isOnDown;
		leftUpArrowHovered = leftXGood && isOnUp;
		leftDownArrowHovered = leftXGood && isOnDown;
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, rightUpArrowHovered ? MOVE_UP_HIGHLIGHTED_TEXTURE : MOVE_UP_TEXTURE, getRowRight() - 16, y, 32, 32);
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, rightDownArrowHovered ? MOVE_DOWN_HIGHLIGHTED_TEXTURE : MOVE_DOWN_TEXTURE, getRowRight() - 16, y, 32, 32);

		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, leftUpArrowHovered ? MOVE_UP_HIGHLIGHTED_TEXTURE : MOVE_UP_TEXTURE, x - 33, y, 32, 32);
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, leftDownArrowHovered ? MOVE_DOWN_HIGHLIGHTED_TEXTURE : MOVE_DOWN_TEXTURE, x - 33, y, 32, 32);
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
	public boolean mouseClicked(Click click, boolean doubled) {
		if (editingPosition == -1) return super.mouseClicked(click, doubled);
		if (rightUpArrowHovered) {
			parent.shiftClickAndWaitForServer(13, 1);
			return true;
		}
		if (rightDownArrowHovered) {
			parent.shiftClickAndWaitForServer(13, 0);
			return true;
		}
		if (leftUpArrowHovered) {
			parent.clickAndWaitForServer(13, 1);
			return true;
		}
		if (leftDownArrowHovered) {
			parent.clickAndWaitForServer(13, 0);
			return true;
		}
		return super.mouseClicked(click, doubled);
	}
}
