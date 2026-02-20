package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@RegisterWidget
public class SweepDetailsHudWidget extends ComponentBasedWidget {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Map<String, ItemStack> LOG_TO_ITEM = Map.of(
			"Fig", new ItemStack(Items.STRIPPED_SPRUCE_LOG),
			"Mangrove", new ItemStack(Items.MANGROVE_LOG),
			"Jungle", new ItemStack(Items.JUNGLE_LOG),
			"Acacia", new ItemStack(Items.ACACIA_LOG),
			"Dark Oak", new ItemStack(Items.DARK_OAK_LOG),
			"Spruce", new ItemStack(Items.SPRUCE_LOG),
			"Birch", new ItemStack(Items.BIRCH_LOG),
			"Oak", new ItemStack(Items.OAK_LOG)
	);
	private static final ItemStack RED_CONCRETE = new ItemStack(Items.RED_CONCRETE);
	public static final Set<Location> LOCATIONS = Set.of(Location.GALATEA, Location.HUB, Location.THE_PARK, Location.GARDEN);

	public SweepDetailsHudWidget() {
		super(Component.translatable("skyblocker.galatea.hud.sweepDetails"), 0xFF6E37CC, "sweepDetails");
		update();
	}

	@Override
	public boolean shouldRender(Location location) {
		// While in the hub only show in the forest
		return (!Utils.getLocation().equals(Location.HUB) || Utils.getArea() == Area.Hub.FOREST)
			// While in the garden only show in unclean plots
			&& (!Utils.getLocation().equals(Location.GARDEN) || Utils.STRING_SCOREBOARD.stream().anyMatch(s -> s.contains("Cleanup")))
			&& super.shouldRender(location);
	}

	@Override
	public void updateContent() {
		if (CLIENT.player == null || CLIENT.screen instanceof WidgetsConfigurationScreen) {
			addComponent(Components.iconTextComponent(new ItemStack(Items.STRIPPED_SPRUCE_LOG), Component.translatable("skyblocker.galatea.hud.sweepDetails.treeType", "Fig")));
			addComponent(new PlainTextComponent(Component.translatable("skyblocker.galatea.hud.sweepDetails.toughness", 3.5)));
			addComponent(new PlainTextComponent(Component.translatable("skyblocker.galatea.hud.sweepDetails.sweep", 314.15)));
			return;
		}
		if (!SweepDetailsListener.active || System.currentTimeMillis() > SweepDetailsListener.lastMatch + 1_000) {
			SweepDetailsListener.active = false;
			ItemStack axeIcon = switch (Utils.getLocation()) {
				case HUB -> ItemRepository.getItemStack("SWEET_AXE", new ItemStack(Items.IRON_AXE));
				case THE_PARK -> ItemRepository.getItemStack("TREECAPITATOR_AXE", new ItemStack(Items.GOLDEN_AXE));
				case GALATEA -> ItemRepository.getItemStack("FIGSTONE_AXE", new ItemStack(Items.STONE_AXE));
				default -> RED_CONCRETE;
			};
			addComponent(Components.iconTextComponent(axeIcon, Component.translatable("skyblocker.galatea.hud.sweepDetails.inactive")));
			return;
		}

		ItemStack logItem = LOG_TO_ITEM.getOrDefault(SweepDetailsListener.lastTreeType, RED_CONCRETE);
		addComponent(Components.iconTextComponent(logItem, Component.translatable("skyblocker.galatea.hud.sweepDetails.treeType", SweepDetailsListener.lastTreeType)));
		addComponent(new PlainTextComponent(Component.translatable("skyblocker.galatea.hud.sweepDetails.toughness", SweepDetailsListener.toughness)));

		Component sweepAmount;
		if (SweepDetailsListener.maxSweep > SweepDetailsListener.lastSweep) {
			MutableComponent lastSweep = Component.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.lastSweep)).withColor(CommonColors.SOFT_RED);
			Component thisSweep = Component.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.maxSweep)).withColor(CommonColors.GREEN);
			sweepAmount = lastSweep.append(Component.literal(" (").withColor(CommonColors.WHITE)).append(thisSweep).append(Component.literal(")").withColor(CommonColors.WHITE));
		} else {
			sweepAmount = Component.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.maxSweep)).withColor(CommonColors.GREEN);
		}
		addComponent(new PlainTextComponent(Component.translatable("skyblocker.galatea.hud.sweepDetails.sweep", sweepAmount)));

		addComponent(new PlainTextComponent(Component.translatable("skyblocker.galatea.hud.sweepDetails.logs", Component.literal(SweepDetailsListener.logs).withColor(CommonColors.GREEN))));

		if (SweepDetailsListener.axePenalty) {
			addComponent(Components.iconTextComponent(new ItemStack(Items.BARRIER), Component.translatable("skyblocker.galatea.hud.sweepDetails.throwPenalty", SweepDetailsListener.axePenaltyAmount + "%")));
		}

		if (SweepDetailsListener.stylePenalty) {
			addComponent(Components.iconTextComponent(new ItemStack(Items.BARRIER), Component.translatable("skyblocker.galatea.hud.sweepDetails.stylePenalty", SweepDetailsListener.stylePenaltyAmount + "%")));
			addComponent(new PlainTextComponent(Component.translatable("skyblocker.galatea.hud.sweepDetails.correctStyle", SweepDetailsListener.correctStyle)));
		}
	}

	@Override
	public Set<Location> availableLocations() {
		return LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!availableLocations().contains(location)) return;
		SkyblockerConfigManager.get().foraging.galatea.enableSweepDetailsWidget = enabled;
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
