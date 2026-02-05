package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.config.entries.WidgetsListEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.slot.WidgetSlotEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.slot.WidgetsListSlotEntry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class WidgetsElementList extends ContainerObjectSelectionList<WidgetsListEntry> {
	static final Identifier MOVE_UP_HIGHLIGHTED_TEXTURE = Identifier.withDefaultNamespace("transferable_list/move_up_highlighted");
	static final Identifier MOVE_UP_TEXTURE = Identifier.withDefaultNamespace("transferable_list/move_up");
	static final Identifier MOVE_DOWN_HIGHLIGHTED_TEXTURE = Identifier.withDefaultNamespace("transferable_list/move_down_highlighted");
	static final Identifier MOVE_DOWN_TEXTURE = Identifier.withDefaultNamespace("transferable_list/move_down");

	final int SELECT_COLOR = 0x761111FF;
	final int MOVE_COLOR = 0x76FF3333;

	private final WidgetsListTab parent;
	private @Nullable Button backButton;
	private boolean rightUpArrowHovered = false;
	private boolean rightDownArrowHovered = false;
	private boolean leftUpArrowHovered = false;
	private boolean leftDownArrowHovered = false;

	private boolean enableEditing = false;
	public static int editingPosition = -1;
	public static int maxPosition = -1;
	public static boolean isOnSecondPage = false;

	public WidgetsElementList(WidgetsListTab parent, Minecraft minecraftClient, int width, int height, int y) {
		super(minecraftClient, width, height, y, 32);
		this.parent = parent;
	}

	@Override
	public @Nullable WidgetsListEntry getSelected() {
		if (!enableEditing) return super.getSelected();
		if (editingPosition < 0 || editingPosition >= this.children().size()) return null;
		WidgetsListEntry entry = this.children().get(editingPosition);
		if (!(entry instanceof WidgetSlotEntry widgetSlotEntry) || widgetSlotEntry.getState() != WidgetSlotEntry.State.ENABLED) return null;
		return entry;
	}

	@Override
	protected boolean entriesCanBeSelected() {
		return true;
	}

	@Override
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderWidget(context, mouseX, mouseY, delta);
		WidgetsListEntry hoveredEntry = getHovered();
		if (hoveredEntry != null) {
			hoveredEntry.renderTooltip(context, hoveredEntry.getX(), hoveredEntry.getY(), hoveredEntry.getWidth(), hoveredEntry.getHeight(), mouseX, mouseY);
		}
		if (backButton != null) {
			backButton.render(context, mouseX, mouseY, delta);
		}

		if (!enableEditing || this.getSelected() == null) return;
		if (rightUpArrowHovered || rightDownArrowHovered) {
			context.setTooltipForNextFrame(minecraft.font, Component.literal("Move widget"), mouseX, mouseY);
		}

		if (leftUpArrowHovered || leftDownArrowHovered) {
			context.setTooltipForNextFrame(minecraft.font, Component.literal("Change selection"), mouseX, mouseY);
		}
	}

	@Override
	protected void renderItem(GuiGraphics context, int mouseX, int mouseY, float delta, WidgetsListEntry entry) {
		super.renderItem(context, mouseX, mouseY, delta, entry);
		if (!enableEditing || this.getSelected() != entry) return;

		int x = entry.getX();
		int y = entry.getY();
		int entryHeight = entry.getHeight();
		int halfHeight = entryHeight / 2;

		boolean rightXGood = mouseX >= getRowRight() + 2 && mouseX < getRowRight() + 15;
		boolean leftXGood = mouseX >= x - 15 && mouseX < x - 2;
		boolean isOnUp = mouseY >= y + 5 && mouseY < y + halfHeight;
		boolean isOnDown = mouseY >= y + halfHeight && mouseY < y + entryHeight - 5;
		rightUpArrowHovered = rightXGood && isOnUp;
		rightDownArrowHovered = rightXGood && isOnDown;
		leftUpArrowHovered = leftXGood && isOnUp;
		leftDownArrowHovered = leftXGood && isOnDown;
		context.fill(getRowRight() + 2, y + 5, getRowRight() + 15, y + 27, MOVE_COLOR);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, rightUpArrowHovered ? MOVE_UP_HIGHLIGHTED_TEXTURE : MOVE_UP_TEXTURE, getRowRight() - 15, y, 32, 32);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, rightDownArrowHovered ? MOVE_DOWN_HIGHLIGHTED_TEXTURE : MOVE_DOWN_TEXTURE, getRowRight() - 15, y, 32, 32);

		context.fill(x - 15, y + 5, x - 2, y + 27, SELECT_COLOR);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, leftUpArrowHovered ? MOVE_UP_HIGHLIGHTED_TEXTURE : MOVE_UP_TEXTURE, x - 32, y, 32, 32);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, leftDownArrowHovered ? MOVE_DOWN_HIGHLIGHTED_TEXTURE : MOVE_DOWN_TEXTURE, x - 32, y, 32, 32);
	}

	@Override
	protected int scrollBarX() {
		return super.scrollBarX() + (editingPosition != -1 ? 15 : 0);
	}

	@Override
	public int getRowWidth() {
		return 280;
	}

	public void setEditingPosition(boolean enable, int editing, int max) {
		this.enableEditing = enable;
		editingPosition = editing;
		maxPosition = max;
	}

	public void setIsOnSecondPage(boolean secondPage) {
		isOnSecondPage = secondPage;
		if (secondPage) {
			editingPosition -= 21;
			refreshScrollAmount();
		}
	}

	public void setBackButton(Button backButton) {
		this.backButton = backButton;
	}

	public void updateList() {
		ArrayList<Int2ObjectMap.Entry<WidgetsListSlotEntry>> entries = new ArrayList<>(parent.getEntries());
		clearEntries();
		entries.stream()
				.sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
				.map(Map.Entry::getValue)
				.forEach(this::addEntry);
		if (!parent.getCustomWidgetEntries().isEmpty() && parent.shouldShowCustomWidgetEntries()) {
			if (!children().isEmpty()) addEntry(new SeparatorEntry());
			parent.getCustomWidgetEntries().forEach(this::addEntry);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (backButton != null && backButton.mouseClicked(click, doubled)) return true;
		if (!enableEditing || this.getSelected() == null) return super.mouseClicked(click, doubled);
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
