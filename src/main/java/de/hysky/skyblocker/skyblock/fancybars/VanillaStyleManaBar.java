package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.StatusBarTracker;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class VanillaStyleManaBar {
	private int lastManaValue;
	private int manaValueBlinkStart;

	private int lastOverflowValue;
	private int overflowValueBlinkStart;

	private int blinkEndTick;

	private static final Identifier CONTAINER_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/container");
	private static final Identifier MANA_FULL_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/mana_full");
	private static final Identifier MANA_HALF_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/mana_half");
	private static final Identifier OVERFLOW_FULL_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/overflow_full");
	private static final Identifier OVERFLOW_HALF_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/overflow_half");
	private static final Identifier OVERFLOW_DARK_FULL_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/overflow_dark_full");
	private static final Identifier OVERFLOW_DARK_HALF_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/overflow_dark_half");
	private static final Identifier CONTAINER_BLINK_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/container_blink");
	private static final Identifier MANA_FULL_BLINK_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/mana_full_blink");
	private static final Identifier MANA_HALF_BLINK_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/mana_half_blink");
	private static final Identifier OVERFLOW_FULL_BLINK_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/overflow_full_blink");
	private static final Identifier OVERFLOW_HALF_BLINK_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/overflow_half_blink");
	private static final Identifier OVERFLOW_DARK_FULL_BLINK_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/overflow_dark_full_blink");
	private static final Identifier OVERFLOW_DARK_HALF_BLINK_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "bars/vanilla_mana/overflow_dark_half_blink");


	enum NotchType {
		CONTAINER,
		MANA,
		OVERFLOW,
		OVERFLOW_DARK
	}

	private void drawNotch(DrawContext context, int top, int right, int column, int row, NotchType notchtype, boolean isHalf, boolean isBlinking) {
		int right_offset = right - column * 8 - 9;

		Identifier texture = switch (notchtype) {
			case CONTAINER -> isBlinking ? CONTAINER_BLINK_TEXTURE : CONTAINER_TEXTURE;
			case MANA -> !isHalf ? (isBlinking ? MANA_FULL_BLINK_TEXTURE : MANA_FULL_TEXTURE) : (isBlinking ? MANA_HALF_BLINK_TEXTURE : MANA_HALF_TEXTURE);
			case OVERFLOW -> !isHalf ? (isBlinking ? OVERFLOW_FULL_BLINK_TEXTURE : OVERFLOW_FULL_TEXTURE) : (isBlinking ? OVERFLOW_HALF_BLINK_TEXTURE : OVERFLOW_HALF_TEXTURE);
			case OVERFLOW_DARK -> !isHalf ? (isBlinking ? OVERFLOW_DARK_FULL_BLINK_TEXTURE : OVERFLOW_DARK_FULL_TEXTURE) : (isBlinking ? OVERFLOW_DARK_HALF_BLINK_TEXTURE : OVERFLOW_DARK_HALF_TEXTURE);
		};

		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, right_offset, top - row * 10, 9, 9);
	}

	public boolean render(DrawContext context, int top, int right, int ticks) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.bars.enableVanillaStyleManaBar) return false;
		StatusBarTracker.Resource mana = StatusBarTracker.getMana();

		// Detect loss of mana to start blinking
		if (lastManaValue + lastOverflowValue > mana.value() + mana.overflow() && mana.value() != mana.max()) {
			boolean justStartedBlinking = blinkEndTick <= ticks;
			if (justStartedBlinking) {
				manaValueBlinkStart = lastManaValue;
				overflowValueBlinkStart = lastOverflowValue;
			}
			// If already blinking, keep blinkEndTick%6 the same to maintain current blink animation frame
			if (blinkEndTick >= ticks) {
				blinkEndTick = (ticks + 20) / 6 * 6 + blinkEndTick % 6;
			} else {
				blinkEndTick = ticks + 20;
			}
		}
		boolean blinking = blinkEndTick > ticks && (blinkEndTick - ticks) / 3 % 2 == 1;
		if (blinkEndTick <= ticks) {
			manaValueBlinkStart = 0;
			overflowValueBlinkStart = 0;
		}

		lastManaValue = mana.value();
		lastOverflowValue = mana.overflow();

		// Notches are each of the mana icons, 20 for 2 bars
		int MANA_NOTCH_COUNT = 20;
		int manaHalfNotches = mana.value() * MANA_NOTCH_COUNT * 2 / mana.max();
		int manaNotches = (int) Math.ceil(manaHalfNotches / 2.0);
		int manaBlinkHalfNotches = manaValueBlinkStart * MANA_NOTCH_COUNT * 2 / mana.max();
		int manaBlinkNotches = (int) Math.ceil(manaBlinkHalfNotches / 2.0);
		int overflowHalfNotches = mana.overflow() * MANA_NOTCH_COUNT * 2 / mana.max();
		int overflowNotches = (int) Math.ceil(overflowHalfNotches / 2.0);
		int overflowBlinkHalfNotches = overflowValueBlinkStart * MANA_NOTCH_COUNT * 2 / mana.max();
		int overflowBlinkNotches = (int) Math.ceil(overflowBlinkHalfNotches / 2.0);

		for (int i = 0; i < MANA_NOTCH_COUNT; i++) {
			int row = (i / 10);
			int column = i % 10;

			boolean manaNotch = i < manaNotches;
			boolean manaNotchIsHalf = manaNotch && manaNotches - 1 == i && manaHalfNotches % 2 == 1;
			boolean manaBlinkNotch = i < manaBlinkNotches;
			boolean manaBlinkNotchIsHalf = manaBlinkNotch && manaBlinkNotches - 1 == i && manaBlinkHalfNotches % 2 == 1;
			boolean overflowNotch = i < overflowNotches;
			boolean overflowNotchIsHalf = overflowNotch && overflowNotches - 1 == i && overflowHalfNotches % 2 == 1;
			boolean overflowBlinkNotch = i < overflowBlinkNotches;
			boolean overflowBlinkNotchIsHalf = overflowBlinkNotch && overflowBlinkNotches - 1 == i && overflowBlinkHalfNotches % 2 == 1;

			drawNotch(context, top, right, column, row, NotchType.CONTAINER, false, blinking);
			if (manaNotches > 0) { // There is normal mana left, display normal mana
				if (overflowNotch) drawNotch(context, top, right, column, row, NotchType.OVERFLOW_DARK, overflowNotchIsHalf, blinking);
				if (manaBlinkNotch && blinking) drawNotch(context, top, right, column, row, NotchType.MANA, manaBlinkNotchIsHalf, true);
				if (manaNotch) drawNotch(context, top, right, column, row, NotchType.MANA, manaNotchIsHalf, false);
			} else { // There is no normal mana left, display overflow mana
				if (manaBlinkNotch && blinking) drawNotch(context, top, right, column, row, NotchType.MANA, manaBlinkNotchIsHalf, true);
				if (overflowBlinkNotch && blinking) drawNotch(context, top, right, column, row, NotchType.OVERFLOW, overflowBlinkNotchIsHalf, true);
				if (overflowNotch) drawNotch(context, top, right, column, row, NotchType.OVERFLOW, overflowNotchIsHalf, false);
			}
		}

		return true;
	}
}
