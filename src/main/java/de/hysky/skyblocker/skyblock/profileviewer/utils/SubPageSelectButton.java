package de.hysky.skyblocker.skyblock.profileviewer.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;

public class SubPageSelectButton extends AbstractWidget {
	private final ProfileViewerPage page;
	private final int index;
	private boolean toggled;

	private static final WidgetSprites TEXTURES = new WidgetSprites(SkyblockerMod.id("textures/gui/profile_viewer/button_icon_toggled.png"), SkyblockerMod.id("textures/gui/profile_viewer/button_icon.png"), SkyblockerMod.id("textures/gui/profile_viewer/button_icon_toggled_highlighted.png"), SkyblockerMod.id("textures/gui/profile_viewer/button_icon_highlighted.png"));
	private final ItemStack ICON;

	public SubPageSelectButton(ProfileViewerPage page, int x, int y, int index, ItemStack item) {
		super(x, y, 22, 22, item.getHoverName());
		this.ICON = item;
		this.toggled = index == 0;
		this.index = index;
		this.page = page;
		visible = false;
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURES.get(toggled, (mouseX > getX() && mouseX < getX() + 20 && mouseY > getY() && mouseY < getY() + 20)), this.getX(), this.getY(), 0, 0, 20, 20, 20, 20);
		context.renderItem(ICON, this.getX() + 2, this.getY() + 2);
		if ((mouseX > getX() && mouseX < getX() + 20 && mouseY > getY() && mouseY < getY() + 20)) {
			ItemLore lore = ICON.get(DataComponents.LORE);
			if (lore != null) context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, lore.lines(), mouseX, mouseY + 10);
		}

		this.handleCursor(context);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.active && this.visible && (mouseX > getX() && mouseX < getX() + 20 && mouseY > getY() && mouseY < getY() + 20);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		page.onNavButtonClick(this);
	}

	public int getIndex() {
		return index;
	}
}
