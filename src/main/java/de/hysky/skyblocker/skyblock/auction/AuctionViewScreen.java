package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.gui.AbstractCustomHypixelGUI;
import org.joml.Matrix3x2fStack;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class AuctionViewScreen extends AbstractCustomHypixelGUI<AuctionHouseScreenHandler> {
	protected static final Identifier BACKGROUND_TEXTURE = SkyblockerMod.id("textures/gui/auctions_gui/view.png");

	public static final int BACK_BUTTON_SLOT = 49;

	LinearLayout verticalLayout = LinearLayout.vertical();

	public final boolean isBinAuction;
	private StringWidget priceWidget;
	private final Component clickToEditBidText = Component.translatable("skyblocker.fancyAuctionHouse.editBid").setStyle(Style.EMPTY.withUnderlined(true));

	private StringWidget infoTextWidget;
	public String minBid = "";

	private @Nullable BuyState buyState = null;
	private MutableComponent priceText = Component.literal("?");
	private Button buyButton;
	private StringWidget priceTextWidget;

	public AuctionViewScreen(AuctionHouseScreenHandler handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
		imageHeight = 187;
		isBinAuction = this.getTitle().getString().toLowerCase(Locale.ENGLISH).contains("bin");
		inventoryLabelY = 93;
		titleLabelX = 5;
		titleLabelY = 4;
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		if (input.isEscape()) {
			clickSlot(BACK_BUTTON_SLOT);
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	protected void init() {
		super.init();
		verticalLayout = LinearLayout.vertical();
		verticalLayout.spacing(2).defaultCellSetting().alignHorizontallyCenter();
		priceTextWidget = new StringWidget(isBinAuction ? Component.translatable("skyblocker.fancyAuctionHouse.price") : Component.translatable("skyblocker.fancyAuctionHouse.newBid"), font).setMaxWidth(imageWidth - 10, StringWidget.TextOverflow.SCROLLING);
		verticalLayout.addChild(priceTextWidget);

		priceWidget = new StringWidget(Component.literal("?"), font).setMaxWidth(imageWidth - 10, StringWidget.TextOverflow.SCROLLING);
		priceWidget.active = true;
		verticalLayout.addChild(priceWidget);

		infoTextWidget = new StringWidget(Component.literal("Can't Afford"), font).setMaxWidth(imageWidth - 10, StringWidget.TextOverflow.SCROLLING);
		verticalLayout.addChild(infoTextWidget);

		buyButton = Button.builder(isBinAuction ? Component.translatable("skyblocker.fancyAuctionHouse.buy") : Component.translatable("skyblocker.fancyAuctionHouse.bid"), button -> {
			if (buySlotID == -1) return;
			clickSlot(buySlotID);
		}).size(60, 15).build();
		verticalLayout.addChild(buyButton);
		verticalLayout.visitWidgets(this::addRenderableWidget);
		updateLayout();

		Button backButton = new Button.Builder(Component.literal("<"), button -> this.clickSlot(BACK_BUTTON_SLOT))
				.pos(leftPos + imageWidth - 16, topPos + 4)
				.size(12, 12)
				.tooltip(Tooltip.create(Component.literal("or press ESC!")))
				.build();
		backButton.setTooltipDelay(Duration.ofSeconds(1));
		addRenderableWidget(backButton);


	}

	private void changeState(BuyState newState) {
		if (newState == buyState) return;
		buyState = newState;
		switch (buyState) {
			case CANT_AFFORD -> {
				infoTextWidget.setMessage(Component.translatable("skyblocker.fancyAuctionHouse.cantAfford").withColor(CommonColors.RED));
				buyButton.active = false;
			}
			case TOP_BID -> infoTextWidget.setMessage(Component.translatable("skyblocker.fancyAuctionHouse.alreadyTopBid").withColor(CommonColors.SOFT_YELLOW));
			case AFFORD -> infoTextWidget.setMessage(Component.empty());
			case COLLECT_AUCTION -> {
				infoTextWidget.setMessage(changeProfile ? Component.translatable("skyblocker.fancyAuctionHouse.differentProfile") : wonAuction ? Component.empty() : Component.translatable("skyblocker.fancyAuctionHouse.didntWin"));
				//priceWidget.setMessage(Text.empty());
				priceWidget.active = false;

				if (changeProfile) {
					buyButton.setMessage(Component.translatable("skyblocker.fancyAuctionHouse.changeProfile").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
				} else if (wonAuction) {
					buyButton.setMessage(Component.translatable("skyblocker.fancyAuctionHouse.collectAuction"));
				} else {
					buyButton.setMessage(Component.translatable("skyblocker.fancyAuctionHouse.collectBid"));
				}
				buyButton.setWidth(font.width(buyButton.getMessage()) + 4);

				priceTextWidget.setMessage(Component.translatable("skyblocker.fancyAuctionHouse.auctionEnded"));
			}
			case CANCELLABLE_AUCTION -> {
				buyButton.setMessage(Component.translatable("skyblocker.fancyAuctionHouse.cancelAuction").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
				buyButton.setWidth(font.width(buyButton.getMessage()) + 4);

				buyButton.active = true;
				buyButton.visible = true;
			}
			case OWN_AUCTION -> {
				buyButton.visible = false;
				priceWidget.active = false;

				infoTextWidget.setMessage(Component.translatable("skyblocker.fancyAuctionHouse.yourAuction"));
			}
		}
		updateLayout();
	}

	private void updateLayout() {
		verticalLayout.arrangeElements();
		FrameLayout.centerInRectangle(verticalLayout, leftPos, topPos + 36, imageWidth, 60);
	}

	@Override
	protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
		context.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		if (isWaitingForServer) context.drawString(font, "Waiting...", 0, 0, CommonColors.WHITE, true);

		Matrix3x2fStack matrices = context.pose();

		matrices.pushMatrix();
		matrices.translate(leftPos + 77, topPos + 14);
		matrices.scale(1.375f, 1.375f);
		//matrices.translate(0, 0, 100f);
		ItemStack stack = menu.getSlot(13).getItem();
		context.renderItem(stack, 0, 0);
		context.renderItemDecorations(font, stack, 0, 0);
		matrices.popMatrix();

		if (!isBinAuction && buyState != BuyState.COLLECT_AUCTION) {
			if (priceWidget.isMouseOver(mouseX, mouseY) && buyState != BuyState.CANT_AFFORD) {
				priceWidget.setMessage(clickToEditBidText);
			} else {
				priceWidget.setMessage(priceText);
			}
		}

		renderTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void renderTooltip(GuiGraphics context, int x, int y) {
		super.renderTooltip(context, x, y);
		if (x > this.leftPos + 75 && x < this.leftPos + 75 + 26 && y > this.topPos + 13 && y < this.topPos + 13 + 26) {
			context.setComponentTooltipForNextFrame(this.font, this.getTooltipFromContainerItem(menu.getSlot(13).getItem()), x, y);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (!isBinAuction && priceWidget.isMouseOver(click.x(), click.y())) {
			clickSlot(31);
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSlotChange(AuctionHouseScreenHandler handler, int slotId, ItemStack stack) {
		if (stack.is(Items.BLACK_STAINED_GLASS_PANE) || slotId == 13 || slotId >= handler.getRowCount() * 9) return;
		if (stack.is(Items.RED_TERRACOTTA)) { // Red terracotta shows up when you can cancel it
			changeState(BuyState.CANCELLABLE_AUCTION);
			buySlotID = slotId;
		}
		if (priceParsed) return;
		if (stack.is(Items.POISONOUS_POTATO)) {
			changeState(BuyState.CANT_AFFORD);
			getPriceFromTooltip(ItemUtils.getLore(stack));
			buySlotID = slotId;
		} else if (stack.is(Items.GOLD_NUGGET)) {
			changeState(BuyState.AFFORD);
			getPriceFromTooltip(ItemUtils.getLore(stack));
			buySlotID = slotId;
		} else if (stack.is(Items.GOLD_BLOCK)) {
			changeState(BuyState.TOP_BID);
			getPriceFromTooltip(ItemUtils.getLore(stack));
			buySlotID = slotId;
		} else if (stack.is(Items.NAME_TAG)) {
			getPriceFromTooltip(ItemUtils.getLore(stack));
			changeProfile = true;
			buySlotID = slotId;
		}
		String lowerCase = stack.getHoverName().getString().toLowerCase(Locale.ENGLISH);
		if (priceParsed && lowerCase.contains("collect auction")) {
			changeState(BuyState.COLLECT_AUCTION);
		}
	}

	private int buySlotID = -1;
	private boolean priceParsed = false;
	private boolean wonAuction = true;
	private boolean changeProfile = false;

	private void getPriceFromTooltip(List<Component> tooltip) {
		if (priceParsed) return;
		String minBid = null;
		String priceString = null;
		AtomicReference<String> stringAtomicReference = new AtomicReference<>("");

		for (Component text : tooltip) {
			String string = text.getString();
			String thingToLookFor = (isBinAuction) ? "price:" : "new bid:";
			String lowerCase = string.toLowerCase(Locale.ENGLISH);
			if (lowerCase.contains(thingToLookFor)) {
				String[] split = string.split(":");
				if (split.length < 2) continue;
				priceString = split[1].trim();
			} else if (lowerCase.contains("minimum bid:") && !isBinAuction) {
				String[] split = string.split(":");
				if (split.length < 2) continue;
				minBid = split[1].replace("coins", "").replace(",", "").trim();
			} else if (lowerCase.contains("you pay:")) {
				String[] split = string.split(":");
				if (split.length < 2) continue;
				if (buyState != BuyState.CANT_AFFORD && !isBinAuction) {
					infoTextWidget.setMessage(Component.translatable("skyblocker.fancyAuctionHouse.youPay", split[1].trim()));
				}

			} else if (lowerCase.contains("top bid:")) { // Shows up when an auction ended and you lost
				wonAuction = false;
			} else if (lowerCase.contains("correct profile")) { // When an auction ended but on a different profile
				changeProfile = true;
				priceWidget.setMessage(Component.empty());
			} else if (lowerCase.contains("own auction")) { // it's yours
				changeState(BuyState.OWN_AUCTION);
			}
			text.visit((style, asString) -> {
				// The regex removes [, ] and +. To ignore mvp++ rank and orange + in mvp+
				String res = Objects.equals(style.getColor(), TextColor.fromLegacyFormat(ChatFormatting.GOLD)) && !asString.matches(".*[]\\[+].*") && !asString.contains("Collect") ? asString : null;
				return Optional.ofNullable(res);
			}, Style.EMPTY).ifPresent(s -> stringAtomicReference.set(stringAtomicReference.get() + s));
		}

		if (priceString == null) priceString = stringAtomicReference.get();
		if (minBid != null) this.minBid = minBid;
		else this.minBid = priceString;
		priceText = Component.literal(priceString).setStyle(Style.EMPTY.applyFormat(ChatFormatting.BOLD).withColor(ChatFormatting.GOLD));
		priceWidget.setMessage(priceText);
		priceParsed = true;
		updateLayout();
	}

	public PopupScreen getConfirmPurchasePopup(Component title) {
		// This really shouldn't be possible to be null in its ACTUAL use case.
		//noinspection DataFlowIssue
		return new PopupScreen.Builder(this, title)
				.addButton(Component.translatable("text.skyblocker.confirm"), popupScreen -> this.minecraft.gameMode.handleInventoryMouseClick(this.minecraft.player.containerMenu.containerId, 11, 0, ClickType.PICKUP, minecraft.player))
				.addButton(Component.translatable("gui.cancel"), PopupScreen::onClose)
				.setMessage((isBinAuction ? Component.translatable("skyblocker.fancyAuctionHouse.price") : Component.translatable("skyblocker.fancyAuctionHouse.newBid")).append(" ").append(priceText))
				.onClose(() -> {
					// This really shouldn't be possible to be null in its ACTUAL use case.
					//noinspection DataFlowIssue
					this.minecraft.gameMode.handleInventoryMouseClick(this.minecraft.player.containerMenu.containerId, 15, 0, ClickType.PICKUP, minecraft.player);
				})
				.build();
	}

	private enum BuyState {
		CANT_AFFORD,
		AFFORD,
		TOP_BID,
		COLLECT_AUCTION,
		CANCELLABLE_AUCTION,
		OWN_AUCTION
	}
}
