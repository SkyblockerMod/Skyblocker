package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.TextureTextElement;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

@RegisterWidget
public class SlayerHudWidget extends ElementBasedWidget {
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.CRIMSON_ISLE, Location.HUB, Location.SPIDERS_DEN, Location.THE_END, Location.THE_PARK, Location.THE_RIFT);
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final int TEXTURE_SIZE = 16;
	private static @Nullable SlayerHudWidget instance;

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
		return Objects.requireNonNull(instance, "SlayerHudWidget not initialized");
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!availableLocations().contains(location)) return;
		SkyblockerConfigManager.update(config -> config.slayers.enableHud = enabled);
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
		if (CLIENT.screen instanceof WidgetsConfigurationScreen) {
			SlayerType slayerType = SlayerType.REVENANT;
			SlayerTier slayerTier = SlayerTier.V;

			Component slayerName = Component.literal(slayerType.bossName + " " + slayerTier).withStyle(slayerTier.color);
			addComponent(new TextureTextElement(slayerName, slayerType.texture, TEXTURE_SIZE, TEXTURE_SIZE));
			addSimpleIcoText(Ico.EXPERIENCE_BOTTLE, "XP: ", ChatFormatting.LIGHT_PURPLE, "100,000/400,000");
			addComponent(Elements.iconTextComponent(Ico.NETHER_STAR, Component.translatable("skyblocker.slayer.hud.levelUpIn", Component.literal("200").withStyle(ChatFormatting.LIGHT_PURPLE))));
			return;
		}

		SlayerManager.SlayerQuest slayerQuest = SlayerManager.getSlayerQuest();
		if (CLIENT.player == null || slayerQuest == null) return;

		SlayerType slayerType = slayerQuest.slayerType;
		SlayerTier slayerTier = slayerQuest.slayerTier;
		int level = slayerQuest.level;
		int bossesNeeded = slayerQuest.bossesNeeded;

		Component slayerName = Component.literal(slayerType.bossName + " " + slayerTier).withStyle(slayerTier.color);
		addComponent(new TextureTextElement(slayerName, slayerType.texture, TEXTURE_SIZE, TEXTURE_SIZE));

		if (level == slayerType.maxLevel) {
			addComponent(Elements.iconTextComponent(Ico.EXPERIENCE_BOTTLE, Component.literal("XP: ").append(Component.translatable("skyblocker.slayer.hud.levelMaxed").withStyle(ChatFormatting.GREEN))));
		} else if (level >= 0) {
			int nextMilestone = slayerType.levelMilestones[level];
			int currentXP = nextMilestone - slayerQuest.xpRemaining;
			addSimpleIcoText(Ico.EXPERIENCE_BOTTLE, "XP: ", ChatFormatting.LIGHT_PURPLE, Formatters.INTEGER_NUMBERS.format(currentXP) + "/" + Formatters.INTEGER_NUMBERS.format(nextMilestone));

			if (bossesNeeded > 0) {
				addComponent(Elements.iconTextComponent(Ico.NETHER_STAR, Component.translatable("skyblocker.slayer.hud.levelUpIn", Component.literal(Formatters.INTEGER_NUMBERS.format(bossesNeeded)).withStyle(ChatFormatting.LIGHT_PURPLE))));
			}
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Slayer Hud");
	}
}
