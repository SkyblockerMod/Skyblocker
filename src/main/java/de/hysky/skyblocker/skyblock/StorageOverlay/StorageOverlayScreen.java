package de.hysky.skyblocker.skyblock.StorageOverlay;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.background.ItemBackgroundManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.skyblock.item.tooltip.BackpackPreview;
import de.hysky.skyblocker.utils.render.gui.SearchableGridWidget;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static de.hysky.skyblocker.skyblock.item.tooltip.BackpackPreview.getStorageIndexFromTitle;


public class StorageOverlayScreen extends AbstractContainerScreen<StorageOverlayScreenHandler> {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
	private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("social_interactions/background");
	//inventory texture key values
	private static final int HEADER_H = 17;
	private static final int SLOT_SIZE = 18;
	private static final int MAX_ROW_BATCH = 6; // texture body only has 6 rows
	private static final int MAX_COL_BATCH = 9; // texture body only has 9 cols
	private static final int BOTTOM_V = 215;
	private static final int EDGE_PADDING = 7;

	protected static int openStorage;
	private static double savedScroll = 0;
	private static String savedSearch = "";
	private static backpackGridWidget grid;
	private final StorageOverlayScreenHandler handler;
	private final Component name;
	private final ChestMenu defaultHandler;

	public StorageOverlayScreen(StorageOverlayScreenHandler handler, ChestMenu defaultHandler, Component name, Inventory inventory, int height) {
		super(handler, inventory, name, 176, height);
		this.titleLabelY = -1000; //let title exist for backpack previews caching to work.
		this.handler = handler;
		this.defaultHandler = defaultHandler;
		this.name = name;
	}

	public static Boolean enabled(String title) {
		openStorage = getStorageIndexFromTitle(title);

		return SkyblockerConfigManager.get().uiAndVisuals.storageOverlay.enabled && (title.equals("storage") || openStorage != -1);
	}

	protected static void switchOpenStorage(int index) {
		savedScroll = grid.getScrollAmount();
		if (index <= 8) {
			MessageScheduler.INSTANCE.sendMessageAfterCooldown("/echest " + (index + 1), true);
		} else {
			MessageScheduler.INSTANCE.sendMessageAfterCooldown("/backpack " + (index - 8), true);
		}
	}

	protected static String getStorageName(int index) {
		if (index <= 8) {
			return "Ender Chest " + (index + 1);
		} else {
			return "Backpack " + (index - 8);
		}
	}

	/**
	 * Temporarily hide the overlay and show normal backpack
	 *
	 * @param button button
	 */
	private void hide(Button button) {
		if (CLIENT.player == null) return;
		CLIENT.player.containerMenu = defaultHandler;
		CLIENT.gui.setScreen(new ContainerScreen(defaultHandler, CLIENT.player.getInventory(), name));
	}


	private int getLeftPos() {
		return this.width / 6;
	}

	private int getWidth() {
		return (this.width / 3) * 2;
	}

	private int getHeight() {
		return this.imageHeight - 87;
	}


