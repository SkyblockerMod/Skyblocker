package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Component that consists of an icon and two lines of text
 */
class IcoFatTextComponent extends Component {
	private static final int ICO_OFFS = 4;
	private ItemStack ico;
	private net.minecraft.network.chat.Component line1, line2;

	IcoFatTextComponent(@Nullable ItemStack ico, net.minecraft.network.chat.@Nullable Component l1, net.minecraft.network.chat.@Nullable Component l2) {
		this.ico = (ico == null) ? Ico.BARRIER : ico;

		if (l1 == null || l2 == null) {
			this.ico = Ico.BARRIER;
			this.line1 = net.minecraft.network.chat.Component.literal("No data").withStyle(ChatFormatting.GRAY);
			this.line2 = net.minecraft.network.chat.Component.literal("No data").withStyle(ChatFormatting.GRAY);
		} else {
			this.line1 = l1;
			this.line2 = l2;
		}

		this.width = ICO_DIM.get() + PAD_L + Math.max(txtRend.width(this.line1), txtRend.width(this.line2));
		this.height = txtRend.lineHeight + PAD_S + txtRend.lineHeight;
	}

	@Override
	public void render(GuiGraphics context, int x, int y) {
		int textX = x + ICO_DIM.get() + PAD_L;
		renderIcon(context, ico, x, y + ICO_OFFS);
		context.drawString(txtRend, line1, textX, y, CommonColors.WHITE, false);
		context.drawString(txtRend, line2, textX, y + txtRend.lineHeight + PAD_S, CommonColors.WHITE, false);
	}
}
