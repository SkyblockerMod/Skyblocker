package de.hysky.skyblocker.skyblock.auction;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.gui.HandlerSignBackedScreen;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class AuctionViewScreen extends HandlerSignBackedScreen {

    private static final Identifier BACKGROUND_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE,"textures/gui/auctions_gui/browser/background_view.png");
    private static final ItemStack SPONG = new ItemStack(Items.SPONGE);
    private final PlayerInventory playerInventory;
    private ItemStack auctionedItem = SPONG;
    private final ViewType viewType;
    private BuyState buyState = BuyState.CANT_AFFORD;
    private String endsIn = "";
    private String priceString = "";
    private String minBid = "";

    private int backSlotId = -1;
    private int buySlotId = -1;
    private int editBidSlotId = -1;
    private ButtonWidget buyButton;
    private ButtonWidget editBid;
    private boolean openingPopup = false;
    private boolean closingPopup = false;

    public int x = 0;
    public int y = 0;


    static {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (message.getString().toLowerCase().contains("couldn't read this number") && MinecraftClient.getInstance().currentScreen instanceof AuctionViewScreen) {
                assert MinecraftClient.getInstance().player != null;
                MinecraftClient.getInstance().player.closeScreen();
            }
        });
    }

    public AuctionViewScreen(PlayerInventory playerInventory, Text inventoryName, GenericContainerScreenHandler handler) {
        super(Text.literal("Auction View"), inventoryName, handler);
        this.playerInventory = playerInventory;
        if (this.inventoryName.getString().toLowerCase().contains("bin"))
            viewType = ViewType.BIN;
        else viewType = ViewType.AUCTION;
    }

    @Override
    protected void init() {
        super.init();
        openingPopup = false;
        x = (this.width - 176)/2;
        y = (this.height - 187)/2;
        if (closingPopup) {
            assert this.client != null;
            this.client.setScreen(null);
            return;
        }
        MutableText message = Text.literal("Back");
        ButtonWidget buttonWidget = ButtonWidget.builder(message, this::backClick)
                .position(x + 5, y + 5)
                .size(textRenderer.getWidth(message) + 6, 12)
                .build();
        addDrawableChild(buttonWidget);

        MutableText editBidText = Text.literal("Edit bid");
        int buttonWidth = textRenderer.getWidth(editBidText) + 6;
        this.editBid = ButtonWidget.builder(editBidText, this::editBidClick)
                .position(x + 88 - buttonWidth / 2, y + 40 + textRenderer.fontHeight * 2)
                .size(buttonWidth, 12)
                .build();
        this.editBid.visible = false;
        addDrawableChild(editBid);

        MutableText message1 = (viewType == ViewType.AUCTION ? Text.literal("BID!") : Text.literal("BUY!"))
                .fillStyle(Style.EMPTY.withBold(true));
        buttonWidth = textRenderer.getWidth(message1) + 8;
        buyButton = ButtonWidget.builder(message1.fillStyle(Style.EMPTY.withColor(Formatting.GOLD)), this::buyClick)
                .position(-buttonWidth/2 + x+88, editBid.getBottom()+2)
                .size(buttonWidth, 15)
                .build();
        addDrawableChild(buyButton);
        buyButton.active = false;



    }

    protected void backClick(ButtonWidget buttonWidget) {
        if (backSlotId == -1 || isWaitingForServer()) return;
        clickAndWaitForServer(backSlotId);
    }
    protected void buyClick(ButtonWidget buttonWidget) {
        if (buySlotId == -1 || isWaitingForServer()) return;
        clickAndWaitForServer(buySlotId);
    }

    protected void editBidClick(ButtonWidget buttonWidget) {
        if (editBidSlotId == -1 || isWaitingForServer()) return;
        clickAndWaitForServer(editBidSlotId);
    }

    @Override
    protected boolean update() {
        if (inventoryName.getString().toLowerCase().trim().contains("confirm")) {
            return updateConfirmPurchase();
        }

        auctionedItem = handler.slots.get(13).getStack();
        assert this.client != null;
        List<Text> tooltip = auctionedItem.getTooltip(this.client.player, TooltipContext.BASIC);
        for (int i = tooltip.size() - 1; i >= 0; i--) { // Reverse in case the item lore is long as hell
            String string = tooltip.get(i).getString();
            if (string.toLowerCase().contains("ends in:")) {
                String[] split = string.split(":");
                if (split.length < 2) continue;
                endsIn = split[1].trim();
                break;
            }
        }

        List<Slot> collect = handler.slots.stream().filter(slot -> !slot.getStack().isOf(Items.BLACK_STAINED_GLASS_PANE)).toList();
        for (Slot slot : collect) {
            ItemStack stack = slot.getStack();
            boolean buyItemSlot = false;
            if (slot.id == 13) continue;
            if (stack.isOf(Items.POISONOUS_POTATO)) {
                buyItemSlot = true;
                buyState = BuyState.CANT_AFFORD;
            } else if (stack.isOf(Items.GOLD_NUGGET)) {
                buyItemSlot = true;
                buyState = BuyState.AFFORD;
                buyButton.active = true;
            } else if (stack.isOf(Items.GOLD_BLOCK)) {
                buyItemSlot = true;
                buyState = BuyState.TOP_BID;

            } else if (stack.isOf(Items.ARROW) && stack.getName().getString().toLowerCase().contains("back")) {
                backSlotId = slot.id;
            } else if (stack.isOf(Items.GOLD_INGOT)) {
                editBidSlotId = slot.id;
                editBid.visible = true;
            }

            if (buyItemSlot) {
                tooltip = stack.getTooltip(this.client.player, TooltipContext.BASIC);
                buySlotId = slot.id;
                String minBid = null;
                for (Text text : tooltip) {
                    String string = text.getString();
                    String thingToLookFor = (viewType == ViewType.BIN) ? "price:" : "new bid:";
                    if (string.toLowerCase().contains(thingToLookFor)) {
                        String[] split = string.split(":");
                        if (split.length < 2) continue;
                        priceString = split[1].trim();
                        break;
                    } else if (string.toLowerCase().contains("minimum bid:") && viewType == ViewType.AUCTION) {
                        String[] split = string.split(":");
                        if (split.length < 2) continue;
                        minBid = split[1].replace("coins", "").replace(",", "").trim();
                    }
                }
                if (minBid != null) this.minBid = minBid;
                else this.minBid = priceString;
            }

        }
        return true;
    }

    private boolean updateConfirmPurchase() {
        List<Slot> collect = handler.slots.stream().filter(slot -> !slot.getStack().isOf(Items.BLACK_STAINED_GLASS_PANE)).toList();
        int confirmSlot = -1;
        int cancelSlot = -1;
        for (Slot slot : collect) {
            ItemStack stack = slot.getStack();
            if (stack.isOf(Items.GREEN_TERRACOTTA) && stack.getName().getString().toLowerCase().contains("confirm")) {
                confirmSlot = slot.id;
            } else if (stack.isOf(Items.RED_TERRACOTTA) && stack.getName().getString().toLowerCase().contains("cancel")) {
                cancelSlot = slot.id;
            }
        }
        if (confirmSlot == -1 || cancelSlot == -1) return false;
        int finalConfirmSlot = confirmSlot;
        int finalCancelSlot = cancelSlot;

        openingPopup = true;
        assert client != null;
        client.setScreen(new PopupScreen.Builder(this, Text.literal("Confirm Bid/Purchase"))
                .button(Text.literal("Yes"), popupScreen -> {
                    clickAndWaitForServer(finalConfirmSlot);
                    popupScreen.close();
                })
                .button(Text.literal("Cancel"), popupScreen -> {
                    clickAndWaitForServer(finalCancelSlot);
                    popupScreen.close();
                })
                .onClosed(() -> {
                    if (isWaitingForServer()) return;
                    assert this.client.player != null;
                    this.client.player.closeHandledScreen();
                    closingPopup = true;

                })
                .build()
        );
        return true;
    }

    @Override
    protected void onSignUpdate() {
        if (sign != null) {
            assert client != null;
            openingPopup = true;
            client.setScreen(new EditBidPopup(this, sign, minBid));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // DEBUG
        for (int i = 0; i < handler.slots.size(); i++) {
            context.drawItem(handler.slots.get(i).getStack(), (i % 9) * 16, (i / 9) * 16);
        }

        RenderSystem.disableDepthTest();
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0);

        drawCenteredStringWithoutShadow(context, textRenderer, inventoryName, 88, 4, Colors.GRAY);

        matrices.push();
        matrices.translate(77, 14, 0);
        matrices.scale(1.375f, 1.375f, 1.375f);
        //matrices.translate(0, 0, 100f);
        context.drawItem(auctionedItem, 0, 0);
        context.drawItemInSlot(textRenderer, auctionedItem, 0, 0);
        matrices.pop();


        String name = (viewType == ViewType.BIN) ? "Price:" : "New bid:";
        context.drawCenteredTextWithShadow(textRenderer, name, 88, 38, Colors.WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(priceString).fillStyle(Style.EMPTY.withColor(Formatting.GOLD)), 88, 39+textRenderer.fontHeight, Colors.WHITE);


        if (buyState == BuyState.CANT_AFFORD) drawCenteredStringWithoutShadow(context, textRenderer, Text.literal("Can't afford!"), 88, editBid.getY()-y, Formatting.DARK_RED.getColorValue());
        else if (buyState == BuyState.TOP_BID) drawCenteredStringWithoutShadow(context, textRenderer, Text.literal("Already to bid!"), 88, editBid.getY()-y, Formatting.DARK_GREEN.getColorValue());
        context.drawText(textRenderer, endsIn, 170 - textRenderer.getWidth(endsIn), 93, Colors.GRAY, false);


        renderPlayerInventory(context, playerInventory, textRenderer, 8, 105, mouseX-x, mouseY-y);
        context.drawText(this.textRenderer, playerInventory.getDisplayName(), 8, 93, 0x404040, false);
        matrices.pop();


        if (mouseX-x > 75 && mouseX-x < 100 && mouseY-y > 12 && mouseY-y < 37) {
            assert client != null;
            context.drawTooltip(textRenderer, auctionedItem.getTooltip(client.player, TooltipContext.BASIC), mouseX, mouseY);
        }
        RenderSystem.enableDepthTest();
    }
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderInGameBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, 176, 187);
    }

    @Override
    public void removed() {
        if (!openingPopup) super.removed();
    }

    public enum ViewType {
        AUCTION,
        BIN
    }

    private enum BuyState {
        CANT_AFFORD,
        AFFORD,
        TOP_BID
    }
}
