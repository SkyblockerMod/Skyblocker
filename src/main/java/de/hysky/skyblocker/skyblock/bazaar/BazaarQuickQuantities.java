package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.skyblock.calculators.SignCalculator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;

public class BazaarQuickQuantities {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	public static ButtonWidget[] getButtons(int width, String[] messages) {
		ButtonWidget[] buttons = new ButtonWidget[4];
		UIAndVisualsConfig.BazaarQuickQuantities config = SkyblockerConfigManager.get().uiAndVisuals.bazaarQuickQuantities;

		int x = width / 2 + 50;
		int y = 45;

		buttons[0] = ButtonWidget.builder(Text.of(String.valueOf(config.slot1Quantity)),
				button -> onButtonPressed(messages, config.slot1Quantity)).dimensions(x, y + 20, 50, 20).build();
		buttons[1] = ButtonWidget.builder(Text.of(String.valueOf(config.slot2Quantity)),
				button -> onButtonPressed(messages, config.slot2Quantity)).dimensions(x, y + 40, 50, 20).build();
		buttons[2] = ButtonWidget.builder(Text.of(String.valueOf(config.slot3Quantity)),
				button -> onButtonPressed(messages, config.slot3Quantity)).dimensions(x, y + 60, 50, 20).build();

		String clipboard = client.keyboard.getClipboard();
		if (clipboard.length() <= 5 && NumberUtils.isCreatable(clipboard)) { // Only show option if clipboard is numeric
			MutableText text = Text.literal(clipboard);
			text.styled(style -> style.withColor(Formatting.AQUA));

			buttons[3] = ButtonWidget.builder(text,
					button -> onButtonPressed(messages, clipboard)).dimensions(x, y + 80, 50, 20).build();
		}
		return buttons;
	}

	static void onButtonPressed(String[] messages, int value) {
		onButtonPressed(messages, String.valueOf(value));
	}

	static void onButtonPressed(String[] messages, String value) {
		messages[0] = value;

		UIAndVisualsConfig.BazaarQuickQuantities config = SkyblockerConfigManager.get().uiAndVisuals.bazaarQuickQuantities;
		if (config.closeSignOnUse && client.currentScreen != null) {
			SignCalculator.calculate(messages[0]); // Avoid conflict on `finishEditing` with SignCalculator
			client.currentScreen.close();
		}
	}
}