	@Override
	protected void init() {
		super.init();

		//setup backpack widgets
		int internalCols = SkyblockerConfigManager.get().uiAndVisuals.storageOverlay.backpackWidth;
		grid = new backpackGridWidget(getLeftPos() + 8, this.topPos + 8, getWidth() - 16, getHeight() - 16, internalCols, handler, this.leftPos, this.topPos);
		grid.setSearch(savedSearch);
		grid.setScrollAmount(savedScroll);
		this.addRenderableWidget(grid);

		//add button to temperately disable menu
		addRenderableWidget(Button.builder(Component.translatable("skyblocker.config.uiAndVisuals.storageOverlay.hideButton"), this::hide)
				.tooltip(Tooltip.create(Component.translatable("skyblocker.config.uiAndVisuals.radialMenu.hideButton.@Tooltip")))
				.pos(width - 50, height - 25)
				.size(40, 15)
				.build());

	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);
		this.extractTooltip(graphics, mouseX, mouseY);
	}

	@Override
	public void onClose() {
		if (SkyblockerConfigManager.get().uiAndVisuals.storageOverlay.rememberSearch) {
			savedScroll = grid.getScrollAmount();

		} else {
			savedSearch = "";
			savedScroll = 0;
		}
		super.onClose();
	}

	@Override
	protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
		if (slot.container instanceof Inventory) {
			super.extractSlot(graphics, slot, mouseX, mouseY);
		} else {
			//keep backpack slots within gui
			graphics.enableScissor(-this.leftPos, 28, getWidth(), getHeight() - 8);
			super.extractSlot(graphics, slot, mouseX, mouseY);
			graphics.disableScissor();
		}

	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int xo, int yo) {
		return super.hasClickedOutside(mouseX, mouseY, xo, yo) && (mouseX < getLeftPos() || mouseX > getLeftPos() + width || mouseY < this.topPos || mouseY > this.topPos + getHeight());
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		//let scrolling happen if not scrolling item slot
		if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
			return super.mouseScrolled(x, y, scrollX, scrollY);
		}
		return this.getChildAt(x, y).filter(child -> child.mouseScrolled(x, y, scrollX, scrollY)).isPresent();

	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		//render background
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, getLeftPos(), this.topPos, getWidth(), getHeight());
		//render inventory
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos + this.imageHeight - 94, 0, 128, 175, 94, 256, 256);
	}

	private static class backpackGridWidget extends SearchableGridWidget {

		private final List<backpackWidget> backpackWidgets = new ArrayList<>();
		@Nullable
		private backpackWidget openBackpack = null;

		backpackGridWidget(int x, int y, int width, int height, int internalCols, StorageOverlayScreenHandler handler, int screenLeft, int screenTop) {
			// cut down number of columns if it will not fit on to the current gui size
			int expectedWidth = internalCols * SLOT_SIZE + EDGE_PADDING * 2;
			while (expectedWidth > width - 6) {
				expectedWidth = --internalCols * SLOT_SIZE + EDGE_PADDING * 2;
			}

			super(x, y, width, height, Component.literal("BackPack grid"), expectedWidth, true);

			//add backpacks
			BackpackPreview.Storage[] storages = BackpackPreview.getStorages();
			for (int i = 0; i < storages.length; i++) {
				BackpackPreview.Storage storage = storages[i];
				boolean open = StorageOverlayScreen.openStorage == i;
				if (storage != null) {
					backpackWidget widget = new backpackWidget(internalCols, i, storage, open, handler, screenLeft, screenTop);
					if (open) {
						openBackpack = widget;
					}
					backpackWidgets.add(widget);
				}
			}
		}

		@Override
		protected Collection<? extends AbstractWidget> filterWidgets(String input) {
			savedSearch = input;
			return backpackWidgets.stream().filter(backpack -> backpack.matches(input)).toList();
		}

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			//let the mouse go though gui if the user is interacting with open backpack
			if (openBackpack != null && openBackpack.isMouseOver(mouseX, mouseY)) {
				return false;
			}
			return super.isMouseOver(mouseX, mouseY);
		}

		@Override
		protected double scrollRate() {
			return 15;
		}
	}


	private static class backpackWidget extends AbstractWidget {
		private final int rows;
		private final int columns;
		private final String label;
		private final int index;
		private final BackpackPreview.Storage storage;
		private final boolean open;
		private final StorageOverlayScreenHandler handler;
		private final int screenLeft;
		private final int screenTop;

		backpackWidget(int columns, int index, BackpackPreview.Storage storage, Boolean open, StorageOverlayScreenHandler handler, int screenLeft, int screenTop) {
			int rows = Math.ceilDiv(storage.size() - 9, columns);
			super(0, 0, columns * SLOT_SIZE + EDGE_PADDING * 2, rows * SLOT_SIZE + HEADER_H + EDGE_PADDING, Component.literal("Backpack preview"));
			this.rows = rows;
			this.columns = columns;
			this.label = getStorageName(index);
			this.index = index;
			this.storage = storage;
			this.open = open;
			this.handler = handler;
			this.screenLeft = screenLeft;
			this.screenTop = screenTop;
		}

		public boolean matches(String filter) {
			//keep open backpack displayed even if no match
			if (open) return true;

			//matches if any item in backpack contains the filter word
			for (int i = 9; i < storage.size(); ++i) {
				ItemStack currentStack = storage.getStack(i);
				if (currentStack.getDisplayName().getString().toLowerCase(Locale.ENGLISH).contains(filter.toLowerCase(Locale.ENGLISH))) {
					return true;
				}
			}
			return false;
		}


		private void CustomInventorySize(GuiGraphicsExtractor graphics, int x, int y, int rows, int columns) {
			//top
			extractSection(graphics, x, y, columns, HEADER_H, 0);

			//slots split into batches of up to 6 rows
			int rowsRemaining = rows;
			while (rowsRemaining > 0) {
				int rowBatch = Math.min(rowsRemaining, MAX_ROW_BATCH);
				int batchH = rowBatch * SLOT_SIZE;
				int rowY = y + HEADER_H + (rows - rowsRemaining) * SLOT_SIZE;

				extractSection(graphics, x, rowY, columns, batchH, HEADER_H);

				rowsRemaining -= rowBatch;
			}

			//bottom
			int bottomY = y + HEADER_H + rows * SLOT_SIZE;
			extractSection(graphics, x, bottomY, columns, EDGE_PADDING, BOTTOM_V);
		}

		private void extractSection(GuiGraphicsExtractor graphics, int x, int y, int columns, int height, int textureYOffset) {
			//blit middle separated into 9 cols (MAX_COL_BATCH) as that is the most that will fit in the texture
			int columnsRemaining = columns;
			while (columnsRemaining > 0) {
				int batch = Math.min(columnsRemaining, MAX_COL_BATCH);
				int batchX = (columns - columnsRemaining) * SLOT_SIZE + EDGE_PADDING + x;
				graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, batchX, y, EDGE_PADDING, textureYOffset, batch * SLOT_SIZE, height, 256, 256);
				columnsRemaining -= batch;
			}
			//blit left and right edges for section
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, textureYOffset, EDGE_PADDING, height, 256, 256);
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, columns * SLOT_SIZE + EDGE_PADDING + x, y, 169, textureYOffset, EDGE_PADDING, height, 256, 256);
		}


		private void RenderCustomBackpack(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y, int rows, int columns, String label, BackpackPreview.Storage storage) {
			//render background
			CustomInventorySize(graphics, x, y, rows, columns);

			//render label
			Font textRenderer = CLIENT.font;
			graphics.text(textRenderer, label, x + 8, y + 6, 0xFF404040, false);


			if (!open) {
				//render cached items
				for (int i = 9; i < storage.size(); ++i) {
					ItemStack currentStack = storage.getStack(i);
					if (currentStack.isEmpty()) continue;
					int itemX = x + (i - 9) % columns * SLOT_SIZE + 8;
					int itemY = y + (i - 9) / columns * SLOT_SIZE + SLOT_SIZE;
					// draw custom backgrounds ect as well as item
					ItemBackgroundManager.drawBackgrounds(currentStack, graphics, itemX, itemY);

					if (ItemProtection.isItemProtected(currentStack)) {
						ItemProtection.drawSlotIcon(graphics, itemX, itemY);
					}

					graphics.item(currentStack, itemX, itemY);
					graphics.itemDecorations(textRenderer, currentStack, itemX, itemY);
					SlotTextManager.extractSlotText(graphics, textRenderer, null, currentStack, i, itemX, itemY);

					//draw tooltip if hovered
					if (mouseX > itemX && mouseX <= itemX + SLOT_SIZE && mouseY > itemY && mouseY <= itemY + SLOT_SIZE) {
						graphics.setTooltipForNextFrame(CLIENT.font, currentStack, mouseX, mouseY);
					}
				}
			} else {
				//move slots to fit this open backpack
				handler.moveBackpackSlots(getX() - screenLeft + 8, getY() - screenTop + SLOT_SIZE, columns);
			}

			//darken other slots to show they don't actually exist
			for (int i = storage.size() - 9; i < rows * columns; ++i) {
				int itemX = x + i % columns * SLOT_SIZE + 8;
				int itemY = y + i / columns * SLOT_SIZE + SLOT_SIZE;
				graphics.fill(itemX, itemY, itemX + 16, itemY + 16, 0xB0_000000);

			}
		}

		@Override
		public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			RenderCustomBackpack(graphics, mouseX, mouseY, getX(), getY(), rows, columns, label, storage);
			//outline open
			if (open) {
				graphics.outline(getX(), getY(), getWidth(), getHeight(), Color.yellow.getRGB());
			}
		}

		@Override
		public void onClick(MouseButtonEvent event, boolean doubleClick) {
			super.onClick(event, doubleClick);
			if (!open) {
				switchOpenStorage(index);
			}
		}


		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
		}
	}
}
