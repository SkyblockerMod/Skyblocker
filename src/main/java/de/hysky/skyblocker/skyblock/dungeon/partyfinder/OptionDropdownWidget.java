package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.List;

public class OptionDropdownWidget extends EntryListWidget<OptionDropdownWidget.AbstractEntry> {
	private static final int CLOSED_HEIGHT = 35;

	private final int slotId;
	private final Text name;
	private final int maxHeight;
	private final PartyFinderScreen screen;
	private final Header header = new Header();

	private int backButtonId = -1;
	private @Nullable Option selectedOption;
	private boolean isOpen = false;

	private float animationProgress = 0f;

	public OptionDropdownWidget(PartyFinderScreen screen, Text name, int x, int y, int width, int maxHeight, int slotId) {
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
	protected int getScrollbarX() {
		return getRowLeft() + getRowWidth();
	}

	@Override
	public int getRowWidth() {
		return getWidth() - 6;
	}

	public void setSelectedOption(@NotNull OptionDropdownWidget.Option entry) {
		selectedOption = entry;
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
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
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if (isOpen) {
			if (animationProgress < 1) animationProgress += delta * 0.5f;
			else if (animationProgress != 1) animationProgress = 1;
		} else {
			animationProgress = 0;
		}

		if (PartyFinderScreen.DEBUG) {
			context.drawText(MinecraftClient.getInstance().textRenderer, String.valueOf(slotId), getX(), getY() - 10, Colors.RED, true);
			context.drawText(MinecraftClient.getInstance().textRenderer, String.valueOf(backButtonId), getX() + 50, getY() - 10, Colors.RED, true);
			context.drawText(client.textRenderer, String.valueOf(animationProgress), getX() - 10, getY(), Colors.GREEN, true);
		}
		if (isOpen) {
			int listHeight = Math.min(getHeight(), getContentsHeightWithPadding() - header.getHeight() - 4);
			int openedListHeight = isOpen ? (int) (listHeight * animationProgress) : (int) (listHeight * (1 - animationProgress));
			context.fill(getX(), header.getY() + header.getHeight(), getX() + getWidth() - 1, header.getY() + openedListHeight + header.getHeight(), 0xFFF0F0F0);
			context.fill(getX() + 1, header.getY() + header.getHeight() + 1, getX() + getWidth() - 2, header.getY() + openedListHeight + header.getHeight() - 1, Colors.BLACK);
		}

		super.renderWidget(context, mouseX, mouseY, delta);
	}

	@Override
	protected void drawHeaderAndFooterSeparators(DrawContext context) {
	}

	@Override
	protected void drawMenuListBackground(DrawContext context) {
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
	protected boolean isEntrySelectionAllowed() {
		return false;
	}

	private class Header extends AbstractEntry {
		@Override
		public boolean mouseClicked(Click click, boolean doubled) {
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
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = this.getX();
			int y = this.getY();
			context.drawText(MinecraftClient.getInstance().textRenderer, name, x, y + 1, 0xFFD0D0D0, false);
			int offset = 10;
			context.fill(x - 2, y + offset, x - 3 + OptionDropdownWidget.this.getWidth(), y + 15 + offset, 0xFFF0F0F0);
			context.fill(x - 1, y + 1 + offset, x - 3 + OptionDropdownWidget.this.getWidth() - 1, y + 14 + offset, Colors.BLACK);
			if (selectedOption != null) {
				context.drawText(MinecraftClient.getInstance().textRenderer, selectedOption.message, x + 2, y + 3 + offset, Colors.WHITE, true);
			} else context.drawText(MinecraftClient.getInstance().textRenderer, "???", x + 2, y + 3 + offset, Colors.WHITE, true);
		}
	}

	public class Option extends AbstractEntry {

		private final String message;
		private final ItemStack icon;
		private final int optionSlotId;

		public Option(@NotNull String message, @Nullable ItemStack icon, int slotId) {
			this.message = message;
			this.icon = icon;
			this.optionSlotId = slotId;
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			Matrix3x2fStack matrices = context.getMatrices();
			matrices.pushMatrix();
			int iconY = this.getY() + 1;
			matrices.translate(this.getX(), iconY);
			matrices.scale(0.8f, 0.8f);
			matrices.translate(-this.getX(), -iconY);
			context.drawItem(icon, this.getX(), iconY);
			matrices.popMatrix();

			if (PartyFinderScreen.DEBUG) context.drawText(client.textRenderer, String.valueOf(optionSlotId), this.getX() + 8, this.getY(), Colors.RED, true);
			MutableText text = Text.literal(message).fillStyle(Style.EMPTY.withUnderline(hovered));
			if (client.textRenderer.getWidth(text) >= this.getWidth() - 14) {
				ClickableWidget.drawScrollableText(context, client.textRenderer, text, this.getX() + 14, getY() + 3, this.getX() + this.getWidth(), this.getY() + 3 + client.textRenderer.fontHeight, Colors.WHITE);
			} else {
				context.drawText(client.textRenderer, text, this.getX() + 14, this.getY() + 3, Colors.WHITE, false);
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
		public boolean mouseClicked(Click click, boolean doubled) {
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

	abstract static class AbstractEntry extends EntryListWidget.Entry<AbstractEntry> {}
}
