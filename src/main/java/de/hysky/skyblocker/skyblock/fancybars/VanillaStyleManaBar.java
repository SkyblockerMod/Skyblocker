package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.StatusBarTracker;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class VanillaStyleManaBar {
	private static int lastManaValue;
	private static int manaValueBlinkStart;

	private static int lastOverflowValue;
	private static int overflowValueBlinkStart;

	private static long blinkEndTime;

	// Two versions of the same bar, one that renders when the hunger bar is visible and one for the mount health bar
	// Ideally we would only need one bar, but since there is not currently a way to override vanilla render conditions this will work
	private static final Identifier MANABAR_FOOD_HUD_ID = SkyblockerMod.id("vanilla_style_mana_bar_food");
	private static final Identifier MANABAR_MOUNT_HUD_ID = SkyblockerMod.id("vanilla_style_mana_bar_mount");

	private static final Identifier CONTAINER_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/container");
	private static final Identifier MANA_FULL_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/mana_full");
	private static final Identifier MANA_HALF_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/mana_half");
	private static final Identifier OVERFLOW_FULL_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/overflow_full");
	private static final Identifier OVERFLOW_HALF_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/overflow_half");
	private static final Identifier OVERFLOW_DARK_FULL_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/overflow_dark_full");
	private static final Identifier OVERFLOW_DARK_HALF_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/overflow_dark_half");
	private static final Identifier CONTAINER_BLINK_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/container_blink");
	private static final Identifier MANA_FULL_BLINK_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/mana_full_blink");
	private static final Identifier MANA_HALF_BLINK_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/mana_half_blink");
	private static final Identifier OVERFLOW_FULL_BLINK_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/overflow_full_blink");
	private static final Identifier OVERFLOW_HALF_BLINK_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/overflow_half_blink");
	private static final Identifier OVERFLOW_DARK_FULL_BLINK_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/overflow_dark_full_blink");
	private static final Identifier OVERFLOW_DARK_HALF_BLINK_TEXTURE = SkyblockerMod.id("bars/vanilla_mana/overflow_dark_half_blink");

	enum NotchType {
		CONTAINER,
		MANA,
		OVERFLOW,
		OVERFLOW_DARK
	}

	@Init
	public static void init() {
		Function<HudElement, HudElement> hideIfVanillaStyleManaBarEnabled = hudElement -> {
			if (isEnabled())
				return (context, tickCounter) -> {};
			return hudElement;
		};

		HudElementRegistry.replaceElement(VanillaHudElements.FOOD_BAR, hideIfVanillaStyleManaBarEnabled);
		HudElementRegistry.replaceElement(VanillaHudElements.MOUNT_HEALTH, hideIfVanillaStyleManaBarEnabled);

		HudElementRegistry.attachElementBefore(VanillaHudElements.FOOD_BAR, MANABAR_FOOD_HUD_ID, (context, tickCounter) -> {
			if (isEnabled()) render(context);
		});
		HudElementRegistry.attachElementBefore(VanillaHudElements.MOUNT_HEALTH, MANABAR_MOUNT_HUD_ID, (context, tickCounter) -> {
			if (isEnabled()) render(context);
		});
	}

	private static boolean isEnabled() {
		return Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.bars.enableVanillaStyleManaBar  && !FancyStatusBars.isEnabled();
	}

	private static void drawNotch(GuiGraphics context, int column, int row, NotchType notchtype, boolean isHalf, boolean isBlinking) {
		int top = context.guiHeight() - 39;       // Top of mana bar area
		int right = context.guiWidth() / 2 + 91;  // Rightmost point of mana bar area

		Identifier texture = switch (notchtype) {
			case CONTAINER -> isBlinking ? CONTAINER_BLINK_TEXTURE : CONTAINER_TEXTURE;
			case MANA -> !isHalf ? (isBlinking ? MANA_FULL_BLINK_TEXTURE : MANA_FULL_TEXTURE) : (isBlinking ? MANA_HALF_BLINK_TEXTURE : MANA_HALF_TEXTURE);
			case OVERFLOW -> !isHalf ? (isBlinking ? OVERFLOW_FULL_BLINK_TEXTURE : OVERFLOW_FULL_TEXTURE) : (isBlinking ? OVERFLOW_HALF_BLINK_TEXTURE : OVERFLOW_HALF_TEXTURE);
			case OVERFLOW_DARK -> !isHalf ? (isBlinking ? OVERFLOW_DARK_FULL_BLINK_TEXTURE : OVERFLOW_DARK_FULL_TEXTURE) : (isBlinking ? OVERFLOW_DARK_HALF_BLINK_TEXTURE : OVERFLOW_DARK_HALF_TEXTURE);
		};

		context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, right - column * 8 - 9, top - row * 10, 9, 9);
	}

	public static boolean render(GuiGraphics context) {
		StatusBarTracker.Resource mana = StatusBarTracker.getMana();

		// Detect loss of mana to start blinking
		long currentTime = Util.getMillis();
		final long BLINK_TIME_LENGTH = 1000;
		final long BLINK_FREQUENCY = 300;
		if (lastManaValue + lastOverflowValue > mana.value() + mana.overflow() && mana.value() != mana.max()) {
			boolean justStartedBlinking = blinkEndTime <= currentTime;
			if (justStartedBlinking) {
				manaValueBlinkStart = lastManaValue;
				overflowValueBlinkStart = lastOverflowValue;
			}
			// Increment blink end time
			// If already blinking, keep blinkEndTick%BLINK_FREQUENCY the same to maintain current blink animation frame, otherwise increment normally
			if (blinkEndTime >= currentTime) {
				blinkEndTime = ((currentTime + BLINK_TIME_LENGTH) / BLINK_FREQUENCY * BLINK_FREQUENCY) + blinkEndTime % BLINK_FREQUENCY;
			} else {
				blinkEndTime = currentTime + BLINK_TIME_LENGTH;
			}
		}
		boolean blinking = blinkEndTime > currentTime && (blinkEndTime - currentTime) / (BLINK_FREQUENCY/2) % 2 == 1;
		if (blinkEndTime <= currentTime) {
			manaValueBlinkStart = 0;
			overflowValueBlinkStart = 0;
		}

		lastManaValue = mana.value();
		lastOverflowValue = mana.overflow();

		// Notches are each of the mana icons, 20 for 2 bars
		final int MANA_NOTCH_COUNT = 20;
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

			drawNotch(context, column, row, NotchType.CONTAINER, false, blinking);
			if (manaNotches > 0) { // There is normal mana left, display normal mana
				if (overflowNotch) drawNotch(context, column, row, NotchType.OVERFLOW_DARK, overflowNotchIsHalf, blinking);
				if (manaBlinkNotch && blinking) drawNotch(context, column, row, NotchType.MANA, manaBlinkNotchIsHalf, true);
				if (manaNotch) drawNotch(context, column, row, NotchType.MANA, manaNotchIsHalf, false);
			} else { // There is no normal mana left, display overflow mana
				if (manaBlinkNotch && blinking) drawNotch(context, column, row, NotchType.MANA, manaBlinkNotchIsHalf, true);
				if (overflowBlinkNotch && blinking) drawNotch(context, column, row, NotchType.OVERFLOW, overflowBlinkNotchIsHalf, true);
				if (overflowNotch) drawNotch(context, column, row, NotchType.OVERFLOW, overflowNotchIsHalf, false);
			}
		}

		return true;
	}
}
