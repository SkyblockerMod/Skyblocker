package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import com.mojang.authlib.GameProfile;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.ProfileViewerPlayer;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class PlayerWidget extends ProfileViewerWidget {
	private static final Identifier BACKGROUND = SkyblockerMod.id("profile_viewer2/player_background");
	public static final int WIDTH = 82;
	public static final int HEIGHT = 110;
	private static final int NAME_TAG_X_OFFSET = 1;
	private static final int NAME_TAG_Y_OFFSET = 2;
	private final ProfileViewerPlayer entity;

	public PlayerWidget(int x, int y, GameProfile playerProfile) {
		super(x, y, WIDTH, HEIGHT, Component.empty());
		this.entity = new ProfileViewerPlayer(playerProfile);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float a) {
		ActiveTextCollector textCollector = graphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE);

		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		textCollector.acceptScrollingWithDefaultCenter(this.entity.getName(), this.getX() + NAME_TAG_X_OFFSET, this.getRight() - NAME_TAG_X_OFFSET, this.getY() + NAME_TAG_Y_OFFSET, this.getY() + NAME_TAG_Y_OFFSET + getFont().lineHeight);
		InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, this.getX(), this.getY(), this.getRight(), this.getBottom(), 42, 0.0625f, mouseX, mouseY, this.entity);
	}
}
