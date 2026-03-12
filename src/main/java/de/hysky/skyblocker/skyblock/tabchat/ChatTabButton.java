package de.hysky.skyblocker.skyblock.tabchat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class ChatTabButton {
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final String chatMode;
	private final String command;
	private final String translationKey;

	public ChatTabButton(int x, int y, int width, int height, String translationKey, String chatMode, String command) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.translationKey = translationKey;
		this.chatMode = chatMode;
		this.command = command;
	}

	public boolean isActive() {
		return TabChat.activeChat.equals(chatMode);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		int alpha = isActive() ? 0xFF : 0x80;
		int bgColor = alpha << 24;
		graphics.fill(x, y, x + width, y + height, bgColor);

		int textColor = isActive() ? 0xFF55FF55 : 0xFFFFFFFF;
		graphics.drawCenteredString(
				Minecraft.getInstance().font,
				Component.translatable(translationKey),
				x + width / 2,
				y + (height - 8) / 2,
				textColor
		);
	}

	public void handleClick() {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
		if (minecraft.player != null && minecraft.player.connection != null) {
			TabChat.activeChat = chatMode;
			minecraft.player.connection.sendCommand(command);
		}
	}
}
