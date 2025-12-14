package de.hysky.skyblocker.skyblock.slayers.hud;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerTier;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

@RegisterWidget
public class SlayerHudWidget extends ComponentBasedWidget {
	private static final Set<Location> AVAILABLE_LOCATIONS = EnumSet.of(Location.CRIMSON_ISLE, Location.HUB, Location.SPIDERS_DEN, Location.THE_END, Location.THE_PARK, Location.THE_RIFT);
	private static SlayerHudWidget instance;
	private final Minecraft client = Minecraft.getInstance();

	public SlayerHudWidget() {
		super(net.minecraft.network.chat.Component.literal("Slayer").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), ChatFormatting.DARK_PURPLE.getColor(), new Information("hud_slayer", net.minecraft.network.chat.Component.literal("Slayer HUD"), AVAILABLE_LOCATIONS::contains)); // TODO translatable
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
				addComponent(Components.iconTextComponent(Ico.EXPERIENCE_BOTTLE, net.minecraft.network.chat.Component.literal("XP: ").append(net.minecraft.network.chat.Component.translatable("skyblocker.slayer.hud.levelMaxed").withStyle(ChatFormatting.GREEN))));
			} else {
				int nextMilestone = type.levelMilestones[level];
				int currentXP = nextMilestone - SlayerManager.getSlayerQuest().xpRemaining;
				addSimpleIcoText(Ico.EXPERIENCE_BOTTLE, "XP: ", ChatFormatting.LIGHT_PURPLE, Formatters.INTEGER_NUMBERS.format(currentXP) + "/" + Formatters.INTEGER_NUMBERS.format(nextMilestone));
			}
		}

		if (bossesNeeded > 0) {
			addComponent(Components.iconTextComponent(Ico.NETHER_STAR, net.minecraft.network.chat.Component.translatable("skyblocker.slayer.hud.levelUpIn", net.minecraft.network.chat.Component.literal(Formatters.INTEGER_NUMBERS.format(bossesNeeded)).withStyle(ChatFormatting.LIGHT_PURPLE))));
		}
	}

	@Override
	protected List<Component> getConfigComponents() {
		SlayerType type = SlayerType.REVENANT;
		SlayerTier tier = SlayerTier.V;
		return List.of(
				Components.iconTextComponent(type.icon, simpleEntryText(type.bossName + " " + tier, "", tier.color)),
				Components.iconTextComponent(Ico.EXPERIENCE_BOTTLE, simpleEntryText("100,000/400,000", "XP: ", ChatFormatting.LIGHT_PURPLE)),
				Components.iconTextComponent(Ico.NETHER_STAR, net.minecraft.network.chat.Component.translatable("skyblocker.slayer.hud.levelUpIn", net.minecraft.network.chat.Component.literal("200").withStyle(ChatFormatting.LIGHT_PURPLE)))
		);
	}
}
