package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.config.entries.WidgetsListEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.slot.WidgetsListSlotEntry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

public class WidgetsElementList extends ContainerObjectSelectionList<WidgetsListEntry> {
	static final Identifier MOVE_UP_HIGHLIGHTED_TEXTURE = Identifier.withDefaultNamespace("transferable_list/move_up_highlighted");
	static final Identifier MOVE_UP_TEXTURE = Identifier.withDefaultNamespace("transferable_list/move_up");
	static final Identifier MOVE_DOWN_HIGHLIGHTED_TEXTURE = Identifier.withDefaultNamespace("transferable_list/move_down_highlighted");
	static final Identifier MOVE_DOWN_TEXTURE = Identifier.withDefaultNamespace("transferable_list/move_down");

	static final WidgetsListEntry SEPARATOR = new WidgetsListEntry() {

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			context.drawCenteredString(Minecraft.getInstance().font, Component.nullToEmpty("- Skyblocker Widgets -"), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 9) / 2, CommonColors.WHITE);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of();
		}

		@Override
		public void drawBorder(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {}
	};

	private final WidgetsListTab parent;
	private @Nullable Button backButton;
	private boolean rightUpArrowHovered = false;
	private boolean rightDownArrowHovered = false;
	private boolean leftUpArrowHovered = false;
	private boolean leftDownArrowHovered = false;

	private int editingPosition = -1;

	public WidgetsElementList(WidgetsListTab parent, Minecraft minecraftClient, int width, int height, int y) {
		super(minecraftClient, width, height, y, 32);
		this.parent = parent;
	}

	@Override
	public @Nullable WidgetsListEntry getSelected() {
		if (editingPosition < 0 || editingPosition >= this.children().size()) return null;
		return this.children().get(editingPosition);
	}

	@Override
	protected boolean entriesCanBeSelected() {
		return true;
	}

	@Override
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
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
			refreshScrollAmount();
		}
		super.renderWidget(context, mouseX, mouseY, delta);
		WidgetsListEntry hoveredEntry = getHovered();
		if (hoveredEntry != null) hoveredEntry.renderTooltip(context, hoveredEntry.getX(), hoveredEntry.getY(), hoveredEntry.getWidth(), hoveredEntry.getHeight(), mouseX, mouseY);
		if (rightUpArrowHovered || rightDownArrowHovered) {
			context.setTooltipForNextFrame(minecraft.font, Component.literal("Move widget"), mouseX, mouseY);
		}
		if (leftUpArrowHovered || leftDownArrowHovered) {
			context.setTooltipForNextFrame(minecraft.font, Component.literal("Change selection"), mouseX, mouseY);
		}
		if (backButton != null) backButton.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected void renderItem(GuiGraphics context, int mouseX, int mouseY, float delta, WidgetsListEntry entry) {
		super.renderItem(context, mouseX, mouseY, delta, entry);
		if (this.getSelected() != entry) return;

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
		context.blitSprite(RenderPipelines.GUI_TEXTURED, rightUpArrowHovered ? MOVE_UP_HIGHLIGHTED_TEXTURE : MOVE_UP_TEXTURE, getRowRight() - 16, y, 32, 32);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, rightDownArrowHovered ? MOVE_DOWN_HIGHLIGHTED_TEXTURE : MOVE_DOWN_TEXTURE, getRowRight() - 16, y, 32, 32);

		context.blitSprite(RenderPipelines.GUI_TEXTURED, leftUpArrowHovered ? MOVE_UP_HIGHLIGHTED_TEXTURE : MOVE_UP_TEXTURE, x - 33, y, 32, 32);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, leftDownArrowHovered ? MOVE_DOWN_HIGHLIGHTED_TEXTURE : MOVE_DOWN_TEXTURE, x - 33, y, 32, 32);
	}

	@Override
	protected int scrollBarX() {
		return super.scrollBarX() + (editingPosition != -1 ? 15 : 0);
	}

	@Override
	public int getRowWidth() {
		return 280;
	}

	public void setEditingPosition(int editingPosition) {
		this.editingPosition = editingPosition;
	}

	public void setBackButton(Button backButton) {
		this.backButton = backButton;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (backButton != null && backButton.mouseClicked(click, doubled)) return true;
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
