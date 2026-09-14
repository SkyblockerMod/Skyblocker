package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class SkillTreeNodeWidget extends AbstractWidget {
	// Same size as a standard slot
	private static final int SIZE = GuiRenderer.DEFAULT_ITEM_SIZE + 2;
	private final ItemStack icon;
	private final List<Component> tooltip;
	private final Identifier tooltipStyle;

	public SkillTreeNodeWidget(ItemStack icon, List<Component> tooltip, @Nullable Identifier tooltipStyle) {
		super(0, 0, SIZE, SIZE, Component.empty());
		this.icon = icon;
		this.tooltip = tooltip;
		this.tooltipStyle = tooltipStyle;

		this.active = false;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		int x = this.getX() + 1;
		int y = this.getY() + 1;

		graphics.item(this.icon, x, y);

		if (this.isHovered()) {
			graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY, this.tooltipStyle);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {}

	@Override
	public boolean shouldTakeFocusAfterInteraction() {
		return false;
	}
}
