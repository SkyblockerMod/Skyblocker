package de.hysky.skyblocker.skyblock.foraging.galatea;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Items;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.ElementCollector;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;

@RegisterWidget
public class SweepDetailsHudWidget extends ElementBasedWidget {
	private static final Map<String, FlexibleItemStack> LOG_TO_ITEM = Map.of(
			"Fig", new FlexibleItemStack(Items.STRIPPED_SPRUCE_LOG),
			"Mangrove", new FlexibleItemStack(Items.MANGROVE_LOG),
			"Helix", new FlexibleItemStack(Items.STRIPPED_MANGROVE_WOOD),
			"Jungle", new FlexibleItemStack(Items.JUNGLE_LOG),
			"Acacia", new FlexibleItemStack(Items.ACACIA_LOG),
			"Dark Oak", new FlexibleItemStack(Items.DARK_OAK_LOG),
			"Spruce", new FlexibleItemStack(Items.SPRUCE_LOG),
			"Birch", new FlexibleItemStack(Items.BIRCH_LOG),
			"Oak", new FlexibleItemStack(Items.OAK_LOG)
	);
	public static final Set<Location> LOCATIONS = EnumSet.of(Location.GALATEA, Location.HUB, Location.THE_PARK, Location.GARDEN, Location.TORRHUS_CANYON);
	public static SweepDetailsHudWidget INSTANCE;

	public SweepDetailsHudWidget() {
		super(Component.translatable("skyblocker.galatea.hud.sweepDetails"), 0xFF6E37CC, new Information("sweep_details", Component.literal("Sweep Details"), LOCATIONS));
		INSTANCE = this;
		update();
	}

	@Override
	public boolean shouldRender() {
		// While in the hub only show in the forest and foraging camp
		return (!Utils.getLocation().equals(Location.HUB) || Utils.getArea() == Area.Hub.FOREST || Utils.getArea() == Area.Hub.FORAGING_CAMP)
			// While in the garden only show in unclean plots
			&& (!Utils.getLocation().equals(Location.GARDEN) || Utils.STRING_SCOREBOARD.stream().anyMatch(s -> s.contains("Cleanup")));
	}

	@Override
	public void updateContent() {
		if (!SweepDetailsListener.active || System.currentTimeMillis() > SweepDetailsListener.lastMatch + SweepDetailsListener.TIMEOUT_MS) {
			SweepDetailsListener.resetStats();
			FlexibleItemStack axeIcon = switch (Utils.getLocation()) {
				case HUB -> ItemRepository.getItemStack("SWEET_AXE", Ico.IRON_AXE);
				case THE_PARK -> ItemRepository.getItemStack("TREECAPITATOR_AXE", Ico.GOLDEN_AXE);
				case GALATEA -> ItemRepository.getItemStack("FIGSTONE_AXE", Ico.STONE_AXE);
				case TORRHUS_CANYON -> ItemRepository.getItemStack("HELIX_CHOPPER", Ico.GOLDEN_AXE);
				default -> Ico.RED_CONCRETE;
			};
			addElement(Elements.iconTextComponent(axeIcon, Component.translatable("skyblocker.galatea.hud.sweepDetails.inactive")));
			return;
		}

		FlexibleItemStack logItem = LOG_TO_ITEM.getOrDefault(SweepDetailsListener.lastTreeType, Ico.RED_CONCRETE);
		addElement(Elements.iconTextComponent(logItem, Component.translatable("skyblocker.galatea.hud.sweepDetails.treeType", SweepDetailsListener.lastTreeType)));
		addElement(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.toughness", SweepDetailsListener.toughness)));

		Component sweepAmount;
		if (SweepDetailsListener.maxSweep > SweepDetailsListener.lastSweep) {
			MutableComponent lastSweep = Component.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.lastSweep)).withColor(CommonColors.SOFT_RED);
			Component thisSweep = Component.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.maxSweep)).withColor(CommonColors.GREEN);
			sweepAmount = lastSweep.append(Component.literal(" (").withColor(CommonColors.WHITE)).append(thisSweep).append(Component.literal(")").withColor(CommonColors.WHITE));
		} else {
			sweepAmount = Component.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.maxSweep)).withColor(CommonColors.GREEN);
		}
		addElement(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.sweep", sweepAmount)));

		addElement(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.logs", Component.literal(SweepDetailsListener.logs).withColor(CommonColors.GREEN))));

		if (SweepDetailsListener.axePenalty) {
			addElement(Elements.iconTextComponent(Ico.BARRIER, Component.translatable("skyblocker.galatea.hud.sweepDetails.throwPenalty", SweepDetailsListener.axePenaltyAmount + "%")));
		}

		if (SweepDetailsListener.stylePenalty) {
			addElement(Elements.iconTextComponent(Ico.BARRIER, Component.translatable("skyblocker.galatea.hud.sweepDetails.stylePenalty", SweepDetailsListener.stylePenaltyAmount + "%")));
			addElement(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.correctStyle", SweepDetailsListener.correctStyle)));
		}
	}

	@Override
	protected void updateConfigContent(ElementCollector collector) {
		collector.addElement(Elements.iconTextComponent(new FlexibleItemStack(Items.STRIPPED_SPRUCE_LOG), Component.translatable("skyblocker.galatea.hud.sweepDetails.treeType", "Fig")));
		collector.addElement(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.toughness", 3.5)));
		collector.addElement(new PlainTextElement(Component.translatable("skyblocker.galatea.hud.sweepDetails.sweep", 314.15)));
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}
}
