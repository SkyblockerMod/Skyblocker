package de.hysky.skyblocker.skyblock.slayers.hud;

import de.hysky.skyblocker.config.HudConfigScreen;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class SlayerHudConfigScreen extends HudConfigScreen {
	public SlayerHudConfigScreen(Screen parent) {
		super(Text.literal("Slayer HUD Config"), parent, SlayerHudWidget.INSTANCE);
	}

	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	protected List<IntIntMutablePair> getConfigPos(SkyblockerConfig config) {
		return List.of(
				IntIntMutablePair.of(config.slayers.slayerHud.x, config.slayers.slayerHud.y)
		);
	}

	@Override
	protected void savePos(SkyblockerConfig configManager, List<HudWidget> widgets) {
		configManager.slayers.slayerHud.x = widgets.getFirst().getX();
		configManager.slayers.slayerHud.y = widgets.getFirst().getY();
	}
}
