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
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class AuctionViewScreen extends AbstractCustomHypixelGUI<AuctionHouseScreenHandler> {
    protected static final Identifier BACKGROUND_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/browser/background_view.png");

    public static final int BACK_BUTTON_SLOT = 49;

    DirectionalLayoutWidget verticalLayout = DirectionalLayoutWidget.vertical();

    public final boolean isBinAuction;
    private TextWidget priceWidget;
    private final Text clickToEditBidText = Text.literal("Click to edit Bid!").setStyle(Style.EMPTY.withUnderline(true));

    private TextWidget cantAffordText;
    public String minBid = "";

    private BuyState buyState = null;
    private MutableText priceText = Text.literal("?");
    private ButtonWidget buyButton;

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
        verticalLayout.add(new TextWidget(Text.literal(isBinAuction ? "Price:" : "New Bid:"), textRenderer).alignCenter());

        priceWidget = new TextWidget(Text.literal("?"), textRenderer).alignCenter();
        priceWidget.setWidth(textRenderer.getWidth(clickToEditBidText));
        priceWidget.active = true;
        verticalLayout.add(priceWidget);

        cantAffordText = new TextWidget(Text.literal("Can't Afford"), textRenderer).alignCenter();
        verticalLayout.add(cantAffordText);

        buyButton = ButtonWidget.builder(Text.literal(isBinAuction ? "Buy!" : "Bid!"), button -> {
            if (buySlotID == -1) return;
            clickSlot(buySlotID);
        }).size(50, 12).build();
        verticalLayout.add(buyButton);
        verticalLayout.forEachChild(this::addDrawableChild);
        updateLayout();

        addDrawableChild(new ButtonWidget.Builder( Text.literal("<"), button -> this.clickSlot(BACK_BUTTON_SLOT))
                .position(x + backgroundWidth - 16, y+4)
                .size(12, 12)
                .build());


    }

    private void changeState(BuyState newState) {
        if (newState == buyState) return;
        buyState = newState;
        switch (buyState) {
            case CANT_AFFORD -> cantAffordText.setMessage(Text.literal("Can't Afford!").withColor(Colors.RED));
            case TOP_BID -> cantAffordText.setMessage(Text.literal("Already top bid!").withColor(Colors.LIGHT_YELLOW));
            case AFFORD -> cantAffordText.setMessage(Text.empty());
        }
        cantAffordText.setWidth(textRenderer.getWidth(cantAffordText.getMessage()));
        buyButton.active = buyState != BuyState.CANT_AFFORD;
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

        if (!isBinAuction) {
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
        }
    }

    private int buySlotID = -1;
    private boolean priceParsed = false;

    private void getPriceFromTooltip(List<Text> tooltip) {
        if (priceParsed) return;
        String minBid = null;
        String priceString = "???";
        for (Text text : tooltip) {
            String string = text.getString();
            String thingToLookFor = (isBinAuction) ? "price:" : "new bid:";
            if (string.toLowerCase().contains(thingToLookFor)) {
                String[] split = string.split(":");
                if (split.length < 2) continue;
                priceString = split[1].trim();
                break;
            } else if (string.toLowerCase().contains("minimum bid:") && !isBinAuction) {
                String[] split = string.split(":");
                if (split.length < 2) continue;
                minBid = split[1].replace("coins", "").replace(",", "").trim();
            }
        }
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
                .button(Text.literal("Confirm"), popupScreen -> this.client.interactionManager.clickSlot(this.client.player.currentScreenHandler.syncId, 11, 0, SlotActionType.PICKUP, client.player))
                .button(Text.literal("Cancel"), popupScreen -> this.client.interactionManager.clickSlot(this.client.player.currentScreenHandler.syncId, 15, 0, SlotActionType.PICKUP, client.player))
                .message(Text.literal(isBinAuction ? "Price: " : "New Bid: ").append(priceText)).build();
    }

    private enum BuyState {
        CANT_AFFORD,
        AFFORD,
        TOP_BID
    }
}
