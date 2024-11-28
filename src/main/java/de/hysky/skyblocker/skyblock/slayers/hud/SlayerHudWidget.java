package de.hysky.skyblocker.skyblock.slayers.hud;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerConstants;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.utils.RomanNumerals;
import net.minecraft.client.MinecraftClient;
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

		addSimpleIcoText(Ico.NETHER_STAR, " ", SlayerConstants.SLAYER_TIERS_COLORS.get(RomanNumerals.romanToDecimal(tier)), type + " " + tier);

		if (level != -1) {
			boolean isMaxed = switch (type) {
				case SlayerConstants.VAMPIRE -> level == 5;
				default -> level == 9;
			};
			if (isMaxed) {
				addComponent(new IcoTextComponent(Ico.ENCHANTING_TABLE, Text.literal("XP: ").append(Text.translatable("skyblocker.slayer.hud.levelMaxed").formatted(Formatting.GREEN))));
			} else {
				int nextMilestone = switch (type) {
					case SlayerConstants.REVENANT -> SlayerConstants.ZombieLevelMilestones[level];
					case SlayerConstants.TARA -> SlayerConstants.SpiderLevelMilestones[level];
					case SlayerConstants.VAMPIRE -> SlayerConstants.VampireLevelMilestones[level];
					default -> SlayerConstants.RegularLevelMilestones[level];
				};
				int currentXP = nextMilestone - SlayerManager.xpRemaining;
				addSimpleIcoText(Ico.ENCHANTING_TABLE, "XP: ", Formatting.LIGHT_PURPLE, numberFormat.format(currentXP) + "/" + numberFormat.format(nextMilestone));
			}
		}

		if (SlayerManager.bossesNeeded > 1) {
			addComponent(new IcoTextComponent(Ico.DIASWORD, Text.translatable("skyblocker.slayer.hud.levelUpIn", Text.literal(numberFormat.format(SlayerManager.bossesNeeded)).formatted(Formatting.LIGHT_PURPLE))));
		}
	}
}
