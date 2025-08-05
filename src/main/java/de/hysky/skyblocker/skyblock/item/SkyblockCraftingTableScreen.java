package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.SkyblockRecipeBookWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.time.Duration;

public class SkyblockCraftingTableScreen extends HandledScreen<SkyblockCraftingTableScreenHandler> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/crafting_table.png");
    protected static final ButtonTextures MORE_CRAFTS_TEXTURES = new ButtonTextures(
            Identifier.of(SkyblockerMod.NAMESPACE, "quick_craft/more_button"),
            Identifier.of(SkyblockerMod.NAMESPACE, "quick_craft/more_button_disabled"),
            Identifier.of(SkyblockerMod.NAMESPACE, "quick_craft/more_button_highlighted")
    );

    protected static final Identifier QUICK_CRAFT = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/sprites/quick_craft/quick_craft_overlay.png");
    private final SkyblockRecipeBookWidget recipeBook = new SkyblockRecipeBookWidget(handler);
    private boolean narrow;
    private TexturedButtonWidget moreCraftsButton;

    public SkyblockCraftingTableScreen(SkyblockCraftingTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.narrow = this.width < 379;
        this.recipeBook.initialize(this.width, this.height, this.client, this.narrow);
        this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
        this.addDrawableChild(new TexturedButtonWidget(this.x + 5, this.height / 2 - 49, 20, 18, RecipeBookWidget.BUTTON_TEXTURES, button -> {
            this.recipeBook.toggleOpen();
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            button.setPosition(this.x + 5, this.height / 2 - 49);
            if (moreCraftsButton != null) moreCraftsButton.setPosition(this.x + 152, this.y + 63);
        }));
        if (!handler.mirrorverse) {
            moreCraftsButton = new TexturedButtonWidget(this.x + 152, y + 63, 16, 16, MORE_CRAFTS_TEXTURES,
                    button -> this.onMouseClick(handler.slots.get(26), handler.slots.get(26).id, 0, SlotActionType.PICKUP));
            moreCraftsButton.setTooltipDelay(Duration.ofMillis(250L));
            moreCraftsButton.setTooltip(Tooltip.of(Text.literal("More Crafts")));
            this.addDrawableChild(moreCraftsButton);
        }
        assert (client != null ? client.player : null) != null;
        this.addSelectableChild(this.recipeBook);
        this.setInitialFocus(this.recipeBook);
        this.titleX = 29;
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        this.recipeBook.update();
        if (moreCraftsButton == null) return;
        ItemStack stack = handler.slots.get(26).getStack();
        moreCraftsButton.active = stack.isEmpty() || stack.isOf(Items.PLAYER_HEAD);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.recipeBook.isOpen() && this.narrow) {
            this.renderBackground(context, mouseX, mouseY, delta);
            this.recipeBook.render(context, mouseX, mouseY, delta);
        } else {
            super.render(context, mouseX, mouseY, delta);
            this.recipeBook.render(context, mouseX, mouseY, delta);
            this.recipeBook.drawGhostSlots(context, true);
        }
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        this.recipeBook.drawTooltip(context, mouseX, mouseY, null);
    }


    @Override
    protected void drawSlot(DrawContext context, Slot slot) {
        if (slot.id == 23 && slot.getStack().isOf(Items.BARRIER)) return;
        super.drawSlot(context, slot);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = this.x;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);
        //4 px of margin to allow some space for custom resource packs that have size differences on the crafting table/inventory textures
        if (!handler.mirrorverse) context.drawTexture(RenderPipelines.GUI_TEXTURED, QUICK_CRAFT, i + 143, j - 3, 0, 0, 37, 90, 37, 90);
    }

    @Override
    protected boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        return (!this.narrow || !this.recipeBook.isOpen()) && super.isPointWithinBounds(x, y, width, height, pointX, pointY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.recipeBook.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.recipeBook);
            return true;
        }
        if (this.narrow && this.recipeBook.isOpen()) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean bl = mouseX < (double) left || mouseY < (double) top || mouseX >= (double) (left + this.backgroundWidth) || mouseY >= (double) (top + this.backgroundHeight);
        return this.recipeBook.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.backgroundWidth, this.backgroundHeight, button) && bl;
    }

    @Override
    protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
        super.onMouseClick(slot, slotId, button, actionType);
        this.recipeBook.onMouseClick(slot);
    }
}
