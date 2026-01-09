package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import org.jspecify.annotations.Nullable;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.ColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

/**
 * Component that consists of an icon, some text and a progress bar.
 * The progress bar either shows the fill percentage or custom text.
 * NOTICE: pcnt is 0-100, not 0-1!
 */
class ProgressComponent extends Component {
	private static final int BAR_WIDTH = 100;
	private static final int BAR_HEIGHT = txtRend.lineHeight + 3;
	private static final int ICO_OFFS = 4;
	private static final int COL_BG_BAR = 0xF0101010;

	private final ItemStack ico;
	private final net.minecraft.network.chat.Component desc, bar;
	private final float pcnt;
	private final int color;
	private final boolean colorIsBright;
	private final int barW;

	/**
	 * @see Components#progressComponent(ItemStack, net.minecraft.network.chat.Component, net.minecraft.network.chat.Component, float)
	 */
	ProgressComponent(@Nullable ItemStack ico, net.minecraft.network.chat.@Nullable Component description, net.minecraft.network.chat.@Nullable Component bar, float percent, int color) {
		boolean showIcons = SkyblockerConfigManager.get().uiAndVisuals.tabHud.displayIcons;
		if (description == null || bar == null) {
			this.ico = showIcons ? Ico.BARRIER : null;
			this.desc = net.minecraft.network.chat.Component.literal("No data").withStyle(ChatFormatting.GRAY);
			this.bar = net.minecraft.network.chat.Component.literal("---").withStyle(ChatFormatting.GRAY);
			this.pcnt = 100f;
			this.color = 0xFF000000 | ChatFormatting.DARK_GRAY.getColor();
		} else {
			this.ico = showIcons ? (ico == null ? Ico.BARRIER : ico) : null;
			this.desc = description;
			this.bar = bar;
			this.pcnt = Math.clamp(percent, 0f, 100f);
			this.color = 0xFF000000 | color;
		}

		this.barW = BAR_WIDTH;
		this.width = (showIcons ? ICO_DIM.get() : 0) + PAD_L + Math.max(this.barW, txtRend.width(this.desc));
		this.height = txtRend.lineHeight + PAD_S + 2 + txtRend.lineHeight + 2;
		this.colorIsBright = ColorUtils.isBright(this.color);
	}

	/**
	 * @see Components#progressComponent(ItemStack, net.minecraft.network.chat.Component, net.minecraft.network.chat.Component, float)
	 */
	ProgressComponent(@Nullable ItemStack ico, net.minecraft.network.chat.@Nullable Component description, net.minecraft.network.chat.@Nullable Component bar, float percent) {
		this(ico, description, bar, percent, ColorUtils.percentToColor(percent));
	}

	/**
	 * @see Components#progressComponent(ItemStack, net.minecraft.network.chat.Component, float)
	 */
	ProgressComponent(@Nullable ItemStack ico, net.minecraft.network.chat.@Nullable Component description, float percent, int color) {
		// make sure percentages always have two decimals
		this(ico, description, net.minecraft.network.chat.Component.nullToEmpty(String.format("%.2f%%", percent)), percent, color);
	}

	/**
	 * @see Components#progressComponent(ItemStack, net.minecraft.network.chat.Component, float)
	 */
	ProgressComponent(@Nullable ItemStack ico, net.minecraft.network.chat.@Nullable Component description, float percent) {
		this(ico, description, percent, ColorUtils.percentToColor(percent));
	}

	ProgressComponent() {
		this(null, null, null, 100, 0);
	}

	@Override
	public void render(GuiGraphics context, int x, int y) {
		int componentX = x + PAD_L;
		if (ico != null) {
			renderIcon(context, ico, x, y + ICO_OFFS);
			componentX += ICO_DIM.get();
		}
		context.drawString(txtRend, desc, componentX, y, CommonColors.WHITE, false);

		int barY = y + txtRend.lineHeight + PAD_S;
		int endOffsX = ((int) (this.barW * (this.pcnt / 100f)));
		context.fill(componentX + endOffsX, barY, componentX + this.barW, barY + BAR_HEIGHT, COL_BG_BAR);
		context.fill(componentX, barY, componentX + endOffsX, barY + BAR_HEIGHT, this.color);

		int textWidth = txtRend.width(bar);
		// Only turn text dark when it is wider than the filled bar and the filled bar is bright.
		// The + 4 is because the text is indented 3 pixels and 1 extra pixel to the right as buffer.
		boolean textDark = endOffsX >= textWidth + 4 && this.colorIsBright;
		context.drawString(txtRend, bar, componentX + 3, barY + 2, textDark ? CommonColors.BLACK : CommonColors.WHITE, !textDark);
	}
}
