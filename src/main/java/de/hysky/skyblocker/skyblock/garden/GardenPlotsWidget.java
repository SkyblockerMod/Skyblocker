package de.hysky.skyblocker.skyblock.garden;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.GuiHelper;
import de.hysky.skyblocker.utils.render.gui.ItemButtonWidget;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.tag.client.v1.ClientTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class GardenPlotsWidget extends AbstractContainerWidget {
	private static final Supplier<ScreenPosition> POSITION = () -> new ScreenPosition(SkyblockerConfigManager.get().farming.plotsWidget.x, SkyblockerConfigManager.get().farming.plotsWidget.y);
	private static final int SPACING = 4;
	private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");
	private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");

	public static final Int2IntMap GARDEN_PLOT_TO_SLOT = Int2IntMap.ofEntries(
			Int2IntMap.entry(0, 12), // The Barn
			Int2IntMap.entry(1, 7),
			Int2IntMap.entry(2, 11),
			Int2IntMap.entry(3, 13),
			Int2IntMap.entry(4, 17),
			Int2IntMap.entry(5, 6),
			Int2IntMap.entry(6, 8),
			Int2IntMap.entry(7, 16),
			Int2IntMap.entry(8, 18),
			Int2IntMap.entry(9, 2),
			Int2IntMap.entry(10, 10),
			Int2IntMap.entry(11, 14),
			Int2IntMap.entry(12, 22),
			Int2IntMap.entry(13, 1),
			Int2IntMap.entry(14, 3),
			Int2IntMap.entry(15, 5),
			Int2IntMap.entry(16, 9),
			Int2IntMap.entry(17, 15),
			Int2IntMap.entry(18, 19),
			Int2IntMap.entry(19, 21),
			Int2IntMap.entry(20, 23),
			Int2IntMap.entry(21, 0),
			Int2IntMap.entry(22, 4),
			Int2IntMap.entry(23, 20),
			Int2IntMap.entry(24, 24)
	);

	private static final @Nullable String[] CUSTOM_ICON_OPTIONS = new @Nullable String[] {
			null,
			"WHEAT",
			"CARROT_ITEM",
			"POTATO_ITEM",
			"SUGAR_CANE",
			"DOUBLE_PLANT",
			"MOONFLOWER",
			"WILD_ROSE",
			"NETHER_STALK",
			"RED_MUSHROOM",
			"CACTUS",
			"MELON",
			"PUMPKIN",
			"INK_SACK-3"
	};

	private static final Identifier BACKGROUND_TEXTURE = SkyblockerMod.id("textures/gui/garden_plots.png");
	private static final MutableComponent GROSS_PEST_TEXT = Component.translatable("skyblocker.gardenPlots.pests").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
	private static final MutableComponent TP_TEXT = Component.translatable("skyblocker.gardenPlots.tp").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);

	private final ItemButtonWidget[] widgets;
	private final IntList infectedPlots = new IntArrayList(8);
	@SuppressWarnings("deprecation")
	private final FlexibleItemStack noneItem = new FlexibleItemStack(new ItemStackTemplate(Items.BARRIER.builtInRegistryHolder(), 1, DataComponentPatch.builder()
			.set(DataComponents.ITEM_NAME, Component.literal("None"))
			.build()));

	private @Nullable ItemStack[] items;
	private int hoveredSlot = -1;
	private int editingSlotIcon = -1;
	private long updateFromTabTime = System.currentTimeMillis();
	private ItemStack[] customIconOptionsItems = new ItemStack[0];
	private boolean dragAreaHovered;
	private @Nullable ScreenPosition dragging;
	private ScreenRectangle inventoryRectangle;


	public GardenPlotsWidget(ScreenRectangle inventoryRectangle) {
		super(0, 0, 104, 132, Component.translatable("skyblocker.gardenPlots"), AbstractScrollArea.defaultSettings(0));
		this.inventoryRectangle = inventoryRectangle;
		updatePlotItems();
		updateInfestedFromTab();

		// Inner widgets
		ItemButtonWidget deskButton = new ItemButtonWidget(
				getX() + 7, getBottom() - 24,
				new ItemStack(Items.BOOK), Component.translatable("skyblocker.gardenPlots.openDesk"),
				_ -> MessageScheduler.INSTANCE.sendMessageAfterCooldown("/desk", true)
		);
		ItemButtonWidget spawnButton = new ItemButtonWidget(
				getRight() - 7 - 40 - 2, getBottom() - 24,
				new ItemStack(Items.ENDER_EYE), Component.translatable("skyblocker.gardenPlots.spawn"),
				_ -> MessageScheduler.INSTANCE.sendMessageAfterCooldown("/warp garden", true)
		);
		ItemButtonWidget setSpawnButton = new ItemButtonWidget(
				getRight() - 7 - 20, getBottom() - 24,
				new ItemStack(Math.random() < 0.001 ? Items.BED.pink() : Items.BED.red()), Component.translatable("skyblocker.gardenPlots.setSpawn"),
				_ -> MessageScheduler.INSTANCE.sendMessageAfterCooldown("/setspawn", true)
		);
		widgets = new ItemButtonWidget[]{deskButton, spawnButton, setSpawnButton};
		setPositionFromConfig();
	}

	private void updatePlotItems() {
		items = Arrays.stream(GardenPlots.GARDEN_PLOTS).map(gardenPlot -> {
			if (gardenPlot == null) return null;
			ItemStack itemStack = gardenPlot.customIcon()
					.map(s -> ItemRepository.getItemStack(s, ItemUtils.getItemIdPlaceholder(s)))
					.map(FlexibleItemStack::getStackOrThrow)
					.orElseGet(() -> gardenPlot.icon().map(FlexibleItemStack::new, s -> ItemRepository.getItemStack(s, ItemUtils.getItemIdPlaceholder(s))).getStackOrThrow()).copy();
			itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(gardenPlot.name()).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
			return itemStack;
		}).toArray(ItemStack[]::new);
		ItemStack barnIcon = new ItemStack(Items.LODESTONE);
		barnIcon.set(DataComponents.ITEM_NAME, Component.literal("The Barn"));
		items[12] = barnIcon;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		Font textRenderer = Minecraft.getInstance().font;
		Matrix3x2fStack matrices = graphics.pose();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());

		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, 0, 0, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());

		graphics.text(textRenderer, editingSlotIcon < 0 ? getMessage() : Component.literal("Custom Icon"), 8, 6, CommonColors.DARK_GRAY, false);

		hoveredSlot = -1;
		long timeMillis = System.currentTimeMillis();
		@Nullable ItemStack[] stacks = editingSlotIcon >= 0 ? customIconOptionsItems : items;
		for (int i = 0; i < stacks.length; i++) {
			int slotX = 7 + (i % 5) * 18;
			int slotY = 17 + (i / 5) * 18;
			boolean hovered = slotX + getX() <= mouseX && mouseX < slotX + getX() + 18 && slotY + getY() <= mouseY && mouseY < slotY + getY() + 18;
			boolean infested = infectedPlots.contains(i);

			if (hovered) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, slotX - 3, slotY - 3, 24, 24);
			}

			ItemStack item = stacks[i];
			// Still show hover highlight & pest outline in empty slots.
			if (item == null) {
				if (hovered)
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, slotX - 3, slotY - 3, 24, 24);

				if (infested && (timeMillis & 512) != 0)
					GuiHelper.border(graphics, slotX + 1, slotY + 1, 16, 16, CommonColors.RED);

				continue;
			}

			if (hovered) {
				//noinspection deprecation
				if (ClientTags.isInLocal(ConventionalItemTags.GLASS_PANES, item.getItem().builtInRegistryHolder().key())) {
					graphics.item(item, slotX + 1, slotY + 1);
				} else {
					matrices.pushMatrix();
					matrices.translate(slotX, slotY);
					matrices.scale(1.125f, 1.125f);
					graphics.item(item, 0, 0);
					matrices.popMatrix();
				}
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, slotX - 3, slotY - 3, 24, 24);
				hoveredSlot = i;
			} else
				graphics.item(item, slotX + 1, slotY + 1);

			if (editingSlotIcon >= 0) {
				if (hovered) {
					graphics.setComponentTooltipForNextFrame(textRenderer, List.of(item.getHoverName()), mouseX, mouseY);
				}
				continue;
			}

			if (infested && (timeMillis & 512) != 0) {
				GuiHelper.border(graphics, slotX + 1, slotY + 1, 16, 16, CommonColors.RED);
			}

			// tooltip
			if (hovered) {
				List<Component> tooltip = infested ?
						List.of(
								Component.translatable("skyblocker.gardenPlots.plot", item.getHoverName()),
								GROSS_PEST_TEXT,
								Component.empty(),
								TP_TEXT) :

						i == 12 ?
								List.of(
										item.getHoverName(),
										Component.empty(),
										TP_TEXT) :

								List.of(
										Component.translatable("skyblocker.gardenPlots.plot", item.getHoverName()),
										Component.empty(),
										TP_TEXT
								);
				graphics.setComponentTooltipForNextFrame(textRenderer, tooltip, mouseX, mouseY);
			}
		}

		matrices.popMatrix();

		for (ItemButtonWidget widget : widgets) {
			widget.extractRenderState(graphics, mouseX, mouseY, a);
		}



		if (timeMillis - updateFromTabTime > 3000) {
			updateFromTabTime = timeMillis;
			updateInfestedFromTab();
		}

		dragAreaHovered = getX() + 4 < mouseX && getY() + 4 < mouseY && mouseX < getRight() - 4 && mouseY < getY() + 15;
		if (dragAreaHovered) graphics.requestCursor(CursorTypes.RESIZE_ALL);
	}

	private void updateInfestedFromTab() {
		infectedPlots.clear();
		for (int i = 0; i < PlayerListManager.getPlayerStringList().size(); i++) {
			String string = PlayerListManager.getPlayerStringList().get(i);
			if (string.startsWith("Plots:")) {
				String[] split = string.split(":")[1].split(",");
				for (String s : split) {
					try {
						infectedPlots.add(GARDEN_PLOT_TO_SLOT.getOrDefault(Integer.parseInt(s.strip()), -1));
					} catch (NumberFormatException _) {}
				}
				break;
			}
		}
	}

	private void setPositionFromConfig() {
		setPosition(inventoryRectangle.right() + SPACING + POSITION.get().x(), inventoryRectangle.top() + POSITION.get().y());
	}

	public void setInventoryRectangle(ScreenRectangle inventoryRectangle) {
		this.inventoryRectangle = inventoryRectangle;
		setPositionFromConfig();
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		super.onClick(click, doubled);
		if (dragAreaHovered) {
			if (click.button() == InputConstants.MOUSE_BUTTON_RIGHT && Minecraft.getInstance().hasShiftDown()) {
				setPosition(inventoryRectangle.right() + SPACING, inventoryRectangle.top());
				savePositionToConfig();
			} else {
				dragging = new ScreenPosition((int) click.x() - getX(), (int) click.y() - getY());
			}
		}
		if (hoveredSlot == -1) return;

		if (editingSlotIcon >= 0) {
			GardenPlots.GardenPlot plot = GardenPlots.GARDEN_PLOTS[editingSlotIcon];
			if (plot != null) GardenPlots.GARDEN_PLOTS[editingSlotIcon] = plot.withCustomIcon(CUSTOM_ICON_OPTIONS[hoveredSlot]);
			editingSlotIcon = -1;
			updatePlotItems();
			return;
		}

		if (click.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
			editingSlotIcon = hoveredSlot;
			customIconOptionsItems = Arrays.stream(CUSTOM_ICON_OPTIONS).map(s -> {
				if (s == null) return noneItem;
				FlexibleItemStack stack = ItemRepository.getItemStack(s);
				if (stack == null) return ItemUtils.getItemIdPlaceholder(s);
				return stack;
			}).map(FlexibleItemStack::getStackOrThrow).toArray(ItemStack[]::new);
			return;
		}

		if (SkyblockerConfigManager.get().farming.plotsWidget.closeScreenOnPlotClick && Minecraft.getInstance().gui.screen() != null)
			Minecraft.getInstance().gui.screen().onClose();

		if (hoveredSlot == 12) MessageScheduler.INSTANCE.sendMessageAfterCooldown("/plottp barn", true);
		else MessageScheduler.INSTANCE.sendMessageAfterCooldown("/plottp " + GardenPlots.GARDEN_PLOTS[hoveredSlot].name(), true);
	}

	@Override
	public void onRelease(MouseButtonEvent event) {
		super.onRelease(event);
		if (dragging == null) return;
		dragging = null;
		savePositionToConfig();
	}

	private void savePositionToConfig() {
		SkyblockerConfigManager.update(config -> {
			config.farming.plotsWidget.x = getX() - inventoryRectangle.right() - SPACING;
			config.farming.plotsWidget.y = getY() - inventoryRectangle.top();
		});
	}

	@Override
	protected void onDrag(MouseButtonEvent event, double dx, double dy) {
		if (dragging == null) return;
		setPosition(
				Math.max((int) event.x() - dragging.x(), inventoryRectangle.right() + SPACING),
				(int) event.y() - dragging.y());
	}

	@Override
	protected boolean isValidClickButton(MouseButtonInfo input) {
		return (super.isValidClickButton(input) || input.button() == InputConstants.MOUSE_BUTTON_RIGHT) && (hoveredSlot != -1 || dragAreaHovered);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of(widgets);
	}

	@Override
	protected int contentHeight() {
		return getHeight();
	}

	@Override
	public void setX(int x) {
		int prevX = getX();
		super.setX(x);
		int diff = x - prevX;
		for (ItemButtonWidget widget : widgets) {
			widget.setX(widget.getX() + diff);
		}
	}

	@Override
	public void setY(int y) {
		int prevY = getY();
		super.setY(y);
		int diff = y - prevY;
		for (ItemButtonWidget widget : widgets) {
			widget.setY(widget.getY() + diff);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (isMouseOver(click.x(), click.y()) && isValidClickButton(click.buttonInfo())) {
			onClick(click, doubled);
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

}
