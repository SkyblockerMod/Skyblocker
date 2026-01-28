package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.compatibility.ResourcePackCompatibility;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.auction.widgets.AuctionTypeWidget;
import de.hysky.skyblocker.skyblock.auction.widgets.CategoryTabWidget;
import de.hysky.skyblocker.skyblock.auction.widgets.RarityWidget;
import de.hysky.skyblocker.skyblock.auction.widgets.SortWidget;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.render.gui.AbstractCustomHypixelGUI;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import org.apache.commons.lang3.math.NumberUtils;
import org.joml.Matrix3x2fStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AuctionBrowserScreen extends AbstractCustomHypixelGUI<AuctionHouseScreenHandler> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuctionBrowserScreen.class);
	private static final Identifier TEXTURE = SkyblockerMod.id("textures/gui/auctions_gui/browser.png");
	private static final Identifier SCROLLER_TEXTURE = Identifier.withDefaultNamespace("container/creative_inventory/scroller");

	private static final Identifier up_arrow_tex = SkyblockerMod.id("up_arrow_even"); // Put them in their own fields to avoid object allocation on each frame
	private static final Identifier down_arrow_tex = SkyblockerMod.id("down_arrow_even");
	public static final Supplier<TextureAtlasSprite> UP_ARROW = () -> Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI).getSprite(up_arrow_tex);
	public static final Supplier<TextureAtlasSprite> DOWN_ARROW = () -> Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI).getSprite(down_arrow_tex);


	// SLOTS
	public static final int RESET_BUTTON_SLOT = 47;
	public static final int SEARCH_BUTTON_SLOT = 48;
	public static final int BACK_BUTTON_SLOT = 49;
	public static final int SORT_BUTTON_SLOT = 50;
	public static final int RARITY_BUTTON_SLOT = 51;
	public static final int AUCTION_TYPE_BUTTON_SLOT = 52;

	public static final int PREV_PAGE_BUTTON = 46;
	public static final int NEXT_PAGE_BUTTON = 53;

	private final Int2BooleanOpenHashMap isSlotHighlighted = new Int2BooleanOpenHashMap(24);


	// WIDGETS
	private SortWidget sortWidget;
	private AuctionTypeWidget auctionTypeWidget;
	private RarityWidget rarityWidget;
	private Button resetFiltersButton;
	private final List<CategoryTabWidget> categoryTabWidgets = new ArrayList<>(6);
	private String search = "";

	public AuctionBrowserScreen(AuctionHouseScreenHandler handler, Inventory inventory) {
		super(handler, inventory, ResourcePackCompatibility.options.renameAuctionBrowser().orElse(false) ? Component.literal("AuctionBrowserSkyblocker") : Component.literal("Auctions Browser"));
		this.imageHeight = 187;
		this.inventoryLabelY = 92;
		this.titleLabelX = 999;
	}

	@Override
	protected void init() {
		super.init();
		sortWidget = new SortWidget(leftPos + 25, topPos + 81, this::clickSlot);
		sortWidget.setSlotId(SORT_BUTTON_SLOT);
		addRenderableWidget(sortWidget);
		auctionTypeWidget = new AuctionTypeWidget(leftPos + 134, topPos + 77, this::clickSlot);
		auctionTypeWidget.setSlotId(AUCTION_TYPE_BUTTON_SLOT);
		addRenderableWidget(auctionTypeWidget);
		rarityWidget = new RarityWidget(leftPos + 73, topPos + 80, this::clickSlot);
		rarityWidget.setSlotId(RARITY_BUTTON_SLOT);
		addRenderableWidget(rarityWidget);
		resetFiltersButton = new ScaledTextButtonWidget(leftPos + 10, topPos + 77, 12, 12, Component.literal("↻"), this::onResetPressed);
		addRenderableWidget(resetFiltersButton);
		resetFiltersButton.setTooltip(Tooltip.create(Component.literal("Reset Filters")));
		resetFiltersButton.setTooltipDelay(Duration.ofMillis(500));

		addRenderableWidget(new Button.Builder(Component.literal("<"), button -> this.clickSlot(BACK_BUTTON_SLOT))
				.pos(leftPos + 98, topPos + 4)
				.size(12, 12)
				.build());

		if (categoryTabWidgets.isEmpty())
			for (int i = 0; i < 6; i++) {
				CategoryTabWidget categoryTabWidget = new CategoryTabWidget(new ItemStack(Items.SPONGE), this::clickSlot);
				categoryTabWidgets.add(categoryTabWidget);
				addWidget(categoryTabWidget); // This method only makes it clickable, does not add it to the drawables list
				// manually rendered in the render method to have it not render behind the durability bars
				categoryTabWidget.setPosition(leftPos - 30, topPos + 3 + i * 28);
			}
		else
			for (int i = 0; i < categoryTabWidgets.size(); i++) {
				CategoryTabWidget categoryTabWidget = categoryTabWidgets.get(i);
				categoryTabWidget.setPosition(leftPos - 30, topPos + 3 + i * 28);
				addWidget(categoryTabWidget);

			}
	}

	private void onResetPressed(Button buttonWidget) {
		buttonWidget.setFocused(false); // Annoying.
		this.clickSlot(RESET_BUTTON_SLOT, 0);
	}

	@Override
	protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		for (CategoryTabWidget categoryTabWidget : categoryTabWidgets) {
			categoryTabWidget.render(context, mouseX, mouseY, delta);
		}
		if (isWaitingForServer) {
			String waiting = "Waiting for server...";
			context.drawString(font, waiting, this.width - font.width(waiting) - 5, this.height - font.lineHeight - 2, CommonColors.WHITE, true);
		}

		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();
		matrices.translate(leftPos, topPos);
		// Search
		context.enableScissor(7, 4, 97, 16);
		context.drawString(font, Component.literal(search).withStyle(Style.EMPTY.withUnderlined(onSearchField(mouseX, mouseY))), 9, 6, CommonColors.WHITE, true);
		context.disableScissor();

		// Scrollbar
		if (prevPageVisible) {
			if (onScrollbarTop(mouseX, mouseY))
				context.blitSprite(RenderPipelines.GUI_TEXTURED, UP_ARROW.get(), 159, 13, 6, 3);
			else context.blitSprite(RenderPipelines.GUI_TEXTURED, UP_ARROW.get(), 159, 13, 6, 3, ARGB.color(137, 137, 137));
		}

		if (nextPageVisible) {
			if (onScrollbarBottom(mouseX, mouseY))
				context.blitSprite(RenderPipelines.GUI_TEXTURED, DOWN_ARROW.get(), 159, 72, 6, 3);
			else context.blitSprite(RenderPipelines.GUI_TEXTURED, DOWN_ARROW.get(), 159, 72, 6, 3, ARGB.color(137, 137, 137));
		}
		context.drawString(font, String.format("%d/%d", currentPage, totalPages), 111, 6, CommonColors.GRAY, false);
		if (totalPages <= 1)
			context.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_TEXTURE, 156, 18, 12, 15);
		else
			context.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_TEXTURE, 156, (int) (18 + (float) (Math.min(currentPage, totalPages) - 1) / (totalPages - 1) * 37), 12, 15);

		matrices.popMatrix();

		this.renderTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void renderSlot(GuiGraphics context, Slot slot) {
		if (SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.highlightCheapBIN && slot.hasItem() && isSlotHighlighted.getOrDefault(slot.index, false)) {
			HudHelper.drawBorder(context, slot.x, slot.y, 16, 16, new Color(0, 255, 0, 100).getRGB());
		}
		super.renderSlot(context, slot);
	}

	@Override
	protected void slotClicked(Slot slot, int slotId, int button, ClickType actionType) {
		if (slotId >= menu.getRowCount() * 9) return;
		super.slotClicked(slot, slotId, button, actionType);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (isWaitingForServer) return super.mouseClicked(click, doubled);
		if (onScrollbarTop((int) click.x(), (int) click.y()) && prevPageVisible) {
			clickSlot(PREV_PAGE_BUTTON);
			return true;
		}
		if (onScrollbarBottom((int) click.x(), (int) click.y()) && nextPageVisible) {
			clickSlot(NEXT_PAGE_BUTTON);
			return true;
		}

		if (onSearchField((int) click.x(), (int) click.y())) {
			clickSlot(SEARCH_BUTTON_SLOT);
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	private boolean onScrollbarTop(int mouseX, int mouseY) {
		int localX = mouseX - leftPos;
		int localY = mouseY - topPos;
		return localX > 154 && localX < 169 && localY > 6 && localY < 44;
	}

	private boolean onScrollbarBottom(int mouseX, int mouseY) {
		int localX = mouseX - leftPos;
		int localY = mouseY - topPos;
		return localX > 154 && localX < 169 && localY > 43 && localY < 80;
	}

	private boolean onSearchField(int mouseX, int mouseY) {
		int localX = mouseX - leftPos;
		int localY = mouseY - topPos;
		return localX > 6 && localX < 97 && localY > 3 && localY < 16;
	}

	@Override
	public void onSlotChange(AuctionHouseScreenHandler handler, int slotId, ItemStack stack) {
		if (stack.isEmpty()) return;
		isWaitingForServer = false;

		switch (slotId) {
			case PREV_PAGE_BUTTON -> {
				prevPageVisible = false;
				if (stack.is(Items.ARROW)) {
					prevPageVisible = true;
					parsePage(stack);
				}
			}
			case NEXT_PAGE_BUTTON -> {
				nextPageVisible = false;
				if (stack.is(Items.ARROW)) {
					nextPageVisible = true;
					parsePage(stack);
				}
			}
			case SORT_BUTTON_SLOT ->
					sortWidget.setCurrent(SortWidget.Option.get(getOrdinal(stack.skyblocker$getLoreStrings())));
			case AUCTION_TYPE_BUTTON_SLOT ->
					auctionTypeWidget.setCurrent(AuctionTypeWidget.Option.get(getOrdinal(stack.skyblocker$getLoreStrings())));
			case RARITY_BUTTON_SLOT -> {
				int ordinal = getOrdinal(stack.skyblocker$getLoreStrings());
				@SuppressWarnings("deprecation")
				List<Component> tooltip = ItemUtils.getLore(stack);
				String split = tooltip.get(ordinal + 1).getString().substring(2);
				rarityWidget.setText(tooltip.subList(1, tooltip.size() - 3), split);
			}
			case RESET_BUTTON_SLOT -> resetFiltersButton.active = handler.getSlot(slotId).getItem().is(Items.ANVIL);
			case SEARCH_BUTTON_SLOT -> {
				List<String> tooltipSearch = stack.skyblocker$getLoreStrings();
				for (String string : tooltipSearch) {
					if (string.contains("Filtered:")) {
						String[] splitSearch = string.split(":");
						if (splitSearch.length < 2) {
							search = "";
						} else search = splitSearch[1].trim();
						break;
					}
				}
			}
			default -> {
				if (slotId < this.menu.getRowCount() * 9 && slotId % 9 == 0) {
					CategoryTabWidget categoryTabWidget = categoryTabWidgets.get(slotId / 9);
					categoryTabWidget.setSlotId(slotId);
					categoryTabWidget.setIcon(handler.getSlot(slotId).getItem());
					List<String> tooltipDefault = handler.getSlot(slotId).getItem().skyblocker$getLoreStrings();
					for (int j = tooltipDefault.size() - 1; j >= 0; j--) {
						String lowerCase = tooltipDefault.get(j).toLowerCase(Locale.ENGLISH);
						if (lowerCase.contains("currently")) {
							categoryTabWidget.select();
							break;
						} else if (lowerCase.contains("click")) {
							categoryTabWidget.unselect();
							break;
						} else categoryTabWidget.unselect();
					}
				} else if (slotId > 9 && slotId < (handler.getRowCount() - 1) * 9 && slotId % 9 > 1 && slotId % 9 < 8) {
					Object2DoubleMap<String> data = TooltipInfoType.THREE_DAY_AVERAGE.getData();
					if (!SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.highlightCheapBIN || data == null) return;
					List<String> tooltip = stack.skyblocker$getLoreStrings();
					for (int k = tooltip.size() - 1; k >= 0; k--) {
						String string = tooltip.get(k);
						if (string.toLowerCase(Locale.ENGLISH).contains("buy it now:")) {
							String[] split = string.split(":");
							if (split.length < 2) continue;
							String coins = split[1].replace(",", "").replace("coins", "").trim();
							long parsed = NumberUtils.toLong(coins, Long.MAX_VALUE);
							double price = data.getDouble(stack.getNeuName());
							isSlotHighlighted.put(slotId, price > parsed);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		if (input.isUp() && prevPageVisible) {
			clickSlot(PREV_PAGE_BUTTON);
			return true;
		}
		if (input.isDown() && nextPageVisible) {
			clickSlot(NEXT_PAGE_BUTTON);
			return true;
		}
		return super.keyPressed(input);
	}

	private static int getOrdinal(List<String> tooltip) {
		int ordinal = 0;
		for (int j = 0; j < tooltip.size() - 4; j++) {
			if (j + 1 >= tooltip.size()) break;
			if (tooltip.get(j + 1).contains("▶")) {
				ordinal = j;
				break;
			}
		}
		return ordinal;
	}

	int currentPage = 0;
	int totalPages = 1;
	private boolean prevPageVisible = false;
	private boolean nextPageVisible = false;

	private void parsePage(ItemStack stack) {
		try {
			List<String> tooltip = stack.skyblocker$getLoreStrings();
			String str = tooltip.getFirst().trim();
			str = str.substring(1, str.length() - 1); // remove parentheses
			String[] parts = str.split("/"); // split the string
			currentPage = Integer.parseInt(parts[0].replace(",", "")); // parse current page
			totalPages = Integer.parseInt(parts[1].replace(",", "")); // parse total
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Fancy Auction House] Failed to parse page arrow", e);
		}
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
		return mouseX < (double) left - 32 || mouseY < (double) top || mouseX >= (double) (left + this.imageWidth) || mouseY >= (double) (top + this.imageHeight);
	}

	private static class ScaledTextButtonWidget extends Button {

		protected ScaledTextButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress) {
			super(x, y, width, height, message, onPress, Supplier::get);
		}

		// Code taken mostly from YACL by isxander. Love you <3
		@Override
		public void renderString(GuiGraphics context, Font textRenderer, int color) {
			Font font = Minecraft.getInstance().font;
			Matrix3x2fStack matrices = context.pose();
			float textScale = 2.f;

			matrices.pushMatrix();
			matrices.translate(((this.getX() + this.width / 2f) - font.width(getMessage()) * textScale / 2) + 1, (float) this.getY() + (this.height - font.lineHeight * textScale) / 2f - 1);
			matrices.scale(textScale, textScale);
			context.drawString(font, getMessage(), 0, 0, color | Mth.ceil(this.alpha * 255.0F) << 24, true);
			matrices.popMatrix();
		}
	}
}
