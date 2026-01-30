package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

// This is kinda excessive, but I thought it was a good idea
public class SliderWidget<E extends Enum<E> & SliderWidget.OptionInfo> extends AbstractWidget {
	private final SlotClickHandler clickSlot;
	private int button = 0;
	private int slotId = -1;

	protected E current;

	float posProgress;

	/**
	 * @param x             x position
	 * @param y             y position
	 * @param width         width
	 * @param height        height
	 * @param message       probably useless, just put the widget name
	 * @param clickSlot     the parent AuctionsBrowser
	 * @param defaultOption the default option <strong>should be the one at ordinal 0</strong>
	 */
	public SliderWidget(int x, int y, int width, int height, Component message, SlotClickHandler clickSlot, E defaultOption) {
		super(x, y, width, height, message);
		this.clickSlot = clickSlot;
		this.current = defaultOption;
		posProgress = current.getOffset();
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		if (posProgress < current.getOffset()) {
			posProgress += delta * 5;
			if (posProgress > current.getOffset()) posProgress = current.getOffset();
		} else if (posProgress > current.getOffset()) {
			posProgress -= delta * 5;
			if (posProgress < current.getOffset()) posProgress = current.getOffset();
		}


		context.pose().pushMatrix();
		context.pose().translate(getX(), getY());

		int x = current.isVertical() ? 0 : Math.round(posProgress);
		int y = current.isVertical() ? Math.round(posProgress) : 0;

		int optionWidth = current.getOptionSize()[0];
		int optionHeight = current.getOptionSize()[1];

		context.blitSprite(RenderPipelines.GUI_TEXTURED, current.getBackTexture(), 0, 0, getWidth(), getHeight());
		if (isHovered()) {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, current.getHoverTexture(), x, y, optionWidth, optionHeight);
		} else {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, current.getOptionTexture(), x, y, optionWidth, optionHeight);
		}
		context.pose().popMatrix();
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		if (slotId == -1) return;
		clickSlot.click(slotId, button);
		super.onClick(click, doubled);
	}

	@Override
	protected boolean isValidClickButton(MouseButtonInfo input) {
		this.button = input.button();
		return super.isValidClickButton(input) || button == 1;
	}

	public void setSlotId(int slotId) {
		this.slotId = slotId;
	}

	public void setCurrent(E current) {
		this.current = current;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
	}

	public interface OptionInfo {
		boolean isVertical();

		/**
		 * @return The current option's position offset from the first option's position
		 */
		int getOffset();

		int[] getOptionSize();

		Identifier getOptionTexture();

		Identifier getBackTexture();

		Identifier getHoverTexture();

	}
}
