package de.hysky.skyblocker.skyblock.tabhud.widget.element;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

/**
 * Element that consists of an icon and two lines of text
 */
class IcoFatTextElement extends Element {
	private static final int ICO_OFFS = 4;
	private FlexibleItemStack ico;
	private final Component line1;
	private final Component line2;

	IcoFatTextElement(@Nullable FlexibleItemStack ico, @Nullable Component l1, @Nullable Component l2) {
		this.ico = (ico == null) ? Ico.BARRIER : ico;

		if (l1 == null || l2 == null) {
			this.ico = Ico.BARRIER;
			this.line1 = Component.literal("No data").withStyle(ChatFormatting.GRAY);
			this.line2 = Component.literal("No data").withStyle(ChatFormatting.GRAY);
		} else {
			this.line1 = l1;
			this.line2 = l2;
		}

		this.width = ICO_DIM.get() + PAD_L + Math.max(txtRend.width(this.line1), txtRend.width(this.line2));
		this.height = txtRend.lineHeight + PAD_S + txtRend.lineHeight;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y) {
		int textX = x + ICO_DIM.get() + PAD_L;
		extractIcon(graphics, ico, x, y + ICO_OFFS);
		graphics.text(txtRend, line1, textX, y, CommonColors.WHITE, false);
		graphics.text(txtRend, line2, textX, y + txtRend.lineHeight + PAD_S, CommonColors.WHITE, false);
	}
}
