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
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@RegisterWidget
public class SweepDetailsHudWidget extends ComponentBasedWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	// Doing this will allow these axes to be affected by SkyBlock resource packs.
	private static final Supplier<ItemStack> SWEET_AXE = ItemRepository.getItemStackSupplier("SWEET_AXE");
	private static final Supplier<ItemStack> TREECAPITATOR_AXE = ItemRepository.getItemStackSupplier("TREECAPITATOR_AXE");
	private static final Supplier<ItemStack> FIGSTONE_AXE = ItemRepository.getItemStackSupplier("FIGSTONE_AXE");
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
	public static final Set<Location> LOCATIONS = Set.of(Location.GALATEA, Location.HUB, Location.THE_PARK);

	public SweepDetailsHudWidget() {
		super(Text.translatable("skyblocker.galatea.hud.sweepDetails"), 0xFF6E37CC, "sweepDetails");
		update();
	}

	@Override
	public boolean shouldRender(Location location) {
		return (!Utils.getLocation().equals(Location.HUB) || Utils.getArea().equals(Area.FOREST)) && super.shouldRender(location);
	}

	@Override
	public void updateContent() {
		if (CLIENT.player == null || CLIENT.currentScreen instanceof WidgetsConfigurationScreen) {
			addComponent(Components.iconTextComponent(new ItemStack(Items.STRIPPED_SPRUCE_LOG), Text.translatable("skyblocker.galatea.hud.sweepDetails.treeType", "Fig")));
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.toughness", 3.5)));
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.sweep", 314.15)));
			return;
		}
		if (!SweepDetailsListener.active || System.currentTimeMillis() > SweepDetailsListener.lastMatch + 1_000) {
			SweepDetailsListener.active = false;
			ItemStack axeIcon = switch (Utils.getLocation()) {
				case HUB -> Optional.ofNullable(SWEET_AXE.get()).orElse(new ItemStack(Items.IRON_AXE));
				case THE_PARK -> Optional.ofNullable(TREECAPITATOR_AXE.get()).orElse(new ItemStack(Items.GOLDEN_AXE));
				case GALATEA -> Optional.ofNullable(FIGSTONE_AXE.get()).orElse(new ItemStack(Items.STONE_AXE));
				default -> RED_CONCRETE;
			};
			addComponent(Components.iconTextComponent(axeIcon, Text.translatable("skyblocker.galatea.hud.sweepDetails.inactive")));
			return;
		}

		ItemStack logItem = LOG_TO_ITEM.getOrDefault(SweepDetailsListener.lastTreeType, RED_CONCRETE);
		addComponent(Components.iconTextComponent(logItem, Text.translatable("skyblocker.galatea.hud.sweepDetails.treeType", SweepDetailsListener.lastTreeType)));
		addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.toughness", SweepDetailsListener.toughness)));

		Text sweepAmount;
		if (SweepDetailsListener.maxSweep > SweepDetailsListener.lastSweep) {
			MutableText lastSweep = Text.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.lastSweep)).withColor(Colors.LIGHT_RED);
			Text thisSweep = Text.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.maxSweep)).withColor(Colors.GREEN);
			sweepAmount = lastSweep.append(Text.literal(" (").withColor(Colors.WHITE)).append(thisSweep).append(Text.literal(")").withColor(Colors.WHITE));
		} else {
			sweepAmount = Text.literal(Formatters.DOUBLE_NUMBERS.format(SweepDetailsListener.maxSweep)).withColor(Colors.GREEN);
		}
		addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.sweep", sweepAmount)));

		addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.logs", Text.literal(SweepDetailsListener.logs).withColor(Colors.GREEN))));

		if (SweepDetailsListener.axePenalty) {
			addComponent(Components.iconTextComponent(new ItemStack(Items.BARRIER), Text.translatable("skyblocker.galatea.hud.sweepDetails.throwPenalty", SweepDetailsListener.axePenaltyAmount + "%")));
		}

		if (SweepDetailsListener.stylePenalty) {
			addComponent(Components.iconTextComponent(new ItemStack(Items.BARRIER), Text.translatable("skyblocker.galatea.hud.sweepDetails.stylePenalty", SweepDetailsListener.stylePenaltyAmount + "%")));
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.correctStyle", SweepDetailsListener.correctStyle)));
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
	public Text getDisplayName() {
		return Text.translatable("skyblocker.galatea.hud.sweepDetails");
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}
}
