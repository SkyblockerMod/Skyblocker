package de.hysky.skyblocker.skyblock.slayers.hud;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerTier;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@RegisterWidget
public class SlayerHudWidget extends ComponentBasedWidget {
	private static final Set<Location> AVAILABLE_LOCATIONS = EnumSet.of(Location.CRIMSON_ISLE, Location.HUB, Location.SPIDERS_DEN, Location.THE_END, Location.THE_PARK, Location.THE_RIFT);
	private static SlayerHudWidget instance;
	private final MinecraftClient client = MinecraftClient.getInstance();

	public SlayerHudWidget() {
		super(Text.literal("Slayer").formatted(Formatting.DARK_PURPLE, Formatting.BOLD), Formatting.DARK_PURPLE.getColorValue(), new Information("hud_slayer", Text.literal("Slayer HUD"), AVAILABLE_LOCATIONS::contains)); // TODO translatable
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
	public boolean shouldRender() {
		return SlayerManager.isInSlayer() && !SlayerManager.getSlayerType().isUnknown() && !SlayerManager.getSlayerTier().isUnknown();
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
				addSimpleIcoText(Ico.EXPERIENCE_BOTTLE, "XP: ", Formatting.LIGHT_PURPLE, Formatters.INTEGER_NUMBERS.format(currentXP) + "/" + Formatters.INTEGER_NUMBERS.format(nextMilestone));
			}
		}

		if (bossesNeeded > 0) {
			addComponent(new IcoTextComponent(Ico.NETHER_STAR, Text.translatable("skyblocker.slayer.hud.levelUpIn", Text.literal(Formatters.INTEGER_NUMBERS.format(bossesNeeded)).formatted(Formatting.LIGHT_PURPLE))));
		}
	}

	@Override
	protected List<Component> getConfigComponents() {
		SlayerType type = SlayerType.REVENANT;
		SlayerTier tier = SlayerTier.V;
		return List.of(
				new IcoTextComponent(type.icon, simpleEntryText(type.bossName + " " + tier, "", tier.color)),
				new IcoTextComponent(Ico.EXPERIENCE_BOTTLE, simpleEntryText("100,000/400,000", "XP: ", Formatting.LIGHT_PURPLE)),
				new IcoTextComponent(Ico.NETHER_STAR, Text.translatable("skyblocker.slayer.hud.levelUpIn", Text.literal("200").formatted(Formatting.LIGHT_PURPLE)))
		);
	}
}
