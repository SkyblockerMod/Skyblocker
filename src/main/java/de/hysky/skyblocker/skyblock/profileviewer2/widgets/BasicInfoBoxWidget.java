package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import de.hysky.skyblocker.SkyblockerMod;

public sealed class BasicInfoBoxWidget extends AbstractWidget permits AccessoryPowerWidget, CroesusWidget, DailyRunsWidget, DungeonRunsWidget, DungeonSecretsWidget, FloorRunsWidget, SkillsInfoBoxWidget, TuningSlotWidget {
	private static final Identifier BACKGROUND = SkyblockerMod.id("profile_viewer2/basic_background");

	public BasicInfoBoxWidget(int width, int height) {
		super(0, 0, width, height, Component.empty());
		this.active = false;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {}

	@Override
	public boolean shouldTakeFocusAfterInteraction() {
		return false;
	}
}
