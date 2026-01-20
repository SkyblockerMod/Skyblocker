package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class RangedValueWidget extends AbstractContainerWidget {
	private final PartyFinderScreen screen;

	private final int slotId;
	private final Component name;
	private int minSlotId = -1;
	private int maxSlotId = -1;
	private int backSlotId = -1;

	private int min = -1;
	private int max = -1;

	private State state = State.CLOSED;

	private final ModifiedTextFieldWidget input;
	private final Button okButton;

	public RangedValueWidget(PartyFinderScreen screen, Component name, int x, int y, int width, int slotId) {
		super(x, y, width, 45, Component.empty());
		this.slotId = slotId;
		this.screen = screen;
		this.name = name;

		this.input = new ModifiedTextFieldWidget(Minecraft.getInstance().font, x, y + 25, width - 15, 15, Component.empty());
		this.input.setVisible(false);
		this.input.setMaxLength(3);
		input.setResponder(this::updateConfirmButton);
		this.okButton = Button.builder(Component.literal("✔"), (a) -> sendPacket())
				.bounds(x + width - 15, y + 25, 15, 15)
				.build();
		this.okButton.visible = false;
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of(this.input, this.okButton);
	}

	public void updateConfirmButton(String string) {
		try {
			int i = Integer.parseInt(string.trim());
			if (i < 0 || i > 999) { // Too beeg or too smol
				this.okButton.active = false;
				this.input.setGood(false);
			} else if (state == State.MODIFYING_MIN && i > max) { // If editing min and bigger than max
				this.okButton.active = false;
				this.input.setGood(false);
			} else { // If editing max and smaller than min
				boolean active1 = state != State.MODIFYING_MAX || i >= min;
				this.okButton.active = active1;
				this.input.setGood(active1);
			}
		} catch (NumberFormatException e) {
			this.okButton.active = false;
			this.input.setGood(false);
		}
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		context.drawString(Minecraft.getInstance().font, name, getX(), getY(), 0xFFD0D0D0, false);
		int textOffset = 10;
		if (!visible) return;
		final Font textRenderer = screen.getClient().font;
		if (PartyFinderScreen.DEBUG) {
			context.drawString(textRenderer, String.valueOf(slotId), getX(), getY() - 10, 0xFFFF0000, true);
			context.drawString(textRenderer, String.valueOf(minSlotId), getX() + 20, getY() - 10, 0xFFFF0000, true);
			context.drawString(textRenderer, String.valueOf(maxSlotId), getX() + 40, getY() - 10, 0xFFFF0000, true);
			context.drawString(textRenderer, String.valueOf(backSlotId), getX() + 60, getY() - 10, 0xFFFF0000, true);
		}
		this.input.render(context, mouseX, mouseY, delta);
		this.okButton.render(context, mouseX, mouseY, delta);
		if (Objects.requireNonNull(this.state) == State.CLOSED) {
			context.fill(getX(), getY() + textOffset, getX() + width, getY() + 15 + textOffset, 0xFFFFFFFF);
			context.fill(getX() + 1, getY() + 1 + textOffset, getX() + width - 1, getY() + 14 + textOffset, 0xFF000000);
			context.drawString(textRenderer, min + " - " + max, getX() + 3, getY() + 3 + textOffset, 0xFFFFFFFF, false);
		} else {
			context.fill(getX(), getY() + textOffset, getX() + width, getY() + 15 + textOffset, 0xFFFFFFFF);
			context.fill(getX() + 1, getY() + 1 + textOffset, getX() + width - 1, getY() + 14 + textOffset, 0xFF000000);
			context.drawCenteredString(textRenderer, "-", getX() + (width >> 1), getY() + 3 + textOffset, 0xFFFFFFFF);
			int selectedColor = 0xFFFFFF00;
			int unselectedColor = 0xFFD0D0D0;

			boolean mouseOverMin = mouseOverMinButton(mouseX, mouseY);
			boolean mouseOverMax = mouseOverMaxButton(mouseX, mouseY);

			// Minimum
			int minStartX = getX() + 1;
			int minEndX = getX() + (width >> 1) - 6;
			context.fill(minStartX, getY() + 1 + textOffset, minEndX, getY() + 14 + textOffset, state == State.MODIFYING_MIN ? selectedColor : (mouseOverMin ? 0xFFFFFFFF : unselectedColor));
			context.fill(minStartX + 1, getY() + 2 + textOffset, minEndX - 1, getY() + 13 + textOffset, 0xFF000000);

			context.drawCenteredString(textRenderer, String.valueOf(min), (minStartX + minEndX) >> 1, getY() + 3 + textOffset, 0xFFFFFFFF);

			// Maximum
			int maxStartX = getX() + (width >> 1) + 5;
			int maxEndX = getX() + width - 1;
			context.fill(maxStartX, getY() + 1 + textOffset, maxEndX, getY() + 14 + textOffset, state == State.MODIFYING_MAX ? selectedColor : (mouseOverMax ? 0xFFFFFFFF : unselectedColor));
			context.fill(maxStartX + 1, getY() + 2 + textOffset, maxEndX - 1, getY() + 13 + textOffset, 0xFF000000);

			context.drawCenteredString(textRenderer, String.valueOf(max), (maxStartX + maxEndX) >> 1, getY() + 3 + textOffset, 0xFFFFFFFF);
		}
	}

	private boolean mouseOverMinButton(int mouseX, int mouseY) {
		return isMouseOver(mouseX, mouseY) && mouseX < getX() + (width >> 1) - 5 && mouseY < getY() + 25 && mouseY > getY() + 10;
	}

	private boolean mouseOverMaxButton(int mouseX, int mouseY) {
		return isMouseOver(mouseX, mouseY) && mouseX > getX() + (width >> 1) + 5 && mouseY < getY() + 25 && mouseY > getY() + 10;
	}

	public void setState(State state) {
		this.state = state;
		switch (state) {
			case CLOSED, OPEN -> {
				this.input.setVisible(false);
				this.input.setFocused(false);
				this.okButton.visible = false;
				this.okButton.setFocused(false);
			}
			case MODIFYING_MIN, MODIFYING_MAX -> {
				this.input.setVisible(true);
				this.input.moveCursorTo(0, false);
				this.input.setValue(String.valueOf(state == State.MODIFYING_MIN ? min : max));
				this.input.setFocused(true);
				this.okButton.visible = true;
			}
		}
	}

	public void setStateAndSlots(State state, int minSlotId, int maxSlotId, int backSlotId) {
		setState(state);
		this.minSlotId = minSlotId;
		this.maxSlotId = maxSlotId;
		this.backSlotId = backSlotId;
	}

	public void setMinAndMax(int min, int max) {
		this.min = min;
		this.max = max;
	}

	private void sendPacket() {
		SignBlockEntity sign = screen.getSign();
		String inputTrimmed = input.getValue().trim();
		if (state == State.MODIFYING_MIN) {
			try { min = Integer.parseInt(inputTrimmed); } catch (NumberFormatException ignored) {}
		} else if (state == State.MODIFYING_MAX) {
			try { max = Integer.parseInt(inputTrimmed); } catch (NumberFormatException ignored) {}
		}
		if (sign != null) {
			Component[] messages = sign.getText(screen.isSignFront()).getMessages(screen.getClient().isTextFilteringEnabled());
			screen.getClient().player.connection.send(new ServerboundSignUpdatePacket(sign.getBlockPos(), screen.isSignFront(),
					inputTrimmed,
					messages[1].getString(),
					messages[2].getString(),
					messages[3].getString()
			));
		}
		screen.closedSign();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (screen.isWaitingForServer() || !screen.getSettingsContainer().canInteract(this)) return false;
		if (!visible) return false;
		if (!isMouseOver(click.x(), click.y())) {
			if (state == State.OPEN && backSlotId != -1) {
				screen.clickAndWaitForServer(backSlotId);
				return true;
			} else if (state == State.MODIFYING_MIN || state == State.MODIFYING_MAX) {
				// revert back to previous value if this value is not valid
				if (!input.isGood) input.setValue(String.valueOf(state == State.MODIFYING_MIN ? min : max));
				sendPacket();
				return true;
			} else return false;
		}
		switch (state) {
			case CLOSED -> {
				if (click.y() > getY() + 25) return false;
				screen.clickAndWaitForServer(slotId);
				return true;
			}
			case OPEN -> {

				if (mouseOverMinButton((int) click.x(), (int) click.y())) {
					if (minSlotId == -1) return false;
					screen.clickAndWaitForServer(minSlotId);
				} else if (mouseOverMaxButton((int) click.x(), (int) click.y())) {
					if (maxSlotId == -1) return false;
					screen.clickAndWaitForServer(maxSlotId);
				} else return !(click.y() > getY() + 25);
				return true;
			}
			default -> {
				return super.mouseClicked(click, doubled);
			}
		}
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		this.input.setX(getX());
		this.okButton.setX(getX() + getWidth() - 15);
	}

	@Override
	public void setY(int y) {
		super.setY(y);
		this.input.setY(getY() + 25);
		this.okButton.setY(getY() + 25);
	}

	public enum State {
		CLOSED,
		OPEN,
		MODIFYING_MIN,
		MODIFYING_MAX
	}

	protected class ModifiedTextFieldWidget extends EditBox {
		private boolean isGood = false;

		public ModifiedTextFieldWidget(Font textRenderer, int x, int y, int width, int height, Component text) {
			super(textRenderer, x, y, width, height, text);
		}

		@Override
		public boolean keyPressed(KeyEvent input) {
			if (!this.isFocused()) return false;
			if (input.isConfirmation() && isGood) {
				sendPacket();
				return true;
			}
			return super.keyPressed(input);
		}

		public void setGood(boolean good) {
			isGood = good;
		}

	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	@Override
	protected int contentHeight() {
		return 0;
	}

	@Override
	protected double scrollRate() {
		return 0;
	}
}
