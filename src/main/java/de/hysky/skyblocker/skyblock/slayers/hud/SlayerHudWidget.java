package de.hysky.skyblocker.skyblock.slayers.hud;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerTier;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@RegisterWidget
public class SlayerHudWidget extends ComponentBasedWidget {
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.CRIMSON_ISLE, Location.HUB, Location.SPIDERS_DEN, Location.THE_END, Location.THE_PARK, Location.THE_RIFT);
	private static SlayerHudWidget instance;
	private final Minecraft client = Minecraft.getInstance();

	public SlayerHudWidget() {
		super(Component.literal("Slayer").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), ChatFormatting.DARK_PURPLE.getColor(), "hud_slayer");
		instance = this;
		update();
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	public static SlayerHudWidget getInstance() {
		return instance;
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!availableLocations().contains(location)) return;
		SkyblockerConfigManager.get().slayers.enableHud = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return availableLocations().contains(location) && SkyblockerConfigManager.get().slayers.enableHud;
	}

	@Override
	public boolean shouldRender(Location location) {
		return super.shouldRender(location) && SlayerManager.isInSlayer() && !SlayerManager.getSlayerType().isUnknown() && !SlayerManager.getSlayerTier().isUnknown();
	}

	@Override
	public void updateContent() {
		if (Minecraft.getInstance().screen instanceof WidgetsConfigurationScreen) {
			SlayerType type = SlayerType.REVENANT;
			SlayerTier tier = SlayerTier.V;

			addSimpleIcoText(type.icon, "", tier.color, type.bossName + " " + tier);
			addSimpleIcoText(Ico.EXPERIENCE_BOTTLE, "XP: ", ChatFormatting.LIGHT_PURPLE, "100,000/400,000");
			addComponent(Components.iconTextComponent(Ico.NETHER_STAR, Component.translatable("skyblocker.slayer.hud.levelUpIn", Component.literal("200").withStyle(ChatFormatting.LIGHT_PURPLE))));
			return;
		}

		if (client.player == null || SlayerManager.getSlayerQuest() == null) return;

		SlayerType type = SlayerManager.getSlayerType();
		SlayerTier tier = SlayerManager.getSlayerTier();
		int level = SlayerManager.getSlayerQuest().level;
		int bossesNeeded = SlayerManager.getSlayerQuest().bossesNeeded;

		if (type == null || tier == null) return;

		addSimpleIcoText(type.icon, "", tier.color, type.bossName + " " + tier);
		if (level > 0) {
			if (level == type.maxLevel) {
				addComponent(Components.iconTextComponent(Ico.EXPERIENCE_BOTTLE, Component.literal("XP: ").append(Component.translatable("skyblocker.slayer.hud.levelMaxed").withStyle(ChatFormatting.GREEN))));
			} else {
				int nextMilestone = type.levelMilestones[level];
				int currentXP = nextMilestone - SlayerManager.getSlayerQuest().xpRemaining;
				addSimpleIcoText(Ico.EXPERIENCE_BOTTLE, "XP: ", ChatFormatting.LIGHT_PURPLE, Formatters.INTEGER_NUMBERS.format(currentXP) + "/" + Formatters.INTEGER_NUMBERS.format(nextMilestone));
			}
		}

		if (bossesNeeded > 0) {
			addComponent(Components.iconTextComponent(Ico.NETHER_STAR, Component.translatable("skyblocker.slayer.hud.levelUpIn", Component.literal(Formatters.INTEGER_NUMBERS.format(bossesNeeded)).withStyle(ChatFormatting.LIGHT_PURPLE))));
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Slayer Hud");
	}
}
