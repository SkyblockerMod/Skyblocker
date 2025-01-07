package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class Components {
	public static Component progressComponent(Text text, ItemStack icon, Text description, float percent) {
		return SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isFancy() ? new ProgressComponent(icon, description, percent) : new PlainTextComponent(text);
	}

	public static Component progressComponent(Text text, ItemStack icon, Text description, Text bar, float percent) {
		return SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isFancy() ? new ProgressComponent(icon, description, bar, percent) : new PlainTextComponent(text);
	}
}
