package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class SideTabButtonWidget extends ToggleButtonWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/tab"), Identifier.ofVanilla("recipe_book/tab_selected"));
    protected @NotNull ItemStack icon;

    public void setIcon(@NotNull ItemStack icon) {
        this.icon = icon.copy();
    }

    public SideTabButtonWidget(int x, int y, boolean toggled, @NotNull ItemStack icon) {
        super(x, y, 35, 27, toggled);
        this.icon = icon.copy();
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
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        if (!isToggled()) this.setToggled(true);
    }
}
