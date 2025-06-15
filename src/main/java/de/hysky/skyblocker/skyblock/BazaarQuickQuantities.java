package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class BazaarQuickQuantities {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	public static ButtonWidget[] getButtons(int width, String[] messages) {
		ButtonWidget[] buttons = new ButtonWidget[3];
		UIAndVisualsConfig.BazaarQuickQuantities config = SkyblockerConfigManager.get().uiAndVisuals.bazaarQuickQuantities;

		int x = width / 2 + 50;
		int y = 45;

		buttons[0] = ButtonWidget.builder(Text.of(String.valueOf(config.slot1Quantity)), button -> {
			messages[0] = String.valueOf(config.slot1Quantity);
			if (config.closeSignOnUse) client.setScreen(null);
		}).dimensions(x, y += 20, 50, 20).build();
		buttons[1] = ButtonWidget.builder(Text.of(String.valueOf(config.slot2Quantity)), button -> {
			messages[0] = String.valueOf(config.slot2Quantity);
			if (config.closeSignOnUse) client.setScreen(null);
		}).dimensions(x, y += 20, 50, 20).build();
		buttons[2] = ButtonWidget.builder(Text.of(String.valueOf(config.slot3Quantity)), button -> {
			messages[0] = String.valueOf(config.slot3Quantity);
			if (config.closeSignOnUse) client.setScreen(null);
		}).dimensions(x, y + 20, 50, 20).build();
		return buttons;
	}
}
