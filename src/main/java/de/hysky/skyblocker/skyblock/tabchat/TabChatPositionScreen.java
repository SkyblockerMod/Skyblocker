package de.hysky.skyblocker.skyblock.tabchat;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class TabChatPositionScreen extends Screen {
	private int groupX;
	private int groupY;
	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;

	private static final int BTN_W = TabChat.BUTTON_WIDTH;
	private static final int BTN_H = TabChat.BUTTON_HEIGHT;
	private static final int BTN_GAP = TabChat.BUTTON_GAP;
	private static final String[] LABEL_KEYS = {"skyblocker.tabChat.guild", "skyblocker.tabChat.all", "skyblocker.tabChat.party"};

	public TabChatPositionScreen() {
		super(Component.translatable("skyblocker.tabChat.positionScreen.title"));
	}

	@Override
	protected void init() {
		super.init();
		groupX = TabChat.getButtonX(this.width);
		groupY = TabChat.getButtonY(this.height);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		graphics.fill(0, 0, this.width, this.height, 0x60000000);

		graphics.drawCenteredString(
				this.font,
				Component.translatable("skyblocker.tabChat.positionScreen.hint"),
				this.width / 2, 10, 0xFFFFFF
		);

		for (int i = 0; i < 3; i++) {
			int bx = groupX + i * (BTN_W + BTN_GAP);
			int by = groupY;

			boolean hovered = mouseX >= bx && mouseX <= bx + BTN_W && mouseY >= by && mouseY <= by + BTN_H;
			int bgColor = hovered ? 0xCC333333 : 0xCC000000;
			graphics.fill(bx, by, bx + BTN_W, by + BTN_H, bgColor);

			graphics.drawCenteredString(
					this.font,
					Component.translatable(LABEL_KEYS[i]),
					bx + BTN_W / 2,
					by + (BTN_H - 8) / 2,
					0xFFFFFF
			);
		}

		int totalWidth = 3 * BTN_W + 2 * BTN_GAP;
		int outlineColor = 0x80FFFFFF;
		graphics.hLine(groupX - 2, groupX + totalWidth + 1, groupY - 2, outlineColor);
		graphics.hLine(groupX - 2, groupX + totalWidth + 1, groupY + BTN_H + 1, outlineColor);
		graphics.vLine(groupX - 2, groupY - 2, groupY + BTN_H + 1, outlineColor);
		graphics.vLine(groupX + totalWidth + 1, groupY - 2, groupY + BTN_H + 1, outlineColor);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (click.button() == 0) {
			int totalWidth = 3 * (BTN_W + BTN_GAP) - BTN_GAP;
			double mx = click.x();
			double my = click.y();
			if (mx >= groupX && mx <= groupX + totalWidth && my >= groupY && my <= groupY + BTN_H) {
				dragging = true;
				dragOffsetX = (int) (mx - groupX);
				dragOffsetY = (int) (my - groupY);
				return true;
			}
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
		if (dragging && click.button() == 0) {
			int totalWidth = 3 * (BTN_W + BTN_GAP) - BTN_GAP;
			groupX = (int) (click.x() - dragOffsetX);
			groupY = (int) (click.y() - dragOffsetY);
			groupX = Math.max(0, Math.min(groupX, this.width - totalWidth));
			groupY = Math.max(0, Math.min(groupY, this.height - BTN_H));
			return true;
		}
		return super.mouseDragged(click, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent click) {
		if (dragging && click.button() == 0) {
			dragging = false;
			return true;
		}
		return super.mouseReleased(click);
	}

	@Override
	public void onClose() {
		SkyblockerConfigManager.update(config -> {
			config.uiAndVisuals.tabChat.buttonX = groupX;
			config.uiAndVisuals.tabChat.buttonY = groupY;
		});
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
