package de.hysky.skyblocker.skyblock.tabhud.widget.element;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

/**
 * Element that consists of an icon and a line of text.
 */
class IcoTextElement extends Element {
	private FlexibleItemStack ico;
	private final Component text;

	IcoTextElement(@Nullable FlexibleItemStack ico, @Nullable Component txt) {
		this.ico = (ico == null) ? Ico.BARRIER : ico;

		if (txt != null) {
			this.text = txt;
		} else {
			this.ico = Ico.BARRIER;
			this.text = Component.literal("No data").withStyle(ChatFormatting.GRAY);
		}

		int iconDim = ICO_DIM.get();
		this.width = iconDim + PAD_L + txtRend.width(this.text);
		this.height = iconDim;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y) {
		int offset = SkyblockerConfigManager.get().uiAndVisuals.tabHud.compactWidgets ? 2 : 4;
		extractIcon(graphics, ico, x, y);
		graphics.text(txtRend, text, x + ICO_DIM.get() + PAD_L, y + offset, CommonColors.WHITE, false);
	}
}
