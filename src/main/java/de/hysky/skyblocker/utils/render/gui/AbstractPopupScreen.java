package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * A more bare-bones version of Vanilla's Popup Screen. Meant to be extended.
 */
public class AbstractPopupScreen extends Screen {
	private static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("popup/background");
	public final Screen backgroundScreen;

	protected AbstractPopupScreen(Component title, Screen backgroundScreen) {
		super(title);
		this.backgroundScreen = backgroundScreen;
	}

	@Override
	public void onClose() {
		assert this.minecraft != null;
		this.minecraft.setScreen(this.backgroundScreen);
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		this.backgroundScreen.renderBackground(context, -1, -1, delta);
		context.nextStratum();
		this.backgroundScreen.render(context, -1, -1, delta);
		context.nextStratum();
		this.renderTransparentBackground(context);
	}

	/**
	 * These are the inner positions and size of the popup, not outer
	 */
	public static void drawPopupBackground(GuiGraphics context, int x, int y, int width, int height) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x - 18, y - 18, width + 36, height + 36);
	}

	@Override
	protected void init() {
		super.init();
		repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.backgroundScreen.resize(this.minecraft, this.width, this.height);
	}

	@Override
	public void added() {
		super.added();
		this.backgroundScreen.clearFocus();
	}

	public static class EnterConfirmTextFieldWidget extends EditBox {

		private final Runnable onEnter;

		public EnterConfirmTextFieldWidget(Font textRenderer, int width, int height, Component text, Runnable onEnter) {
			this(textRenderer, 0, 0, width, height, text, onEnter);
		}

		public EnterConfirmTextFieldWidget(Font textRenderer, int x, int y, int width, int height, Component text, Runnable onEnter) {
			this(textRenderer, x, y, width, height, null, text, onEnter);
		}

		public EnterConfirmTextFieldWidget(Font textRenderer, int x, int y, int width, int height, @Nullable EditBox copyFrom, Component text, Runnable onEnter) {
			super(textRenderer, x, y, width, height, copyFrom, text);
			this.onEnter = onEnter;
		}


		@Override
		public boolean keyPressed(KeyEvent input) {
			if (!super.keyPressed(input)) {
				if (input.isConfirmation()) {
					onEnter.run();
					return true;
				}
			} else return true;
			return false;
		}
	}

}
