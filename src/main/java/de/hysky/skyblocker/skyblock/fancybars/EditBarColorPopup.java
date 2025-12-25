package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

// TODO use the new color things after collapse buttons is merged
public class EditBarColorPopup extends AbstractPopupScreen {

	private final Consumer<Color> setColor;

	private LinearLayout layout = LinearLayout.vertical();
	private BasicColorSelector colorSelector;

	protected EditBarColorPopup(Component title, Screen backgroundScreen, Consumer<Color> setColor) {
		super(title, backgroundScreen);
		this.setColor = setColor;
	}

	@Override
	protected void init() {
		super.init();
		layout = LinearLayout.vertical();
		layout.spacing(8).defaultCellSetting().alignHorizontallyCenter();
		layout.addChild(new StringWidget(title.copy().withStyle(Style.EMPTY.withBold(true)), Minecraft.getInstance().font));
		colorSelector = new BasicColorSelector(0, 0, 150, () -> done(null));
		layout.addChild(colorSelector);

		LinearLayout horizontal = LinearLayout.horizontal();
		Button buttonWidget = Button.builder(Component.literal("Cancel"), button -> onClose()).width(80).build();
		horizontal.addChild(buttonWidget);
		horizontal.addChild(Button.builder(Component.literal("Done"), this::done).width(80).build());

		layout.addChild(horizontal);
		layout.visitWidgets(this::addRenderableWidget);
		this.layout.arrangeElements();
		FrameLayout.centerInRectangle(layout, this.getRectangle());
	}

	private void done(Object object) {
		if (colorSelector.validColor) setColor.accept(new Color(colorSelector.getColor()));
		onClose();
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
	}

	private static class BasicColorSelector extends AbstractContainerWidget {

		private final EnterConfirmTextFieldWidget textFieldWidget;

		private BasicColorSelector(int x, int y, int width, Runnable onEnter) {
			super(x, y, width, 15, Component.literal("edit color"));
			textFieldWidget = new EnterConfirmTextFieldWidget(Minecraft.getInstance().font, getX() + 16, getY(), width - 16, 15, Component.empty(), onEnter);
			textFieldWidget.setResponder(this::onTextChange);
			textFieldWidget.setFilter(s -> s.length() <= 6);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of(textFieldWidget);
		}

		private int getColor() {
			return color;
		}

		private int color = 0xFF000000;
		private boolean validColor = false;

		private void onTextChange(String text) {
			try {
				color = Integer.parseInt(text, 16) | 0xFF000000;
				validColor = true;
			} catch (NumberFormatException e) {
				color = 0;
				validColor = false;
			}
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			HudHelper.drawBorder(context, getX(), getY(), 15, 15, validColor ? -1 : 0xFFDD0000);
			context.fill(getX() + 1, getY() + 1, getX() + 14, getY() + 14, color);
			textFieldWidget.renderWidget(context, mouseX, mouseY, delta);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {

		}

		@Override
		public void setX(int x) {
			super.setX(x);
			textFieldWidget.setX(getX() + 16);
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			textFieldWidget.setY(getY());
		}

		@Override
		protected int contentHeight() {
			return 0;
		}

		@Override
		protected double scrollRate() {
			return 0;
		}
	}
}
