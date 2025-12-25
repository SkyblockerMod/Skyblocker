package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.CommonColors;

/**
 * Component that consists of 1 or 2 lines of text.
 */
public class PlainTextComponent extends Component {
	private final List<net.minecraft.network.chat.Component> lines = new ArrayList<>();

	public PlainTextComponent(net.minecraft.network.chat.@Nullable Component txt) {
		lines.add(txt == null ? net.minecraft.network.chat.Component.literal("No data").withStyle(ChatFormatting.GRAY) : txt);

		this.width = PAD_L + txtRend.width(lines.getFirst()); // looks off without padding
		this.height = txtRend.lineHeight;
	}

	public PlainTextComponent(net.minecraft.network.chat.@Nullable Component line1, net.minecraft.network.chat.@Nullable Component line2) {
		lines.add(line1 == null ? net.minecraft.network.chat.Component.literal("No data").withStyle(ChatFormatting.GRAY) : line1);
		lines.add(line2 == null ? net.minecraft.network.chat.Component.literal("No data").withStyle(ChatFormatting.GRAY) : line2);

		this.width = PAD_L + Math.max(txtRend.width(lines.get(0)), txtRend.width(lines.get(1)));
		this.height = (txtRend.lineHeight * 2) + PAD_S;
	}

	@Override
	public void render(GuiGraphics context, int x, int y) {
		int yOffset = 0;
		for (net.minecraft.network.chat.Component line : lines) {
			context.drawString(txtRend, line, x + PAD_L, y + yOffset, CommonColors.WHITE, false);
			yOffset += txtRend.lineHeight + PAD_S;
		}
	}
}
