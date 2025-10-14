package de.hysky.skyblocker.skyblock.quicknav;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A special type of {@link QuickNavButton} that requires double-clicking to open.
 */
public class QuickNavConfirmationButton extends QuickNavButton {
	private static final Tooltip CONFIRM_TOOLTIP = Tooltip.of(Text.translatable("skyblocker.quickNav.confirm"));
	private static final long DOUBLE_CLICK_TIME = 1000;
	private long lastClicked = 0;
	private boolean showingConfirmTooltip;
	private static final Identifier DOUBLE_CLICK_ICON = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/quick_nav_double_click.png");

	protected final int index;

	/**
	 * See {@link QuickNavButton#QuickNavButton(int, boolean, String, ItemStack, String)}
	 */
	public QuickNavConfirmationButton(int index, boolean toggled, String command, ItemStack icon, String tooltip) {
		super(index, toggled, command, icon, tooltip);
		this.index = index;
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderWidget(context, mouseX, mouseY, delta);
		if (!toggled()) {
			context.drawTexture(RenderPipelines.GUI_TEXTURED, DOUBLE_CLICK_ICON,
					getX() + 5, getY() + 8 + (index < 7 ? 1 : -1),
					0, 0, 16, 16, 16, 16);
			long now = System.currentTimeMillis();
			long timeSinceClick = now - lastClicked;
			if (timeSinceClick < DOUBLE_CLICK_TIME != showingConfirmTooltip) {
				showingConfirmTooltip = !showingConfirmTooltip;
				setTooltip(showingConfirmTooltip ? CONFIRM_TOOLTIP : tooltip);
			}
		}
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		long now = System.currentTimeMillis();
		if (now - lastClicked > DOUBLE_CLICK_TIME) {
			lastClicked = System.currentTimeMillis();
			return;
		}

		super.onClick(mouseX, mouseY);
	}
}
