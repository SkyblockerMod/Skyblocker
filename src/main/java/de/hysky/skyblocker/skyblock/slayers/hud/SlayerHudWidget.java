package de.hysky.skyblocker.skyblock.slayers.hud;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerConstants;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;

public class SlayerHudWidget extends Widget {
	public static final SlayerHudWidget INSTANCE = new SlayerHudWidget();
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final NumberFormat numberFormat = NumberFormat.getInstance();

	public SlayerHudWidget() {
		super(Text.literal("Slayer").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), Formatting.DARK_PURPLE.getColorValue());
		setX(SkyblockerConfigManager.get().slayers.slayerHud.x);
		setY(SkyblockerConfigManager.get().slayers.slayerHud.y);
		update();
	}

	@Override
	public void updateContent() {
		if (client.player == null) return;
		String type = SlayerManager.slayerType;
		String tier = SlayerManager.slayerTier;
		int level = SlayerManager.level;
		addSimpleIcoText(Ico.NETHER_STAR, " ", Formatting.GOLD, type + " " + tier);
		if (level != -1) {
			boolean isMaxed = switch (type) {
				case SlayerConstants.VAMPIRE -> level == 5;
				default -> level == 9;
			};
			if (isMaxed) {
				addSimpleIcoText(Ico.ENCHANTING_TABLE, I18n.translate("skyblocker.slayer.hud.lvlUpIn") + ": ", Formatting.GREEN, I18n.translate("skyblocker.slayer.hud.lvlMaxed"));
			} else {
				int nextMilestone = switch (type) {
					case SlayerConstants.REVENANT -> SlayerConstants.ZombieLevelMilestones[level];
					case SlayerConstants.TARA -> SlayerConstants.SpiderLevelMilestones[level];
					case SlayerConstants.VAMPIRE -> SlayerConstants.VampireLevelMilestones[level];
					default -> SlayerConstants.RegularLevelMilestones[level];
				};
				int currentXP = nextMilestone - SlayerManager.xpRemaining;
				addSimpleIcoText(Ico.ENCHANTING_TABLE, I18n.translate("skyblocker.slayer.hud.lvlUpIn") + ": ", Formatting.AQUA, numberFormat.format(currentXP) + "/" + numberFormat.format(nextMilestone));
			}
		}
		if (SlayerManager.xpRemaining > 0) {
			addSimpleIcoText(Ico.DIASWORD, I18n.translate("skyblocker.slayer.hud.bossesNeeded") + ": ", Formatting.AQUA, numberFormat.format(SlayerManager.calculateBossesNeeded()));
		}
	}
}
