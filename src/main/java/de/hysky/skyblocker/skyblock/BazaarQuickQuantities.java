package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
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

		buttons[0] = ButtonWidget.builder(Text.of(String.valueOf(config.slot1Quantity)), button -> {
			messages[0] = String.valueOf(config.slot1Quantity);
			if (config.closeSignOnUse) client.setScreen(null);
		}).dimensions(x, y + 20, 50, 20).build();
		buttons[1] = ButtonWidget.builder(Text.of(String.valueOf(config.slot2Quantity)), button -> {
			messages[0] = String.valueOf(config.slot2Quantity);
			if (config.closeSignOnUse) client.setScreen(null);
		}).dimensions(x, y + 40, 50, 20).build();
		buttons[2] = ButtonWidget.builder(Text.of(String.valueOf(config.slot3Quantity)), button -> {
			messages[0] = String.valueOf(config.slot3Quantity);
			if (config.closeSignOnUse) client.setScreen(null);
		}).dimensions(x, y + 60, 50, 20).build();

		String clipboard = client.keyboard.getClipboard();
		if (clipboard.length() <= 5 && NumberUtils.isCreatable(clipboard)) { // Only show option if clipboard is numeric
			MutableText text = Text.literal(clipboard);
			text.styled(style -> style.withColor(Formatting.AQUA));

			buttons[3] = ButtonWidget.builder(text, button -> {
				messages[0] = clipboard;
				if (config.closeSignOnUse) client.setScreen(null);
			}).dimensions(x, y + 80, 50, 20).build();
		}
		return buttons;
	}
}
