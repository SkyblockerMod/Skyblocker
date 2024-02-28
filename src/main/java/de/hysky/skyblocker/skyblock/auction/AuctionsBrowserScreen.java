package de.hysky.skyblocker.skyblock.auction;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipInfoType;
import de.hysky.skyblocker.utils.render.gui.HandlerBackedScreen;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("DataFlowIssue") // For this.client and stuff
public class AuctionsBrowserScreen extends HandlerBackedScreen {

    private static final Identifier BACKGROUND_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE,"textures/gui/auctions_gui/browser/background.png");
    private static final Identifier SCROLLER_TEXTURE = new Identifier("container/creative_inventory/scroller");

    private static final Identifier up_arrow_tex = new Identifier(SkyblockerMod.NAMESPACE, "up_arrow_even"); // Put them in their own fields to avoid object allocation on each frame
    private static final Identifier down_arrow_tex = new Identifier(SkyblockerMod.NAMESPACE, "down_arrow_even");
    public static final Supplier<Sprite> UP_ARROW = () -> MinecraftClient.getInstance().getGuiAtlasManager().getSprite(up_arrow_tex);
    public static final Supplier<Sprite> DOWN_ARROW = () -> MinecraftClient.getInstance().getGuiAtlasManager().getSprite(down_arrow_tex);
    private final PlayerInventory playerInventory;
    private final List<Slot> auctionedItems = new ArrayList<>(24);
    private final Int2BooleanOpenHashMap highlight = new Int2BooleanOpenHashMap(24);
    private @Nullable Slot hoveredSlot = null;
    private int prevPageSlotId = -1;
    private int nextPageSlotId = -1;
    private int searchSlotId = -1;
    private String search = "";
    private int resetSlotId = -1;

    private int currentPage = 1;
    private int totalPages = 1;

    // WIDGETS
    private SortWidget sortWidget;
    private AuctionTypeWidget auctionTypeWidget;
    private RarityWidget rarityWidget;
    private ButtonWidget resetFiltersButton;

    private final List<CategoryTabWidget> categoryTabWidgets = new ArrayList<>(6);

    public int x = 0;
    public int y = 0;

    public AuctionsBrowserScreen(GenericContainerScreenHandler handler, PlayerInventory playerInventory, Text inventoryName) {
        super(Text.literal("Auctions Browser"), inventoryName, handler);
        this.handler = handler;
        this.playerInventory = playerInventory;
    }

    public boolean update() {

        // AUCTION ITEMS
        auctionedItems.clear();
        highlight.clear();
        for (int i = 1; i < 5; i++) {
            for (int j = 2; j < 8; j++) {
                Slot slot = handler.slots.get(i * 9 + j);
                auctionedItems.add(slot);
                ItemStack stack = slot.getStack();
                List<Text> tooltip = stack.getTooltip(client.player, TooltipContext.BASIC);
                for (int k = tooltip.size() - 1; k >= 0; k--) {
                    Text text = tooltip.get(k);
                    String string = text.getString();
                    if (string.toLowerCase().contains("buy it now:")) {
                        String[] split = string.split(":");
                        if (split.length < 2) continue;
                        String coins = split[1].replace(",", "").replace("coins", "").trim();
                        try {
                            int parsed = Integer.parseInt(coins);
                            String name = ItemTooltip.getInternalNameFromNBT(stack, false);
                            String internalID = ItemTooltip.getInternalNameFromNBT(stack, true);
                            String neuName = name;
                            if (name == null || internalID == null) break;
                            if (name.startsWith("ISSHINY_")) {
                                neuName = internalID;
                            }
                            JsonElement jsonElement = TooltipInfoType.THREE_DAY_AVERAGE.getData().get(ItemTooltip.getNeuName(internalID, neuName));
                            if (jsonElement == null) break;
                            else {
                                highlight.put(slot.id, jsonElement.getAsDouble() > parsed);
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }

        // BOTTOM ROW
        nextPageSlotId = -1;
        prevPageSlotId = -1;
        resetSlotId = -1;
        resetFiltersButton.active = false;

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
            } else if (stack.isOf(Items.OAK_SIGN) && stackName.contains("search")) {
                searchSlotId = i;
                List<Text> tooltip = stack.getTooltip(client.player, TooltipContext.BASIC);
                for (Text text : tooltip) {
                    String string = text.getString();
                    if (string.contains("Filtered:")) {
                        String[] split = string.split(":");
                        if (split.length < 2) {
                            search = "";
                        } else search = split[1].trim();
                        break;
                    }
                }
            } else if (stack.isOf(Items.ANVIL) && stackName.contains("reset")) {
                resetSlotId = i;
                resetFiltersButton.active = true;
            }
        }
        if (!pageParsed) {
            currentPage = 1;
            totalPages = 1;
        }
        // CATEGORIES
        for (int i = 0; i < handler.getRows(); i++) {
            Slot slot = handler.slots.get(i * 9);
            if (!slot.hasStack()) continue;
            ItemStack stack = slot.getStack();
            List<Text> tooltip = stack.getTooltip(client.player, TooltipContext.BASIC);
            if (tooltip.size() < 2) continue;
            if (!tooltip.get(1).getString().toLowerCase().contains("category")) continue;
            CategoryTabWidget categoryTabWidget = categoryTabWidgets.get(i);
            categoryTabWidget.setIcon(stack);
            categoryTabWidget.setSlotId(i*9);
            for (int j = tooltip.size() - 1; j >= 0; j--) {
                String lowerCase = tooltip.get(j).getString().toLowerCase();
                if (lowerCase.contains("currently")) {
                    categoryTabWidget.setToggled(true);
                    break;
                } else if (lowerCase.contains("click")) {
                    categoryTabWidget.setToggled(false);
                    break;
                } else categoryTabWidget.setToggled(false);
            }
        }
        return true;
    }

    private void parsePage(ItemStack stack) {
        List<Text> tooltip = stack.getTooltip(client.player, TooltipContext.BASIC);
        String str = tooltip.get(1).getString().trim();
        str = str.substring(1, str.length() - 1); // remove parentheses
        String[] parts = str.split("/"); // split the string
        try {
            currentPage = Integer.parseInt(parts[0].replace(",", "")); // parse current page
            totalPages = Integer.parseInt(parts[1].replace(",", "")); // parse total
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

    @Override
    protected void init() {
        super.init();
        x = (this.width - 176)/2;
        y = (this.height - 187)/2;
        sortWidget = new SortWidget(x + 25, y+81, this); addDrawableChild(sortWidget);
        auctionTypeWidget = new AuctionTypeWidget(x + 134, y + 77, this); addDrawableChild(auctionTypeWidget);
        rarityWidget = new RarityWidget(x + 73, y+80, this); addDrawableChild(rarityWidget);
        resetFiltersButton = new ScaledTextButtonWidget(x+10, y+77, 12, 12, Text.literal("↻"), this::onResetPressed);
        addDrawableChild(resetFiltersButton);
        resetFiltersButton.setTooltip(Tooltip.of(Text.literal("Reset Filters")));
        resetFiltersButton.setTooltipDelay(500);
        if (categoryTabWidgets.isEmpty())
            for (int i = 0; i < 6; i++) {
                CategoryTabWidget categoryTabWidget = new CategoryTabWidget(new ItemStack(Items.SPONGE), this);
                categoryTabWidgets.add(categoryTabWidget);
                addSelectableChild(categoryTabWidget); // This method only makes it clickable, does not add it to the drawables list
                // manually rendered in the render method to have it not render behind the durability bars
                categoryTabWidget.setPosition(x-30, y+3+i*28);
            }
        else
            for (int i = 0; i < categoryTabWidgets.size(); i++) {
                CategoryTabWidget categoryTabWidget = categoryTabWidgets.get(i);
                categoryTabWidget.setPosition(x-30, y+3+i*28);

            }
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
            if (highlight.getOrDefault(slot.id, false)) {
                context.drawBorder(x1, y1, 16, 16, new Color(0, 255, 0, 100).getRGB());
            }
            context.drawItem(slot.getStack(), x1, y1);
            context.drawItemInSlot(this.textRenderer, slot.getStack(), x1, y1);
        }
        // Player inv
        renderPlayerInventory(context, playerInventory, this.textRenderer, 8, 105, mouseX-x, mouseY-y);
        matrices.pop();
        // Inventory title
        context.drawText(this.textRenderer, playerInventory.getDisplayName(), 8, 187-94, 0x404040, false);
        // Slot highlight
        if (highlightSlotX != -1) HandledScreen.drawSlotHighlight(context, highlightSlotX, highlightSlotY, 0);

        // Search
        context.enableScissor(x+7, y+4, x+97, y+16);
        context.drawText(textRenderer, Text.literal(search).fillStyle(Style.EMPTY.withUnderline(onSearchField(mouseX, mouseY))), 9, 6, Colors.WHITE, true);
        context.disableScissor();

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
            context.drawGuiTexture(SCROLLER_TEXTURE, 156, (int) (18 + (float)(Math.min(currentPage, totalPages)-1)/(totalPages-1)*37), 12, 15);


        matrices.pop();

        for (CategoryTabWidget categoryTabWidget : categoryTabWidgets) {
            categoryTabWidget.render(context, mouseX, mouseY, delta);
        }

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

    private boolean onSearchField(int mouseX, int mouseY) {
        int localX = mouseX - x;
        int localY = mouseY - y;
        return localX > 6 && localX < 97 && localY > 3 && localY < 16;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (isWaitingForServer()) return false;
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
        if (hoveredSlot != null) {
            clickAndWaitForServer(hoveredSlot.id);
            return true;
        }
        if (onSearchField((int)mouseX, (int) mouseY) && searchSlotId != -1) {
            clickAndWaitForServer(searchSlotId);
            playClickSound();
            return true;
        }
        return false;
    }

    private void onResetPressed(ButtonWidget buttonWidget) {
        buttonWidget.setFocused(false); // Annoying.
        if (resetSlotId == -1) return;
        clickAndWaitForServer(resetSlotId);
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
