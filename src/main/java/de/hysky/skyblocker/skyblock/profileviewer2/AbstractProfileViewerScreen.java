package de.hysky.skyblocker.skyblock.profileviewer2;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public abstract sealed class AbstractProfileViewerScreen extends Screen permits ErrorProfileViewerScreen, LoadingProfileViewerScreen, ProfileViewerScreen {
	private static final Identifier BACKGROUND = SkyblockerMod.id("textures/gui/profile_viewer2/base_plate.png");
	private static final int BACKGROUND_WIDTH = 322;
	private static final int BACKGROUND_HEIGHT = 180;

	protected AbstractProfileViewerScreen(Component title) {
		super(title);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		super.render(graphics, mouseX, mouseY, a);
		int backgroundX = this.width / 2 - BACKGROUND_WIDTH / 2;
		int backgroundY = this.height / 2 - BACKGROUND_HEIGHT / 2 + 5;
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, backgroundX, backgroundY, 0f, 0f, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
	}
}
