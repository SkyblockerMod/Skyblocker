package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.HudHelper;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class GardenPlotsWidget extends AbstractContainerWidget {
	private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");
	private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");

	public static final Int2IntMap GARDEN_PLOT_TO_SLOT = Int2IntMap.ofEntries(
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
	private final ItemStack noneItem = new ItemStack(Items.BARRIER.builtInRegistryHolder(), 1, DataComponentPatch.builder()
			.set(DataComponents.ITEM_NAME, Component.literal("None"))
			.build());

	private @Nullable ItemStack[] items;
	private int hoveredSlot = -1;
	private int editingSlotIcon = -1;
	private long updateFromTabTime = System.currentTimeMillis();
	private ItemStack[] customIconOptionsItems = new ItemStack[0];


	public GardenPlotsWidget(int x, int y) {
		super(x, y, 104, 132, Component.translatable("skyblocker.gardenPlots"));
		updatePlotItems();
		updateInfestedFromTab();

		// Inner widgets
		ItemButtonWidget deskButton = new ItemButtonWidget(
				getX() + 7, getBottom() - 24,
				new ItemStack(Items.BOOK), Component.translatable("skyblocker.gardenPlots.openDesk"),
				button -> MessageScheduler.INSTANCE.sendMessageAfterCooldown("/desk", true)
		);
		ItemButtonWidget spawnButton = new ItemButtonWidget(
				getRight() - 7 - 40 - 2, getBottom() - 24,
				new ItemStack(Items.ENDER_EYE), Component.translatable("skyblocker.gardenPlots.spawn"),
				button -> MessageScheduler.INSTANCE.sendMessageAfterCooldown("/warp garden", true)
		);
		ItemButtonWidget setSpawnButton = new ItemButtonWidget(
				getRight() - 7 - 20, getBottom() - 24,
				new ItemStack(Math.random() < 0.001 ? Items.PINK_BED : Items.RED_BED), Component.translatable("skyblocker.gardenPlots.setSpawn"),
				button -> MessageScheduler.INSTANCE.sendMessageAfterCooldown("/setspawn", true)
		);
		widgets = new ItemButtonWidget[]{deskButton, spawnButton, setSpawnButton};
	}

	private void updatePlotItems() {
		items = Arrays.stream(GardenPlots.GARDEN_PLOTS).map(gardenPlot -> {
			if (gardenPlot == null) return null;
			ItemStack itemStack = gardenPlot.customIcon()
					.map(s -> ItemRepository.getItemStack(s, ItemUtils.getItemIdPlaceholder(s)))
					.orElseGet(() -> gardenPlot.icon().map(ItemStack::new, s -> ItemRepository.getItemStack(s, ItemUtils.getItemIdPlaceholder(s)))).copy();
			itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(gardenPlot.name()).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
			return itemStack;
		}).toArray(ItemStack[]::new);
		items[12] = new ItemStack(Items.LODESTONE);
		items[12].set(DataComponents.ITEM_NAME, Component.literal("The Barn"));
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		Font textRenderer = Minecraft.getInstance().font;
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());

		context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, 0, 0, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());

		context.drawString(textRenderer, editingSlotIcon < 0 ? getMessage() : Component.literal("Custom Icon"), 8, 6, CommonColors.DARK_GRAY, false);

		hoveredSlot = -1;
		long timeMillis = System.currentTimeMillis();
		@Nullable ItemStack[] stacks = editingSlotIcon >= 0 ? customIconOptionsItems : items;
		for (int i = 0; i < stacks.length; i++) {
			ItemStack item = stacks[i];
			if (item == null) continue;


			int slotX = 7 + (i % 5) * 18;
			int slotY = 17 + (i / 5) * 18;
			boolean hovered = slotX + getX() <= mouseX && mouseX < slotX + getX() + 18 && slotY + getY() <= mouseY && mouseY < slotY + getY() + 18;

			if (hovered) {
				context.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, slotX - 3, slotY - 3, 24, 24);
				//noinspection deprecation
				if (ClientTags.isInLocal(ConventionalItemTags.GLASS_PANES, item.getItem().builtInRegistryHolder().key())) {
					context.renderItem(item, slotX + 1, slotY + 1);
				} else {
					matrices.pushMatrix();
					matrices.translate(slotX, slotY);
					matrices.scale(1.125f, 1.125f);
					context.renderItem(item, 0, 0);
					matrices.popMatrix();
				}
				context.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, slotX - 3, slotY - 3, 24, 24);
				hoveredSlot = i;
			} else
				context.renderItem(item, slotX + 1, slotY + 1);

			if (editingSlotIcon >= 0) {
				if (hovered) {
					context.setComponentTooltipForNextFrame(textRenderer, List.of(item.getHoverName()), mouseX, mouseY);
				}
				continue;
			}
			boolean infested = infectedPlots.contains(i);
			if (infested && (timeMillis & 512) != 0) {
				HudHelper.drawBorder(context, slotX + 1, slotY + 1, 16, 16, CommonColors.RED);
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
				context.setComponentTooltipForNextFrame(textRenderer, tooltip, mouseX, mouseY);
			}
		}

		matrices.popMatrix();

		for (ItemButtonWidget widget : widgets) {
			widget.render(context, mouseX, mouseY, delta);
		}



		if (timeMillis - updateFromTabTime > 3000) {
			updateFromTabTime = timeMillis;
			updateInfestedFromTab();
		}
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
					} catch (NumberFormatException ignored) {}
				}
				break;
			}
		}
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		super.onClick(click, doubled);
		if (hoveredSlot == -1) return;

		if (editingSlotIcon >= 0) {
			GardenPlots.GardenPlot plot = GardenPlots.GARDEN_PLOTS[editingSlotIcon];
			if (plot != null) GardenPlots.GARDEN_PLOTS[editingSlotIcon] = plot.withCustomIcon(CUSTOM_ICON_OPTIONS[hoveredSlot]);
			editingSlotIcon = -1;
			updatePlotItems();
			return;
		}

		if (click.button() == 1) {
			editingSlotIcon = hoveredSlot;
			customIconOptionsItems = Arrays.stream(CUSTOM_ICON_OPTIONS).map(s -> {
				if (s == null) return noneItem;
				ItemStack stack = ItemRepository.getItemStack(s);
				if (stack == null) return ItemUtils.getItemIdPlaceholder(s);
				return stack;
			}).toArray(ItemStack[]::new);
			return;
		}

		if (SkyblockerConfigManager.get().farming.garden.closeScreenOnPlotClick && Minecraft.getInstance().screen != null)
			Minecraft.getInstance().screen.onClose();

		if (hoveredSlot == 12) MessageScheduler.INSTANCE.sendMessageAfterCooldown("/plottp barn", true);
		else MessageScheduler.INSTANCE.sendMessageAfterCooldown("/plottp " + GardenPlots.GARDEN_PLOTS[hoveredSlot].name(), true);
	}

	@Override
	protected boolean isValidClickButton(MouseButtonInfo input) {
		return (super.isValidClickButton(input) || input.button() == 1) && hoveredSlot != -1;
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
	protected double scrollRate() {
		return 0;
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
