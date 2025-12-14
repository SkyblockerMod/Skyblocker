package de.hysky.skyblocker.skyblock.quicknav;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

/**
 * A special type of {@link QuickNavButton} that requires double-clicking to open.
 */
public class QuickNavConfirmationButton extends QuickNavButton {
	private static final Tooltip CONFIRM_TOOLTIP = Tooltip.create(Component.translatable("skyblocker.quickNav.confirm"));
	private static final long DOUBLE_CLICK_TIME = 1000;
	private long lastClicked = 0;
	private boolean showingConfirmTooltip;

	protected final int index;

	/**
	 * See {@link QuickNavButton#QuickNavButton(int, boolean, String, ItemStack, String)}
	 */
	public QuickNavConfirmationButton(int index, boolean toggled, String command, ItemStack icon, String tooltip) {
		super(index, toggled, command, icon, tooltip);
		this.index = index;
	}

	@Override
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderWidget(context, mouseX, mouseY, delta);
		if (toggled()) return;
		if (isDoubleClick() == showingConfirmTooltip) return;
		showingConfirmTooltip = !showingConfirmTooltip;
		setTooltip(showingConfirmTooltip ? CONFIRM_TOOLTIP : tooltip);
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		if (isDoubleClick()) {
			super.playDownSound(soundManager);
			return;
		}
		soundManager.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_CHIME, 1.0F));
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		if (!isDoubleClick()) {
			lastClicked = System.currentTimeMillis();
			return;
		}

		super.onClick(click, doubled);
	}

	private boolean isDoubleClick() {
		long now = System.currentTimeMillis();
		return now - lastClicked < DOUBLE_CLICK_TIME;
	}
}
