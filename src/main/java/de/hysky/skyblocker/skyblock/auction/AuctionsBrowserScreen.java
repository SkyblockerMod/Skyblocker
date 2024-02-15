package de.hysky.skyblocker.skyblock.auction;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("DataFlowIssue") // For this.client and stuff
public class AuctionsBrowserScreen extends Screen {

    private static final Identifier BACKGROUND_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE,"textures/gui/auctions_gui/browser/background.png");
    private static final Identifier SCROLLER_TEXTURE = new Identifier("container/creative_inventory/scroller");

    private static final Identifier up_arrow_tex = new Identifier(SkyblockerMod.NAMESPACE, "up_arrow_even"); // Put them in their own fields to avoid object allocation on each frame
    private static final Identifier down_arrow_tex = new Identifier(SkyblockerMod.NAMESPACE, "down_arrow_even");
    public static final Supplier<Sprite> UP_ARROW = () -> MinecraftClient.getInstance().getGuiAtlasManager().getSprite(up_arrow_tex);
    public static final Supplier<Sprite> DOWN_ARROW = () -> MinecraftClient.getInstance().getGuiAtlasManager().getSprite(down_arrow_tex);
    private boolean waitingForServer = true;

    // <editor-fold desc="Boring handled screen stuff">
    @Override
    public boolean shouldPause() {
        return false;
    }
    @Override
    public void close() {
        this.client.player.closeHandledScreen();
        super.close();
    }
    @Override
    public void removed() {
        if (this.client.player == null) {
            return;
        }
        ((ScreenHandler)this.handler).onClosed(this.client.player);
    }
    // </editor-fold>

    private GenericContainerScreenHandler handler;
    private final PlayerInventory playerInventory;
    private final List<Slot> auctionedItems = new ArrayList<>(24);
    private @Nullable Slot hoveredSlot = null;
    private int prevPageSlotId = -1;
    private int nextPageSlotId = -1;

    private int currentPage = 1;
    private int totalPages = 1;

    // WIDGETS
    private SortWidget sortWidget;
    private AuctionTypeWidget auctionTypeWidget;
    private RarityWidget rarityWidget;

    public int x = 0;
    public int y = 0;

    public AuctionsBrowserScreen(GenericContainerScreenHandler handler, PlayerInventory playerInventory) {
        super(Text.literal("Auctions Browser"));
        this.handler = handler;
        this.playerInventory = playerInventory;
    }

    private boolean dirty = false;
    private long dirtiedTime;
    public void markDirty() {dirty = true; dirtiedTime = System.currentTimeMillis();}
    public void changeHandlerAndUpdate(GenericContainerScreenHandler handler) {
        this.handler = handler;
        update();
    }

    @Override
    public void tick() {
        if (!this.client.player.isAlive() || this.client.player.isRemoved()) this.client.player.closeHandledScreen();
        if (dirty && System.currentTimeMillis() - dirtiedTime > 50) update();
    }

    public void update() {
        dirty = false;
        waitingForServer = false;
        auctionedItems.clear();
        for (int i = 1; i < 5; i++) {
            for (int j = 2; j < 8; j++) {
                auctionedItems.add(handler.slots.get(i*9 + j));
            }
        }
        nextPageSlotId = -1;
        prevPageSlotId = -1;

        boolean pageParsed = false;
        for (int i = (handler.getRows() - 1) * 9; i < handler.getRows() * 9; i++) {
            Slot slot = handler.slots.get(i);
            ItemStack stack = slot.getStack();
            if (!slot.hasStack()) continue;
            String stackName = stack.getName().getString().toLowerCase();
            if (stack.isOf(Items.HOPPER) && stackName.contains("sort")) {
                sortWidget.setSlotId(i);
                List<Text> tooltip = stack.getTooltip(client.player, TooltipContext.BASIC);
                int ordinal = getOrdinal(tooltip);
                sortWidget.setCurrent(SortWidget.Option.get(ordinal));
            } else if (stackName.contains("bin filter")) {
                auctionTypeWidget.setSlotId(i);
                List<Text> tooltip = stack.getTooltip(client.player, TooltipContext.BASIC);
                int ordinal = getOrdinal(tooltip);
                auctionTypeWidget.setCurrent(AuctionTypeWidget.Option.get(ordinal));
            } else if (stack.isOf(Items.ENDER_EYE) && stackName.contains("item tier")) {
                rarityWidget.setSlotId(i);
                List<Text> tooltip = stack.getTooltip(client.player, TooltipContext.BASIC);
                int ordinal = getOrdinal(tooltip);
                String split = tooltip.get(ordinal+2).getString().substring(2);
                rarityWidget.setText(tooltip.subList(1, tooltip.size()-3), split);
            } else if (stack.isOf(Items.ARROW) && stackName.contains("previous page")) {
                prevPageSlotId = i;
                if (!pageParsed) {
                    parsePage(stack);
                    pageParsed = true;
                }
            } else if (stack.isOf(Items.ARROW) && stackName.contains("next page")) {
                nextPageSlotId = i;
                if (!pageParsed) {
                    parsePage(stack);
                    pageParsed = true;
                }
            }
        }
        if (!pageParsed) {
            currentPage = 1;
            totalPages = 1;
        }
    }

    private void parsePage(ItemStack stack) {
        List<Text> tooltip = stack.getTooltip(client.player, TooltipContext.BASIC);
        String str = tooltip.get(1).getString().trim();
        str = str.substring(1, str.length() - 1); // remove parentheses
        String[] parts = str.split("/"); // split the string
        try {
            currentPage = Integer.parseInt(parts[0]); // parse current page
            totalPages = Integer.parseInt(parts[1]); // parse total
        } catch (NumberFormatException ignored) {}
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

    public void clickAndWaitForServer(int slotID, int button) {
        //System.out.println("hey");
        assert client != null;
        assert client.interactionManager != null;
        client.interactionManager.clickSlot(handler.syncId, slotID, button, SlotActionType.PICKUP, client.player);
        waitingForServer = true;
    }
    public void clickAndWaitForServer(int slotId) { clickAndWaitForServer(slotId, 0);}

    public boolean isWaitingForServer() {
        return waitingForServer;
    }

    @Override
    protected void init() {
        super.init();
        x = (this.width - 176)/2;
        y = (this.height - 187)/2;
        sortWidget = new SortWidget(x + 25, y+81, this); addDrawableChild(sortWidget);
        auctionTypeWidget = new AuctionTypeWidget(x + 134, y + 77, this); addDrawableChild(auctionTypeWidget);
        rarityWidget = new RarityWidget(x + 73, y+80, this); addDrawableChild(rarityWidget);
        markDirty();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // DEBUG
        for (int i = 0; i < handler.slots.size(); i++) {
            context.drawItem(handler.slots.get(i).getStack(), (i % 9) * 16, (i / 9) * 16);
        }

        context.drawCenteredTextWithShadow(textRenderer, "Auction House", this.width/2, 5, Colors.WHITE);
        if (isWaitingForServer()) {
            String s = "Waiting for server...";
            context.drawText(textRenderer, s, this.width - textRenderer.getWidth(s) - 5, this.height - textRenderer.fontHeight - 2, 0xFFFFFFFF, true);
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        RenderSystem.disableDepthTest();
        matrices.translate(x, y, 0);
        int highlightSlotX = -1;
        int highlightSlotY = -1;
        this.hoveredSlot = null;

        // -- ITEMS --
        matrices.push();
        matrices.translate(0, 0, 100);

        // Auctioned Items
        for (int i = 0; i < auctionedItems.size(); i++) {
            int x1 = (i % 8) * 18 + 8;
            int y1 = i / 8 * 18 + 18;
            Slot slot = auctionedItems.get(i);
            if ((x1-1 <= mouseX - x && mouseX - x <= x1+17) && (y1-1 <= mouseY - y && mouseY - y <= y1+17) && slot.hasStack()){
                highlightSlotX = x1;
                highlightSlotY = y1;
                this.hoveredSlot = slot;
            }
            context.drawItem(slot.getStack(), x1, y1);
            context.drawItemInSlot(this.textRenderer, slot.getStack(), x1, y1);
        }
        // Player inv
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                int x1 = 8 + column * 18;
                int y1 = 105 + row * 18;
                context.drawItem(playerInventory.getStack(column + row * 9 + 9), x1, y1);
                context.drawItemInSlot(this.textRenderer, playerInventory.getStack(column + row * 9 + 9), x1, y1);
            }
        }
        for (int slot = 0; slot < 9; ++slot) {
            context.drawItem(playerInventory.getStack(slot), 8 + slot * 18, 163);
            context.drawItemInSlot(this.textRenderer, playerInventory.getStack(slot), 8 + slot * 18, 163);
        }
        matrices.pop();
        // Inventory title
        context.drawText(this.textRenderer, playerInventory.getDisplayName(), 8, 187-94, 0x404040, false);
        if (highlightSlotX != -1) HandledScreen.drawSlotHighlight(context, highlightSlotX, highlightSlotY, 0);

        // Scrollbar
        if (prevPageSlotId != -1) {
            if (onScrollbarTop(mouseX, mouseY))
                context.drawSprite(159, 13, 0, 6, 3, UP_ARROW.get());
            else context.drawSprite(159, 13, 0, 6, 3, UP_ARROW.get(), 0.54f, 0.54f, 0.54f, 1);
        }

        if (nextPageSlotId != -1) {
            if (onScrollbarBottom(mouseX, mouseY))
                context.drawSprite(159, 72, 0, 6, 3, DOWN_ARROW.get());
            else context.drawSprite(159, 72, 0, 6, 3, DOWN_ARROW.get(), 0.54f, 0.54f, 0.54f, 1);
        }
        context.drawText(textRenderer, String.format("%d/%d", currentPage, totalPages), 99, 6, Colors.GRAY, false);
        if (totalPages <= 1)
            context.drawGuiTexture(SCROLLER_TEXTURE, 156, 18, 12, 15);
        else
            context.drawGuiTexture(SCROLLER_TEXTURE, 156, (int) (18 + (float)(currentPage-1)/(totalPages-1)*37), 12, 15);


        matrices.pop();

        if (this.hoveredSlot != null) {
            ItemStack itemStack = hoveredSlot.getStack();
            context.drawTooltip(this.textRenderer, getTooltipFromItem(this.client, itemStack), itemStack.getTooltipData(), mouseX, mouseY);
        }

        RenderSystem.enableDepthTest();
    }

    private boolean onScrollbarTop(int mouseX, int mouseY) {
        int localX = mouseX - x;
        int localY = mouseY - y;
        return localX > 154 && localX < 169 && localY > 11 && localY < 44;
    }
    private boolean onScrollbarBottom(int mouseX, int mouseY) {
        int localX = mouseX - x;
        int localY = mouseY - y;
        return localX > 154 && localX < 169 && localY > 43 && localY < 75;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (waitingForServer) return false;
        if (onScrollbarTop((int)mouseX, (int) mouseY) && prevPageSlotId != -1) {
            clickAndWaitForServer(prevPageSlotId);
            playClickSound();
            return true;
        }
        if (onScrollbarBottom((int)mouseX, (int) mouseY) && nextPageSlotId != -1) {
            clickAndWaitForServer(nextPageSlotId);
            playClickSound();
            return true;
        }
        return false;
    }

    public static void playClickSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderInGameBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, x, y, 0, 0, 176, 187);
    }

    public TextRenderer getTextRender() {return textRenderer;}
}
