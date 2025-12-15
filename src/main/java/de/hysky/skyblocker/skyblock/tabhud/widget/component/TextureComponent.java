package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

public class TextureComponent extends Component {
	private final Identifier texture;
	private final Text text;
	private final int textureWidth;
	private final int textureHeight;

	public TextureComponent(Text text, Identifier texture, int textureWidth, int textureHeight) {
		this.text = text;
		this.texture = texture;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.width = textureWidth + PAD_L + txtRend.getWidth(this.text);
		this.height = Math.max(textureHeight, txtRend.fontHeight);
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		int offset = SkyblockerConfigManager.get().uiAndVisuals.tabHud.compactWidgets ? 2 : 4;
		context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
		context.drawText(txtRend, text, x + textureWidth + PAD_L, y + offset, Colors.WHITE, false);
	}
}
