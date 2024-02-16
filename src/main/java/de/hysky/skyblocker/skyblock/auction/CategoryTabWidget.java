package de.hysky.skyblocker.skyblock.auction;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class CategoryTabWidget extends ToggleButtonWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(new Identifier("recipe_book/tab"), new Identifier("recipe_book/tab_selected"));

    public void setIcon(@NotNull ItemStack icon) {
        this.icon = icon.copy();
    }

    private @NotNull ItemStack icon;
    private final AuctionsBrowserScreen parent;
    private int slotId = -1;

    public CategoryTabWidget(@NotNull ItemStack icon, AuctionsBrowserScreen parent) {
        super(0, 0, 35, 27, false);
        this.icon = icon.copy(); // copy prevents item disappearing on click
        this.parent = parent;
        setTextures(TEXTURES);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (textures == null) return;
        Identifier identifier = textures.get(true, this.toggled);
        int x = getX();
        if (toggled) x-=2;
        //RenderSystem.disableDepthTest();
        context.drawGuiTexture(identifier, x, this.getY(), this.width, this.height);
        //RenderSystem.enableDepthTest();
        context.drawItem(icon, x+9, getY()+5);

        if (isMouseOver(mouseX, mouseY)) {
            context.getMatrices().push();
            //context.getMatrices().translate(0, 0, 500f);
            context.drawTooltip(parent.getTextRender(), icon.getTooltip(MinecraftClient.getInstance().player, TooltipContext.BASIC), mouseX, mouseY);
            context.getMatrices().pop();
        }

    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.toggled || parent.isWaitingForServer() || slotId == -1) return;
        parent.clickAndWaitForServer(slotId);
        this.setToggled(true);
    }
}
