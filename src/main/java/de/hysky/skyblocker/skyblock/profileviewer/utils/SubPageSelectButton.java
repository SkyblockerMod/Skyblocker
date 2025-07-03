package de.hysky.skyblocker.skyblock.profileviewer.utils;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.*;

public class SubPageSelectButton extends ClickableWidget {
    private final ProfileViewerPage page;
    private final int index;
    private boolean toggled;

    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/button_icon_toggled.png"), Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/button_icon.png"), Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/button_icon_toggled_highlighted.png"), Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/button_icon_highlighted.png"));
    private final ItemStack ICON;

    public SubPageSelectButton(ProfileViewerPage page, int x, int y, int index, ItemStack item) {
        super(x, y, 22, 22, item.getName());
        this.ICON = item;
        this.toggled = index == 0;
        this.index = index;
        this.page = page;
        visible = false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(this.getX(), this.getY(), this.getX() + 20, this.getY() + 20, Color.BLACK.getRGB());
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURES.get(toggled, (mouseX > getX() && mouseX < getX() + 19 && mouseY > getY() && mouseY < getY() + 19)), this.getX() + 1, this.getY() + 1, 0, 0, 18, 18, 18, 18);
        context.drawItem(ICON, this.getX() + 2, this.getY() + 2);
        if ((mouseX > getX() + 1 && mouseX < getX() + 19 && mouseY > getY() + 1 && mouseY < getY() + 19)) {
            LoreComponent lore = ICON.get(DataComponentTypes.LORE);
            if (lore != null) context.drawTooltip(MinecraftClient.getInstance().textRenderer, lore.lines(), mouseX, mouseY + 10);
        }
    }

    @Override
	public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible &&(mouseX > getX() + 1 && mouseX < getX() + 19 && mouseY > getY() + 1 && mouseY < getY() + 19);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        page.onNavButtonClick(this);
    }

    public int getIndex() {
        return index;
    }
}
