package de.hysky.skyblocker.skyblock.auction;

import com.google.gson.JsonElement;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.auction.widgets.AuctionTypeWidget;
import de.hysky.skyblocker.skyblock.auction.widgets.CategoryTabWidget;
import de.hysky.skyblocker.skyblock.auction.widgets.RarityWidget;
import de.hysky.skyblocker.skyblock.auction.widgets.SortWidget;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.gui.AbstractCustomHypixelGUI;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AuctionBrowserScreen extends AbstractCustomHypixelGUI<AuctionHouseScreenHandler> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionBrowserScreen.class);
    private static final Identifier TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/browser/background.png");
    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/creative_inventory/scroller");

    private static final Identifier up_arrow_tex = Identifier.of(SkyblockerMod.NAMESPACE, "up_arrow_even"); // Put them in their own fields to avoid object allocation on each frame
    private static final Identifier down_arrow_tex = Identifier.of(SkyblockerMod.NAMESPACE, "down_arrow_even");
    public static final Supplier<Sprite> UP_ARROW = () -> MinecraftClient.getInstance().getGuiAtlasManager().getSprite(up_arrow_tex);
    public static final Supplier<Sprite> DOWN_ARROW = () -> MinecraftClient.getInstance().getGuiAtlasManager().getSprite(down_arrow_tex);


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
    private ButtonWidget resetFiltersButton;
    private final List<CategoryTabWidget> categoryTabWidgets = new ArrayList<>(6);
    private String search = "";

    public AuctionBrowserScreen(AuctionHouseScreenHandler handler, PlayerInventory inventory) {
        super(handler, inventory, Text.literal("Auctions Browser"));
        this.backgroundHeight = 187;
        this.playerInventoryTitleY = 92;
        this.titleX = 999;
    }

    @Override
    protected void init() {
        super.init();
        sortWidget = new SortWidget(x + 25, y + 81, this::clickSlot);
        sortWidget.setSlotId(SORT_BUTTON_SLOT);
        addDrawableChild(sortWidget);
        auctionTypeWidget = new AuctionTypeWidget(x + 134, y + 77, this::clickSlot);
        auctionTypeWidget.setSlotId(AUCTION_TYPE_BUTTON_SLOT);
        addDrawableChild(auctionTypeWidget);
        rarityWidget = new RarityWidget(x + 73, y + 80, this::clickSlot);
        rarityWidget.setSlotId(RARITY_BUTTON_SLOT);
        addDrawableChild(rarityWidget);
        resetFiltersButton = new ScaledTextButtonWidget(x + 10, y + 77, 12, 12, Text.literal("↻"), this::onResetPressed);
        addDrawableChild(resetFiltersButton);
        resetFiltersButton.setTooltip(Tooltip.of(Text.literal("Reset Filters")));
        resetFiltersButton.setTooltipDelay(Duration.ofMillis(500));

        addDrawableChild(new ButtonWidget.Builder(Text.literal("<"), button -> this.clickSlot(BACK_BUTTON_SLOT))
                .position(x + 98, y + 4)
                .size(12, 12)
                .build());

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

    private void onResetPressed(ButtonWidget buttonWidget) {
        buttonWidget.setFocused(false); // Annoying.
        this.clickSlot(RESET_BUTTON_SLOT, 0);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        for (CategoryTabWidget categoryTabWidget : categoryTabWidgets) {
            categoryTabWidget.render(context, mouseX, mouseY, delta);
        }
        if (isWaitingForServer) {
            String waiting = "Waiting for server...";
            context.drawText(textRenderer, waiting, this.width - textRenderer.getWidth(waiting) - 5, this.height - textRenderer.fontHeight - 2, Colors.WHITE, true);
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0);
        // Search
        context.enableScissor(x + 7, y + 4, x + 97, y + 16);
        context.drawText(textRenderer, Text.literal(search).fillStyle(Style.EMPTY.withUnderline(onSearchField(mouseX, mouseY))), 9, 6, Colors.WHITE, true);
        context.disableScissor();

        // Scrollbar
        if (prevPageVisible) {
            if (onScrollbarTop(mouseX, mouseY))
                context.drawSprite(159, 13, 0, 6, 3, UP_ARROW.get());
            else context.drawSprite(159, 13, 0, 6, 3, UP_ARROW.get(), 0.54f, 0.54f, 0.54f, 1);
        }

        if (nextPageVisible) {
            if (onScrollbarBottom(mouseX, mouseY))
                context.drawSprite(159, 72, 0, 6, 3, DOWN_ARROW.get());
            else context.drawSprite(159, 72, 0, 6, 3, DOWN_ARROW.get(), 0.54f, 0.54f, 0.54f, 1);
        }
        context.drawText(textRenderer, String.format("%d/%d", currentPage, totalPages), 111, 6, Colors.GRAY, false);
        if (totalPages <= 1)
            context.drawGuiTexture(SCROLLER_TEXTURE, 156, 18, 12, 15);
        else
            context.drawGuiTexture(SCROLLER_TEXTURE, 156, (int) (18 + (float) (Math.min(currentPage, totalPages) - 1) / (totalPages - 1) * 37), 12, 15);

        matrices.pop();

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawSlot(DrawContext context, Slot slot) {
        if (SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.highlightCheapBIN && slot.hasStack() && isSlotHighlighted.getOrDefault(slot.id, false)) {
            context.drawBorder(slot.x, slot.y, 16, 16, new Color(0, 255, 0, 100).getRGB());
        }
        super.drawSlot(context, slot);
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        if (slotId >= handler.getRows() * 9) return;
        super.onMouseClick(slot, slotId, button, actionType);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isWaitingForServer) return super.mouseClicked(mouseX, mouseY, button);
        if (onScrollbarTop((int) mouseX, (int) mouseY) && prevPageVisible) {
            clickSlot(PREV_PAGE_BUTTON);
            return true;
        }
        if (onScrollbarBottom((int) mouseX, (int) mouseY) && nextPageVisible) {
            clickSlot(NEXT_PAGE_BUTTON);
            return true;
        }

        if (onSearchField((int) mouseX, (int) mouseY)) {
            clickSlot(SEARCH_BUTTON_SLOT);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean onScrollbarTop(int mouseX, int mouseY) {
        int localX = mouseX - x;
        int localY = mouseY - y;
        return localX > 154 && localX < 169 && localY > 6 && localY < 44;
    }

    private boolean onScrollbarBottom(int mouseX, int mouseY) {
        int localX = mouseX - x;
        int localY = mouseY - y;
        return localX > 154 && localX < 169 && localY > 43 && localY < 80;
    }

    private boolean onSearchField(int mouseX, int mouseY) {
        int localX = mouseX - x;
        int localY = mouseY - y;
        return localX > 6 && localX < 97 && localY > 3 && localY < 16;
    }

    @Override
    public void onSlotChange(AuctionHouseScreenHandler handler, int slotId, ItemStack stack) {
        if (client == null || stack.isEmpty()) return;
        isWaitingForServer = false;

        switch (slotId) {
            case PREV_PAGE_BUTTON -> {
                prevPageVisible = false;
                if (stack.isOf(Items.ARROW)) {
                    prevPageVisible = true;
                    parsePage(stack);
                }
            }
            case NEXT_PAGE_BUTTON -> {
                nextPageVisible = false;
                if (stack.isOf(Items.ARROW)) {
                    nextPageVisible = true;
                    parsePage(stack);
                }
            }
            case SORT_BUTTON_SLOT ->
                    sortWidget.setCurrent(SortWidget.Option.get(getOrdinal(ItemUtils.getLore(stack))));
            case AUCTION_TYPE_BUTTON_SLOT ->
                    auctionTypeWidget.setCurrent(AuctionTypeWidget.Option.get(getOrdinal(ItemUtils.getLore(stack))));
            case RARITY_BUTTON_SLOT -> {
                List<Text> tooltip = ItemUtils.getLore(stack);
                int ordinal = getOrdinal(tooltip);
                String split = tooltip.get(ordinal + 1).getString().substring(2);
                rarityWidget.setText(tooltip.subList(1, tooltip.size() - 3), split);
            }
            case RESET_BUTTON_SLOT -> {
                if (resetFiltersButton != null)
                    resetFiltersButton.active = handler.getSlot(slotId).getStack().isOf(Items.ANVIL);
            }
            case SEARCH_BUTTON_SLOT -> {
                List<Text> tooltipSearch = ItemUtils.getLore(stack);
                for (Text text : tooltipSearch) {
                    String string = text.getString();
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
                if (slotId < this.handler.getRows() * 9 && slotId % 9 == 0) {
                    CategoryTabWidget categoryTabWidget = categoryTabWidgets.get(slotId / 9);
                    categoryTabWidget.setSlotId(slotId);
                    categoryTabWidget.setIcon(handler.getSlot(slotId).getStack());
                    List<Text> tooltipDefault = ItemUtils.getLore(handler.getSlot(slotId).getStack());
                    for (int j = tooltipDefault.size() - 1; j >= 0; j--) {
                        String lowerCase = tooltipDefault.get(j).getString().toLowerCase();
                        if (lowerCase.contains("currently")) {
                            categoryTabWidget.setToggled(true);
                            break;
                        } else if (lowerCase.contains("click")) {
                            categoryTabWidget.setToggled(false);
                            break;
                        } else categoryTabWidget.setToggled(false);
                    }
                } else if (slotId > 9 && slotId < (handler.getRows() - 1) * 9 && slotId % 9 > 1 && slotId % 9 < 8) {
                    if (!SkyblockerConfigManager.get().uiAndVisuals.fancyAuctionHouse.highlightCheapBIN) return;
                    List<Text> tooltip = ItemUtils.getLore(stack);
                    for (int k = tooltip.size() - 1; k >= 0; k--) {
                        Text text = tooltip.get(k);
                        String string = text.getString();
                        if (string.toLowerCase().contains("buy it now:")) {
                            String[] split = string.split(":");
                            if (split.length < 2) continue;
                            String coins = split[1].replace(",", "").replace("coins", "").trim();
                            try {
                                long parsed = Long.parseLong(coins);
                                double price = TooltipInfoType.THREE_DAY_AVERAGE.getData().getDouble(stack.getNeuName());
                                isSlotHighlighted.put(slotId, price > parsed);
                            } catch (Exception e) {
                                LOGGER.error("[Skyblocker Fancy Auction House] Failed to parse BIN price", e);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP && prevPageVisible) {
            clickSlot(PREV_PAGE_BUTTON);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN && nextPageVisible) {
            clickSlot(NEXT_PAGE_BUTTON);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static int getOrdinal(List<Text> tooltip) {
        int ordinal = 0;
        for (int j = 0; j < tooltip.size() - 4; j++) {
            if (j + 1 >= tooltip.size()) break;
            if (tooltip.get(j + 1).getString().contains("▶")) {
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
        assert client != null;
        try {
            List<Text> tooltip = ItemUtils.getLore(stack);
            String str = tooltip.getFirst().getString().trim();
            str = str.substring(1, str.length() - 1); // remove parentheses
            String[] parts = str.split("/"); // split the string
            currentPage = Integer.parseInt(parts[0].replace(",", "")); // parse current page
            totalPages = Integer.parseInt(parts[1].replace(",", "")); // parse total
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Fancy Auction House] Failed to parse page arrow", e);
        }
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        return mouseX < (double) left - 32 || mouseY < (double) top || mouseX >= (double) (left + this.backgroundWidth) || mouseY >= (double) (top + this.backgroundHeight);
    }

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
            pose.translate(((this.getX() + this.width / 2f) - font.getWidth(getMessage()) * textScale / 2) + 1, (float) this.getY() + (this.height - font.fontHeight * textScale) / 2f - 1, 0);
            pose.scale(textScale, textScale, 1);
            graphics.drawText(font, getMessage(), 0, 0, color | MathHelper.ceil(this.alpha * 255.0F) << 24, true);
            pose.pop();
        }
    }
}
