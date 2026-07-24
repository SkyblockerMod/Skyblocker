package de.hysky.skyblocker.skyblock.tabhud.widget.element;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

public class TextureTextElement extends Element {
	private final Identifier texture;
	private final Component text;
	private final int textureWidth;
	private final int textureHeight;

	public TextureTextElement(Component text, Identifier texture, int textureWidth, int textureHeight) {
		this.text = text;
		this.texture = texture;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.width = textureWidth + PAD_L + txtRend.width(this.text);
		this.height = Math.max(textureHeight, txtRend.lineHeight);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int x, int y) {
		int offset = SkyblockerConfigManager.get().uiAndVisuals.tabHud.compactWidgets ? 2 : 4;
		context.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
		context.text(txtRend, text, x + textureWidth + PAD_L, y + offset, CommonColors.WHITE, false);
	}
}
