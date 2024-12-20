package de.hysky.skyblocker.skyblock.slayers.hud;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerTier;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;
import java.util.Set;

@RegisterWidget
public class SlayerHudWidget extends ComponentBasedWidget {
	public static final SlayerHudWidget INSTANCE = new SlayerHudWidget();
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final NumberFormat numberFormat = NumberFormat.getInstance();

	public SlayerHudWidget() {
		super(Text.literal("Slayer").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), Formatting.DARK_PURPLE.getColorValue(), "hud_slayer");
		setX(SkyblockerConfigManager.get().slayers.slayerHud.x);
		setY(SkyblockerConfigManager.get().slayers.slayerHud.y);
		update();
	}

	@Override
	public Set<Location> availableLocations() {
		return Set.of(Location.CRIMSON_ISLE, Location.HUB, Location.SPIDERS_DEN, Location.THE_END, Location.THE_PARK, Location.THE_RIFT);
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!availableLocations().contains(location)) return;
		SkyblockerConfigManager.get().slayers.slayerHud.enableHud = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return availableLocations().contains(location) && SkyblockerConfigManager.get().slayers.slayerHud.enableHud && SlayerManager.isInSlayer() && !SlayerManager.getSlayerType().isUnknown() && !SlayerManager.getSlayerTier().isUnknown();
	}

	@Override
	public void updateContent() {
		if (client.player == null || SlayerManager.getSlayerQuest() == null) return;

		SlayerType type = SlayerManager.getSlayerType();
		SlayerTier tier = SlayerManager.getSlayerTier();
		int level = SlayerManager.getSlayerQuest().level;
		int bossesNeeded = SlayerManager.getSlayerQuest().bossesNeeded;

		if (type == null || tier == null) return;

		addSimpleIcoText(type.icon, "", tier.color, type.bossName + " " + tier);
		if (level > 0) {
			if (level == type.maxLevel) {
				addComponent(new IcoTextComponent(Ico.EXPERIENCE_BOTTLE, Text.literal("XP: ").append(Text.translatable("skyblocker.slayer.hud.levelMaxed").formatted(Formatting.GREEN))));
			} else {
				int nextMilestone = type.levelMilestones[level];
				int currentXP = nextMilestone - SlayerManager.getSlayerQuest().xpRemaining;
				addSimpleIcoText(Ico.EXPERIENCE_BOTTLE, "XP: ", Formatting.LIGHT_PURPLE, numberFormat.format(currentXP) + "/" + numberFormat.format(nextMilestone));
			}
		}

		if (bossesNeeded > 0) {
			addComponent(new IcoTextComponent(Ico.NETHER_STAR, Text.translatable("skyblocker.slayer.hud.levelUpIn", Text.literal(numberFormat.format(bossesNeeded)).formatted(Formatting.LIGHT_PURPLE))));
		}
	}

	@Override
	public Text getDisplayName() {
		return Text.literal("Slayer Hud");
	}
}
