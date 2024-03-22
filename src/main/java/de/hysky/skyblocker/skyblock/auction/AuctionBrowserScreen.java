package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.auction.widgets.AuctionTypeWidget;
import de.hysky.skyblocker.skyblock.auction.widgets.CategoryTabWidget;
import de.hysky.skyblocker.skyblock.auction.widgets.RarityWidget;
import de.hysky.skyblocker.skyblock.auction.widgets.SortWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AuctionBrowserScreen extends HandledScreen<AuctionHouseScreenHandler> implements ScreenHandlerListener {
    protected static final Identifier TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/browser/background.png");

    // WIDGETS
    private SortWidget sortWidget;
    private AuctionTypeWidget auctionTypeWidget;
    private RarityWidget rarityWidget;
    private ButtonWidget resetFiltersButton;
    private final List<CategoryTabWidget> categoryTabWidgets = new ArrayList<>(6);

    public static int RESET_SLOT_ID = 45;


    boolean isWaitingForServer = false;

    public AuctionBrowserScreen(AuctionHouseScreenHandler handler, PlayerInventory inventory) {
        super(handler, inventory, Text.literal("Auctions Browser"));
        this.backgroundHeight = 187;
        this.playerInventoryTitleY = 92;
        this.titleX = 999;
        handler.addListener(this);
    }

    @Override
    protected void init() {
        super.init();
        x = (this.width - 176) / 2;
        y = (this.height - 187) / 2;
        sortWidget = new SortWidget(x + 25, y + 81, this::clickSlot);
        sortWidget.setSlotId(50);
        addDrawableChild(sortWidget);
        auctionTypeWidget = new AuctionTypeWidget(x + 134, y + 77, this::clickSlot);
        auctionTypeWidget.setSlotId(52);
        addDrawableChild(auctionTypeWidget);
        rarityWidget = new RarityWidget(x + 73, y + 80, this::clickSlot);
        rarityWidget.setSlotId(51);
        addDrawableChild(rarityWidget);
        resetFiltersButton = new ScaledTextButtonWidget(x + 10, y + 77, 12, 12, Text.literal("↻"), this::onResetPressed);
        addDrawableChild(resetFiltersButton);
        resetFiltersButton.setTooltip(Tooltip.of(Text.literal("Reset Filters")));
        resetFiltersButton.setTooltipDelay(500);
        if (categoryTabWidgets.isEmpty())
            for (int i = 0; i < 6; i++) {
                CategoryTabWidget categoryTabWidget = new CategoryTabWidget(new ItemStack(Items.SPONGE), this::clickSlot);
                categoryTabWidgets.add(categoryTabWidget);
                addSelectableChild(categoryTabWidget); // This method only makes it clickable, does not add it to the drawables list
                // manually rendered in the render method to have it not render behind the durability bars
                categoryTabWidget.setPosition(x - 30, y + 3 + i * 28);
            }
        else
            for (int i = 0; i < categoryTabWidgets.size(); i++) {
                CategoryTabWidget categoryTabWidget = categoryTabWidgets.get(i);
                categoryTabWidget.setPosition(x - 30, y + 3 + i * 28);

            }
    }

    protected void clickSlot(int slotID, int button) {
        if (isWaitingForServer) return;
        if (client == null) return;
        assert this.client.interactionManager != null;
        this.client.interactionManager.clickSlot(handler.syncId, slotID, button, SlotActionType.PICKUP, client.player);
    }

    private void onResetPressed(ButtonWidget buttonWidget) {
        buttonWidget.setFocused(false); // Annoying.
        if (RESET_SLOT_ID == -1) return;
        this.clickSlot(RESET_SLOT_ID, 0);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private static int getOrdinal(List<Text> tooltip) {
        int ordinal = 0;
        for (int j = 0; j < tooltip.size()-3; j++) {
            if (j+2 >= tooltip.size()) break;
            if (tooltip.get(j+2).getString().contains("▶")) {
                ordinal = j;
                break;
            }
        }
        return ordinal;
    }

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        if (client == null || stack.isEmpty()) return;
        if (slotId == 50) {
            sortWidget.setCurrent(SortWidget.Option.get(getOrdinal(stack.getTooltip(client.player, TooltipContext.BASIC))));
        } else if (slotId == 52) {
            auctionTypeWidget.setCurrent(AuctionTypeWidget.Option.get(getOrdinal(stack.getTooltip(client.player, TooltipContext.BASIC))));
        } else if (slotId == 51) {
            List<Text> tooltip = stack.getTooltip(client.player, TooltipContext.BASIC);
            int ordinal = getOrdinal(tooltip);
            String split = tooltip.get(ordinal+2).getString().substring(2);
            rarityWidget.setText(tooltip.subList(1, tooltip.size()-3), split);
        } else if (slotId == 45) {
            if (resetFiltersButton != null) resetFiltersButton.active = handler.getSlot(slotId).getStack().isOf(Items.ANVIL);
        }
    }

    @Override
    public void removed() {
        super.removed();
        handler.removeListener(this);
    }

    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {}

    private static class ScaledTextButtonWidget extends ButtonWidget {

        protected ScaledTextButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
            super(x, y, width, height, message, onPress, Supplier::get);
        }

        // Code taken mostly from YACL by isxander. Love you <3
        @Override
        public void drawMessage(DrawContext graphics, TextRenderer textRenderer, int color) {
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            MatrixStack pose = graphics.getMatrices();
            float textScale = 2.f;

            pose.push();
            pose.translate(((this.getX() + this.width / 2f) - font.getWidth(getMessage()) * textScale / 2) + 1, (float)this.getY() + (this.height - font.fontHeight * textScale) / 2f - 1, 0);
            pose.scale(textScale, textScale, 1);
            graphics.drawText(font, getMessage(), 0, 0, color | MathHelper.ceil(this.alpha * 255.0F) << 24, true);
            pose.pop();
        }
    }
}
