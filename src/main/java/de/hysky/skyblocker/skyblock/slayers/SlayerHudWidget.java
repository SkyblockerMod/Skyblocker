package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TextureTextComponent;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@RegisterWidget
public class SlayerHudWidget extends ComponentBasedWidget {
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.CRIMSON_ISLE, Location.HUB, Location.SPIDERS_DEN, Location.THE_END, Location.THE_PARK, Location.THE_RIFT);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final int TEXTURE_SIZE = 16;
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
		return super.shouldRender(location) && SlayerManager.isInSlayerQuest();
	}

	@Override
	public void updateContent() {
		if (CLIENT.currentScreen instanceof WidgetsConfigurationScreen) {
			SlayerType slayerType = SlayerType.REVENANT;
			SlayerTier slayerTier = SlayerTier.V;

			Text slayerName = Text.literal(slayerType.bossName + " " + slayerTier).formatted(slayerTier.color);
			addComponent(new TextureTextComponent(slayerName, slayerType.texture, TEXTURE_SIZE, TEXTURE_SIZE));
			addSimpleIcoText(Ico.EXPERIENCE_BOTTLE, "XP: ", Formatting.LIGHT_PURPLE, "100,000/400,000");
			addComponent(Components.iconTextComponent(Ico.NETHER_STAR, Text.translatable("skyblocker.slayer.hud.levelUpIn", Text.literal("200").formatted(Formatting.LIGHT_PURPLE))));
			return;
		}

		SlayerManager.SlayerQuest slayerQuest = SlayerManager.getSlayerQuest();
		if (CLIENT.player == null || slayerQuest == null) return;

		SlayerType slayerType = slayerQuest.slayerType;
		SlayerTier slayerTier = slayerQuest.slayerTier;
		int level = slayerQuest.level;
		int bossesNeeded = slayerQuest.bossesNeeded;

		Text slayerName = Text.literal(slayerType.bossName + " " + slayerTier).formatted(slayerTier.color);
		addComponent(new TextureTextComponent(slayerName, slayerType.texture, TEXTURE_SIZE, TEXTURE_SIZE));

		if (level == slayerType.maxLevel) {
			addComponent(Components.iconTextComponent(Ico.EXPERIENCE_BOTTLE, Text.literal("XP: ").append(Text.translatable("skyblocker.slayer.hud.levelMaxed").formatted(Formatting.GREEN))));
		} else if (level >= 0) {
			int nextMilestone = slayerType.levelMilestones[level];
			int currentXP = nextMilestone - slayerQuest.xpRemaining;
			addSimpleIcoText(Ico.EXPERIENCE_BOTTLE, "XP: ", Formatting.LIGHT_PURPLE, Formatters.INTEGER_NUMBERS.format(currentXP) + "/" + Formatters.INTEGER_NUMBERS.format(nextMilestone));

			if (bossesNeeded > 0) {
				addComponent(Components.iconTextComponent(Ico.NETHER_STAR, Text.translatable("skyblocker.slayer.hud.levelUpIn", Text.literal(Formatters.INTEGER_NUMBERS.format(bossesNeeded)).formatted(Formatting.LIGHT_PURPLE))));
			}
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Slayer Hud");
	}
}
