package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.item.TooltipType;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class CategoryTabWidget extends ToggleButtonWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(new Identifier("recipe_book/tab"), new Identifier("recipe_book/tab_selected"));

    public void setIcon(@NotNull ItemStack icon) {
        this.icon = icon.copy();
    }

    private @NotNull ItemStack icon;
    private final SlotClickHandler slotClick;
    private int slotId = -1;

    public CategoryTabWidget(@NotNull ItemStack icon, SlotClickHandler slotClick) {
        super(0, 0, 35, 27, false);
        this.icon = icon.copy(); // copy prevents item disappearing on click
        this.slotClick = slotClick;
        setTextures(TEXTURES);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (textures == null) return;
        Identifier identifier = textures.get(true, this.toggled);
        int x = getX();
        if (toggled) x -= 2;
        context.drawGuiTexture(identifier, x, this.getY(), this.width, this.height);
        context.drawItem(icon, x + 9, getY() + 5);

        if (isMouseOver(mouseX, mouseY)) {
            context.getMatrices().push();
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, icon.getTooltip(TooltipContext.DEFAULT, MinecraftClient.getInstance().player, TooltipType.BASIC), mouseX, mouseY);
            context.getMatrices().pop();
        }

    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.toggled || slotId == -1) return;
        slotClick.click(slotId);
        this.setToggled(true);
    }
}
