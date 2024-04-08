package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.gui.AbstractCustomHypixelGUI;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class AuctionViewScreen extends AbstractCustomHypixelGUI<AuctionHouseScreenHandler> {
    protected static final Identifier BACKGROUND_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/browser/background_view.png");

    public static final int BACK_BUTTON_SLOT = 49;

    DirectionalLayoutWidget verticalLayout = DirectionalLayoutWidget.vertical();

    public final boolean isBinAuction;
    private TextWidget priceWidget;
    private final Text clickToEditBidText = Text.translatable("skyblocker.fancyAuctionHouse.editBid").setStyle(Style.EMPTY.withUnderline(true));

    private TextWidget infoTextWidget;
    public String minBid = "";

    private BuyState buyState = null;
    private MutableText priceText = Text.literal("?");
    private ButtonWidget buyButton;
    private TextWidget priceTextWidget;

    public AuctionViewScreen(AuctionHouseScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundHeight = 187;
        isBinAuction = this.getTitle().getString().toLowerCase().contains("bin");
        playerInventoryTitleY = 93;
        titleX = 5;
        titleY = 4;
    }

    @Override
    protected void init() {
        super.init();
        verticalLayout.spacing(2).getMainPositioner().alignHorizontalCenter();
        priceTextWidget = new TextWidget(isBinAuction ? Text.translatable("skyblocker.fancyAuctionHouse.price") : Text.translatable("skyblocker.fancyAuctionHouse.newBid"), textRenderer).alignCenter();
        verticalLayout.add(priceTextWidget);

        priceWidget = new TextWidget(Text.literal("?"), textRenderer).alignCenter();
        priceWidget.setWidth(textRenderer.getWidth(clickToEditBidText));
        priceWidget.active = true;
        verticalLayout.add(priceWidget);

        infoTextWidget = new TextWidget(Text.literal("Can't Afford"), textRenderer).alignCenter();
        verticalLayout.add(infoTextWidget);

        buyButton = ButtonWidget.builder(isBinAuction ? Text.translatable("skyblocker.fancyAuctionHouse.buy") : Text.translatable("skyblocker.fancyAuctionHouse.bid"), button -> {
            if (buySlotID == -1) return;
            clickSlot(buySlotID);
        }).size(60, 15).build();
        verticalLayout.add(buyButton);
        verticalLayout.forEachChild(this::addDrawableChild);
        updateLayout();

        addDrawableChild(new ButtonWidget.Builder(Text.literal("<"), button -> this.clickSlot(BACK_BUTTON_SLOT))
                .position(x + backgroundWidth - 16, y + 4)
                .size(12, 12)
                .build());


    }

    private void changeState(BuyState newState) {
        if (newState == buyState) return;
        buyState = newState;
        switch (buyState) {
            case CANT_AFFORD -> {
                infoTextWidget.setMessage(Text.translatable("skyblocker.fancyAuctionHouse.cantAfford").withColor(Colors.RED));
                buyButton.active = false;
            }
            case TOP_BID ->
                    infoTextWidget.setMessage(Text.translatable("skyblocker.fancyAuctionHouse.alreadyTopBid").withColor(Colors.LIGHT_YELLOW));
            case AFFORD -> infoTextWidget.setMessage(Text.empty());
            case COLLECT_AUCTION -> {
                infoTextWidget.setMessage(changeProfile ? Text.translatable("skyblocker.fancyAuctionHouse.differentProfile") : wonAuction ? Text.empty() : Text.translatable("skyblocker.fancyAuctionHouse.didntWin"));
                //priceWidget.setMessage(Text.empty());
                priceWidget.active = false;

                if (changeProfile) {
                    buyButton.setMessage(Text.translatable("skyblocker.fancyAuctionHouse.changeProfile").setStyle(Style.EMPTY.withColor(Formatting.AQUA)));
                } else if (wonAuction) {
                    buyButton.setMessage(Text.translatable("skyblocker.fancyAuctionHouse.collectAuction"));
                } else {
                    buyButton.setMessage(Text.translatable("skyblocker.fancyAuctionHouse.collectBid"));
                }
                buyButton.setWidth(textRenderer.getWidth(buyButton.getMessage()) + 4);

                priceTextWidget.setMessage(Text.translatable("skyblocker.fancyAuctionHouse.auctionEnded"));
                priceTextWidget.setWidth(textRenderer.getWidth(priceTextWidget.getMessage()));
            }
            case CANCELLABLE_AUCTION -> {
                buyButton.setMessage(Text.translatable("skyblocker.fancyAuctionHouse.cancelAuction").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                buyButton.setWidth(textRenderer.getWidth(buyButton.getMessage()) + 4);

                buyButton.active = true;
                buyButton.visible = true;
            }
            case OWN_AUCTION -> {
                buyButton.visible = false;
                priceWidget.active = false;

                infoTextWidget.setMessage(Text.translatable("skyblocker.fancyAuctionHouse.yourAuction"));
            }
        }
        infoTextWidget.setWidth(textRenderer.getWidth(infoTextWidget.getMessage()));
        updateLayout();
    }

    private void updateLayout() {
        verticalLayout.refreshPositions();
        SimplePositioningWidget.setPos(verticalLayout, x, y + 36, backgroundWidth, 60);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (isWaitingForServer) context.drawText(textRenderer, "Waiting...", 0, 0, Colors.WHITE, true);

        MatrixStack matrices = context.getMatrices();

        matrices.push();
        matrices.translate(x + 77, y + 14, 0);
        matrices.scale(1.375f, 1.375f, 1.375f);
        //matrices.translate(0, 0, 100f);
        ItemStack stack = handler.getSlot(13).getStack();
        context.drawItem(stack, 0, 0);
        context.drawItemInSlot(textRenderer, stack, 0, 0);
        matrices.pop();

        if (!isBinAuction && buyState != BuyState.COLLECT_AUCTION) {
            if (priceWidget.isMouseOver(mouseX, mouseY) && buyState != BuyState.CANT_AFFORD) {
                priceWidget.setMessage(clickToEditBidText);
            } else {
                priceWidget.setMessage(priceText);
            }
        }

        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int x, int y) {
        super.drawMouseoverTooltip(context, x, y);
        if (x > this.x + 75 && x < this.x + 75 + 26 && y > this.y + 13 && y < this.y + 13 + 26) {
            context.drawTooltip(this.textRenderer, this.getTooltipFromItem(handler.getSlot(13).getStack()), x, y);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isBinAuction && priceWidget.isMouseOver(mouseX, mouseY)) {
            clickSlot(31);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onSlotChange(AuctionHouseScreenHandler handler, int slotId, ItemStack stack) {
        if (stack.isOf(Items.BLACK_STAINED_GLASS_PANE) || slotId == 13) return;
        assert client != null;
        if (stack.isOf(Items.RED_TERRACOTTA)) { // Red terracotta shows up when you can cancel it
            changeState(BuyState.CANCELLABLE_AUCTION);
            buySlotID = slotId;
        }
        if (priceParsed) return;
        if (stack.isOf(Items.POISONOUS_POTATO)) {
            changeState(BuyState.CANT_AFFORD);
            getPriceFromTooltip(stack.getTooltip(client.player, TooltipContext.BASIC));
            buySlotID = slotId;
        } else if (stack.isOf(Items.GOLD_NUGGET)) {
            changeState(BuyState.AFFORD);
            getPriceFromTooltip(stack.getTooltip(client.player, TooltipContext.BASIC));
            buySlotID = slotId;
        } else if (stack.isOf(Items.GOLD_BLOCK)) {
            changeState(BuyState.TOP_BID);
            getPriceFromTooltip(stack.getTooltip(client.player, TooltipContext.BASIC));
            buySlotID = slotId;
        } else if (stack.isOf(Items.NAME_TAG)) {
            getPriceFromTooltip(stack.getTooltip(client.player, TooltipContext.BASIC));
            changeProfile = true;
            buySlotID = slotId;
        }
        String lowerCase = stack.getName().getString().toLowerCase();
        if (priceParsed && lowerCase.contains("collect auction")) {
            changeState(BuyState.COLLECT_AUCTION);
        }
    }

    private int buySlotID = -1;
    private boolean priceParsed = false;
    private boolean wonAuction = true;
    private boolean changeProfile = false;

    private void getPriceFromTooltip(List<Text> tooltip) {
        if (priceParsed) return;
        String minBid = null;
        String priceString = null;
        AtomicReference<String> stringAtomicReference = new AtomicReference<>("");

        for (Text text : tooltip) {
            String string = text.getString();
            String thingToLookFor = (isBinAuction) ? "price:" : "new bid:";
            String lowerCase = string.toLowerCase();
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
                    infoTextWidget.setMessage(Text.translatable("skyblocker.fancyAuctionHouse.youPay", split[1].trim()));
                    infoTextWidget.setWidth(textRenderer.getWidth(infoTextWidget.getMessage()));
                }

            } else if (lowerCase.contains("top bid:")) { // Shows up when an auction ended and you lost
                wonAuction = false;
            } else if (lowerCase.contains("correct profile")) { // When an auction ended but on a different profile
                changeProfile = true;
                priceWidget.setMessage(Text.empty());
            } else if (lowerCase.contains("own auction")) { // it's yours
                changeState(BuyState.OWN_AUCTION);
            }
            text.visit((style, asString) -> {
                // The regex removes [, ] and +. To ignore mvp++ rank and orange + in mvp+
                String res = Objects.equals(style.getColor(), TextColor.fromFormatting(Formatting.GOLD)) && !asString.matches(".*[]\\[+].*") && !asString.contains("Collect") ? asString : null;
                return Optional.ofNullable(res);
            }, Style.EMPTY).ifPresent(s -> stringAtomicReference.set(stringAtomicReference.get() + s));
        }
        //System.out.println("Experiment: " + stringAtomicReference.get());
        if (priceString == null) priceString = stringAtomicReference.get();
        if (minBid != null) this.minBid = minBid;
        else this.minBid = priceString;
        priceText = Text.literal(priceString).setStyle(Style.EMPTY.withFormatting(Formatting.BOLD).withColor(Formatting.GOLD));
        priceWidget.setMessage(priceText);
        int width = textRenderer.getWidth(priceText);
        if (width > priceWidget.getWidth()) priceWidget.setWidth(width);
        priceParsed = true;
        updateLayout();
    }

    public PopupScreen getConfirmPurchasePopup(Text title) {
        // This really shouldn't be possible to be null in its ACTUAL use case.
        //noinspection DataFlowIssue
        return new PopupScreen.Builder(this, title)
                .button(Text.translatable("text.skyblocker.confirm"), popupScreen -> this.client.interactionManager.clickSlot(this.client.player.currentScreenHandler.syncId, 11, 0, SlotActionType.PICKUP, client.player))
                .button(Text.translatable("gui.cancel"), popupScreen -> this.client.interactionManager.clickSlot(this.client.player.currentScreenHandler.syncId, 15, 0, SlotActionType.PICKUP, client.player))
                .message((isBinAuction ? Text.translatable("skyblocker.fancyAuctionHouse.price") : Text.translatable("skyblocker.fancyAuctionHouse.newBid")).append(" ").append(priceText)).build();
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
