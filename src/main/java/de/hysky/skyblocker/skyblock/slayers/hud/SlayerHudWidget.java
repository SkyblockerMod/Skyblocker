package de.hysky.skyblocker.skyblock.slayers.hud;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerTier;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
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
		if (client.player == null || SlayerManager.getSlayerQuest() == null) return;

		SlayerType type = SlayerManager.getSlayerType();
		SlayerTier tier = SlayerManager.getSlayerTier();
		int level = SlayerManager.getSlayerQuest().level;
		int bossesNeeded = SlayerManager.getSlayerQuest().bossesNeeded;

		if (type == null || tier == null) return;

		addSimpleIcoText(Ico.NETHER_STAR, "", tier.color, type.bossName + " " + tier);
		if (level > 0) {
			if (level == type.maxLevel) {
				addComponent(new IcoTextComponent(Ico.ENCHANTING_TABLE, Text.literal("XP: ").append(Text.translatable("skyblocker.slayer.hud.levelMaxed").formatted(Formatting.GREEN))));
			} else {
				int nextMilestone = type.levelMilestones[level];
				int currentXP = nextMilestone - SlayerManager.getSlayerQuest().xpRemaining;
				addSimpleIcoText(Ico.ENCHANTING_TABLE, "XP: ", Formatting.LIGHT_PURPLE, numberFormat.format(currentXP) + "/" + numberFormat.format(nextMilestone));
			}
		}

		if (bossesNeeded > 0) {
			addComponent(new IcoTextComponent(Ico.DIASWORD, Text.translatable("skyblocker.slayer.hud.levelUpIn", Text.literal(numberFormat.format(bossesNeeded)).formatted(Formatting.LIGHT_PURPLE))));
		}
	}
}
