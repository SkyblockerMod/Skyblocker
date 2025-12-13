package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphics.HoveredTextEffects;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

public class OptionDropdownWidget extends AbstractSelectionList<OptionDropdownWidget.AbstractEntry> {
	private static final int CLOSED_HEIGHT = 35;

	private final int slotId;
	private final Component name;
	private final int maxHeight;
	private final PartyFinderScreen screen;
	private final Header header = new Header();

	private int backButtonId = -1;
	private @Nullable Option selectedOption;
	private boolean isOpen = false;

	private float animationProgress = 0f;

	public OptionDropdownWidget(PartyFinderScreen screen, Component name, int x, int y, int width, int maxHeight, int slotId) {
		super(screen.getClient(), width, CLOSED_HEIGHT, y, 15);
		//super(screen.getClient(), width, CLOSED_HEIGHT, y, 15, 25);
		this.maxHeight = maxHeight;
		this.screen = screen;
		this.slotId = slotId;
		setX(x);
		this.name = name;
		addEntry(header);
	}

	@Override
	public int getRowLeft() {
		return getX() + 2;
	}

	@Override
	protected int scrollBarX() {
		return getRowLeft() + getRowWidth();
	}

	@Override
	public int getRowWidth() {
		return getWidth() - 6;
	}

	public void setSelectedOption(OptionDropdownWidget.Option entry) {
		selectedOption = entry;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (!screen.getSettingsContainer().canInteract(this)) return false;
		if (isOpen) {
			if (!isMouseOver(click.x(), click.y()) && backButtonId != -1) {
				screen.clickAndWaitForServer(backButtonId);
				return true;
			}
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	@Override
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		if (isOpen) {
			if (animationProgress < 1) animationProgress += delta * 0.5f;
			else if (animationProgress != 1) animationProgress = 1;
		} else {
			animationProgress = 0;
		}

		if (PartyFinderScreen.DEBUG) {
			context.drawString(Minecraft.getInstance().font, String.valueOf(slotId), getX(), getY() - 10, CommonColors.RED, true);
			context.drawString(Minecraft.getInstance().font, String.valueOf(backButtonId), getX() + 50, getY() - 10, CommonColors.RED, true);
			context.drawString(minecraft.font, String.valueOf(animationProgress), getX() - 10, getY(), CommonColors.GREEN, true);
		}
		if (isOpen) {
			int listHeight = Math.min(getHeight(), contentHeight() - header.getHeight() - 4);
			int openedListHeight = isOpen ? (int) (listHeight * animationProgress) : (int) (listHeight * (1 - animationProgress));
			context.fill(getX(), header.getY() + header.getHeight(), getX() + getWidth() - 1, header.getY() + openedListHeight + header.getHeight(), 0xFFF0F0F0);
			context.fill(getX() + 1, header.getY() + header.getHeight() + 1, getX() + getWidth() - 2, header.getY() + openedListHeight + header.getHeight() - 1, CommonColors.BLACK);
		}

		super.renderWidget(context, mouseX, mouseY, delta);
	}

	@Override
	protected void renderListSeparators(GuiGraphics context) {
	}

	@Override
	protected void renderListBackground(GuiGraphics context) {
	}

	public void open(List<Option> entries, int backButtonId) {
		if (isOpen) return;
		isOpen = true;
		height = maxHeight;
		animationProgress = 0f;
		clearEntriesExcept(header);
		entries.forEach(this::addEntry);
		this.backButtonId = backButtonId;
	}

	public void close() {
		if (!isOpen) return;
		isOpen = false;
		height = CLOSED_HEIGHT;
		this.clearEntriesExcept(header);
	}

	@Override
	protected boolean entriesCanBeSelected() {
		return false;
	}

	private class Header extends AbstractEntry {
		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			if (screen.isWaitingForServer()) return false;
			if (isOpen) {
				if (backButtonId != -1) screen.clickAndWaitForServer(backButtonId);
			} else {
				animationProgress = 0f;
				screen.clickAndWaitForServer(slotId);
				screen.partyFinderButton.active = false;
			}
			return true;
		}

		@Override
		public int getHeight() {
			return 25;
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = this.getX();
			int y = this.getY();
			context.drawString(Minecraft.getInstance().font, name, x, y + 1, 0xFFD0D0D0, false);
			int offset = 10;
			context.fill(x - 2, y + offset, x - 3 + OptionDropdownWidget.this.getWidth(), y + 15 + offset, 0xFFF0F0F0);
			context.fill(x - 1, y + 1 + offset, x - 3 + OptionDropdownWidget.this.getWidth() - 1, y + 14 + offset, CommonColors.BLACK);
			if (selectedOption != null) {
				context.drawString(Minecraft.getInstance().font, selectedOption.message, x + 2, y + 3 + offset, CommonColors.WHITE, true);
			} else context.drawString(Minecraft.getInstance().font, "???", x + 2, y + 3 + offset, CommonColors.WHITE, true);
		}
	}

	public class Option extends AbstractEntry {

		private final String message;
		private final ItemStack icon;
		private final int optionSlotId;

		public Option(String message, @Nullable ItemStack icon, int slotId) {
			this.message = message;
			this.icon = icon;
			this.optionSlotId = slotId;
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			Matrix3x2fStack matrices = context.pose();
			matrices.pushMatrix();
			int iconY = this.getY() + 1;
			matrices.translate(this.getX(), iconY);
			matrices.scale(0.8f, 0.8f);
			matrices.translate(-this.getX(), -iconY);
			context.renderItem(icon, this.getX(), iconY);
			matrices.popMatrix();

			if (PartyFinderScreen.DEBUG) context.drawString(minecraft.font, String.valueOf(optionSlotId), this.getX() + 8, this.getY(), CommonColors.RED, true);
			MutableComponent text = Component.literal(message).withStyle(Style.EMPTY.withUnderlined(hovered));
			if (minecraft.font.width(text) >= this.getWidth() - 14) {
				context.textRenderer(HoveredTextEffects.NONE).acceptScrollingWithDefaultCenter(text, this.getX() + 14, this.getX() + this.getWidth(), getY() + 3, this.getY() + 3 + minecraft.font.lineHeight);
			} else {
				context.drawString(minecraft.font, text, this.getX() + 14, this.getY() + 3, CommonColors.WHITE, false);
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Option that = (Option) o;

			return message.equals(that.message);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			if (screen.isWaitingForServer()) return false;
			if (click.button() == 0) {
				screen.clickAndWaitForServer(this.optionSlotId);
				setSelectedOption(this);
			}
			return true;
		}

		@Override
		public int hashCode() {
			return message.hashCode();
		}
	}

	abstract static class AbstractEntry extends AbstractSelectionList.Entry<AbstractEntry> {}
}
