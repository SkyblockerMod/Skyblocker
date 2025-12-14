package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

/**
 * Component that consists of an icon and a line of text.
 */
class IcoTextComponent extends Component {
	private ItemStack ico;
	private net.minecraft.network.chat.Component text;

	IcoTextComponent(ItemStack ico, net.minecraft.network.chat.Component txt) {
		this.ico = (ico == null) ? Ico.BARRIER : ico;
		this.text = txt;

		if (txt == null) {
			this.ico = Ico.BARRIER;
			this.text = net.minecraft.network.chat.Component.literal("No data").withStyle(ChatFormatting.GRAY);
		}

		int iconDim = ICO_DIM.get();
		this.width = iconDim + PAD_L + txtRend.width(this.text);
		this.height = iconDim;
	}

	@Override
	public void render(GuiGraphics context, int x, int y) {
		int offset = SkyblockerConfigManager.get().uiAndVisuals.hud.compactWidgets ? 2 : 4;
		renderIcon(context, ico, x, y);
		context.drawString(txtRend, text, x + ICO_DIM.get() + PAD_L, y + offset, CommonColors.WHITE, false);
	}
}
