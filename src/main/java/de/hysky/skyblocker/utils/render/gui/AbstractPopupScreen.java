package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

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
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		this.backgroundScreen.extractBackground(graphics, -1, -1, a);
		graphics.nextStratum();
		this.backgroundScreen.extractRenderState(graphics, -1, -1, a);
		graphics.nextStratum();
		this.extractTransparentBackground(graphics);
	}

	/**
	 * These are the inner positions and size of the popup, not outer
	 */
	public static void extractPopupBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x - 18, y - 18, width + 36, height + 36);
	}

	@Override
	protected void init() {
		super.init();
		repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.backgroundScreen.resize(this.width, this.height);
	}

	@Override
	public void added() {
		super.added();
		this.backgroundScreen.clearFocus();
	}

	public static class EnterConfirmTextFieldWidget extends FilteredEditBox {
		private final Runnable onEnter;

		public EnterConfirmTextFieldWidget(Font font, int width, int height, Component text, Runnable onEnter) {
			this(font, 0, 0, width, height, text, onEnter);
		}

		public EnterConfirmTextFieldWidget(Font font, int x, int y, int width, int height, Component text, Runnable onEnter) {
			this(font, x, y, width, height, null, text, onEnter);
		}

		public EnterConfirmTextFieldWidget(Font font, int x, int y, int width, int height, @Nullable EditBox copyFrom, Component text, Runnable onEnter) {
			super(font, x, y, width, height, copyFrom, text);
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
