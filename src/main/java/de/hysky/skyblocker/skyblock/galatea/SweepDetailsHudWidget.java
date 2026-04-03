package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Items;

@RegisterWidget
public class SweepDetailsHudWidget extends ElementBasedWidget {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Map<String, FlexibleItemStack> LOG_TO_ITEM = Map.of(
			"Fig", new FlexibleItemStack(Items.STRIPPED_SPRUCE_LOG),
			"Mangrove", new FlexibleItemStack(Items.MANGROVE_LOG),
			"Jungle", new FlexibleItemStack(Items.JUNGLE_LOG),
			"Acacia", new FlexibleItemStack(Items.ACACIA_LOG),
			"Dark Oak", new FlexibleItemStack(Items.DARK_OAK_LOG),
			"Spruce", new FlexibleItemStack(Items.SPRUCE_LOG),
			"Birch", new FlexibleItemStack(Items.BIRCH_LOG),
			"Oak", new FlexibleItemStack(Items.OAK_LOG)
	);
	public static final Set<Location> LOCATIONS = Set.of(Location.GALATEA, Location.HUB, Location.THE_PARK, Location.GARDEN);

	public SweepDetailsHudWidget() {
		super(Component.translatable("skyblocker.galatea.hud.sweepDetails"), 0xFF6E37CC, "sweepDetails");
		update();
	}

	@Override
	public boolean shouldRender(Location location) {
		// While in the hub only show in the forest and foraging camp
		return (!Utils.getLocation().equals(Location.HUB) || Utils.getArea() == Area.Hub.FOREST || Utils.getArea() == Area.Hub.FORAGING_CAMP)
			// While in the garden only show in unclean plots
			&& (!Utils.getLocation().equals(Location.GARDEN) || Utils.STRING_SCOREBOARD.stream().anyMatch(s -> s.contains("Cleanup")))
			&& super.shouldRender(location);
	}

	@Override
	public void updateContent() {
		if (CLIENT.player == null || CLIENT.screen instanceof WidgetsConfigurationScreen) {
			addComponent(Elements.iconTextComponent(new FlexibleItemStack(Items.STRIPPED_SPRUCE_LOG), Component.translatable("skyblocker.galatea.hud.sweepDetails.treeType", "Fig")));
			addComponent(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.toughness", 3.5)));
			addComponent(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.sweep", 314.15)));
			return;
		}
		if (!SweepDetailsListener.active || System.currentTimeMillis() > SweepDetailsListener.lastMatch + 1_000) {
			SweepDetailsListener.active = false;
			FlexibleItemStack axeIcon = switch (Utils.getLocation()) {
				case HUB -> ItemRepository.getItemStack("SWEET_AXE", new FlexibleItemStack(Items.IRON_AXE));
				case THE_PARK -> ItemRepository.getItemStack("TREECAPITATOR_AXE", new FlexibleItemStack(Items.GOLDEN_AXE));
				case GALATEA -> ItemRepository.getItemStack("FIGSTONE_AXE", new FlexibleItemStack(Items.STONE_AXE));
				default -> Ico.RED_CONCRETE;
			};
			addComponent(Elements.iconTextComponent(axeIcon, Component.translatable("skyblocker.galatea.hud.sweepDetails.inactive")));
			return;
		}

		FlexibleItemStack logItem = LOG_TO_ITEM.getOrDefault(SweepDetailsListener.lastTreeType, Ico.RED_CONCRETE);
		addComponent(Elements.iconTextComponent(logItem, Component.translatable("skyblocker.galatea.hud.sweepDetails.treeType", SweepDetailsListener.lastTreeType)));
		addComponent(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.toughness", SweepDetailsListener.toughness)));

		Component sweepAmount;
		if (SweepDetailsListener.maxSweep > SweepDetailsListener.lastSweep) {
			MutableComponent lastSweep = Component.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.lastSweep)).withColor(CommonColors.SOFT_RED);
			Component thisSweep = Component.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.maxSweep)).withColor(CommonColors.GREEN);
			sweepAmount = lastSweep.append(Component.literal(" (").withColor(CommonColors.WHITE)).append(thisSweep).append(Component.literal(")").withColor(CommonColors.WHITE));
		} else {
			sweepAmount = Component.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.maxSweep)).withColor(CommonColors.GREEN);
		}
		addComponent(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.sweep", sweepAmount)));

		addComponent(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.logs", Component.literal(SweepDetailsListener.logs).withColor(CommonColors.GREEN))));

		if (SweepDetailsListener.axePenalty) {
			addComponent(Elements.iconTextComponent(Ico.BARRIER, Component.translatable("skyblocker.galatea.hud.sweepDetails.throwPenalty", SweepDetailsListener.axePenaltyAmount + "%")));
		}

		if (SweepDetailsListener.stylePenalty) {
			addComponent(Elements.iconTextComponent(Ico.BARRIER, Component.translatable("skyblocker.galatea.hud.sweepDetails.stylePenalty", SweepDetailsListener.stylePenaltyAmount + "%")));
			addComponent(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.correctStyle", SweepDetailsListener.correctStyle)));
		}
	}

	@Override
	public Set<Location> availableLocations() {
		return LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!availableLocations().contains(location)) return;
		SkyblockerConfigManager.update(config -> config.foraging.galatea.enableSweepDetailsWidget = enabled);
	}

	@Override
	public boolean isEnabledIn(Location location) {
		if (!availableLocations().contains(location)) return false;
		return SkyblockerConfigManager.get().foraging.galatea.enableSweepDetailsWidget;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("skyblocker.galatea.hud.sweepDetails");
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}
}
