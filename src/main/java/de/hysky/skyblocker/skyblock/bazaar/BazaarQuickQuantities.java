package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.skyblock.calculators.SignCalculator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.math.NumberUtils;

public class BazaarQuickQuantities {
	private static final Minecraft client = Minecraft.getInstance();

	public static Button[] getButtons(int width, String[] messages) {
		Button[] buttons = new Button[4];
		UIAndVisualsConfig.BazaarQuickQuantities config = SkyblockerConfigManager.get().uiAndVisuals.bazaarQuickQuantities;

		int x = width / 2 + 50;
		int y = 45;

		buttons[0] = Button.builder(Component.nullToEmpty(String.valueOf(config.slot1Quantity)),
				button -> onButtonPressed(messages, config.slot1Quantity)).bounds(x, y + 20, 50, 20).build();
		buttons[1] = Button.builder(Component.nullToEmpty(String.valueOf(config.slot2Quantity)),
				button -> onButtonPressed(messages, config.slot2Quantity)).bounds(x, y + 40, 50, 20).build();
		buttons[2] = Button.builder(Component.nullToEmpty(String.valueOf(config.slot3Quantity)),
				button -> onButtonPressed(messages, config.slot3Quantity)).bounds(x, y + 60, 50, 20).build();

		String clipboard = client.keyboardHandler.getClipboard();
		if (clipboard.length() <= 5 && NumberUtils.isCreatable(clipboard)) { // Only show option if clipboard is numeric
			MutableComponent text = Component.literal(clipboard);
			text.withStyle(style -> style.withColor(ChatFormatting.AQUA));

			buttons[3] = Button.builder(text,
					button -> onButtonPressed(messages, clipboard)).bounds(x, y + 80, 50, 20).build();
		}
		return buttons;
	}

	static void onButtonPressed(String[] messages, int value) {
		onButtonPressed(messages, String.valueOf(value));
	}

	static void onButtonPressed(String[] messages, String value) {
		messages[0] = value;

		UIAndVisualsConfig.BazaarQuickQuantities config = SkyblockerConfigManager.get().uiAndVisuals.bazaarQuickQuantities;
		if (config.closeSignOnUse && client.screen != null) {
			SignCalculator.calculate(messages[0]); // Avoid conflict on `finishEditing` with SignCalculator
			client.screen.onClose();
		}
	}
}
