package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;

@RegisterWidget
public class SweepDetailsHudWidget extends ComponentBasedWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Map<String, ItemStack> LOG_TO_ITEM = Map.of(
			"Fig", new ItemStack(Items.STRIPPED_SPRUCE_LOG),
			"Mangrove", new ItemStack(Items.MANGROVE_LOG),
			"Jungle", new ItemStack(Items.JUNGLE_LOG),
			"Acacia", new ItemStack(Items.ACACIA_LOG),
			"Dark Oak", new ItemStack(Items.DARK_OAK_LOG),
			"Spruce", new ItemStack(Items.SPRUCE_LOG),
			"Birch", new ItemStack(Items.BIRCH_LOG)
	);
	private static final ItemStack RED_CONCRETE = new ItemStack(Items.RED_CONCRETE);
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.00");
	public static final Set<Location> LOCATIONS = Set.of(Location.GALATEA, Location.THE_PARK);

	public SweepDetailsHudWidget() {
		super(Text.translatable("skyblocker.galatea.hud.sweepDetails"), 0xFF6E37CC, "sweepDetails");
		update();
	}

	@Override
	public void updateContent() {
		if (CLIENT.player == null || CLIENT.currentScreen instanceof WidgetsConfigurationScreen) {
			addComponent(new IcoTextComponent(new ItemStack(Items.STRIPPED_SPRUCE_LOG), Text.translatable("skyblocker.galatea.hud.sweepDetails.treeType", "Fig")));
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.toughness", 3.5)));
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.sweep", 314.15)));
			return;
		}

		if (!SweepDetailsListener.active || System.currentTimeMillis() > SweepDetailsListener.lastMatch + 1_000) {
			SweepDetailsListener.active = false;
			addComponent(new IcoTextComponent(new ItemStack(Items.STONE_AXE), Text.translatable("skyblocker.galatea.hud.sweepDetails.inactive")));
			return;
		}

		ItemStack logItem = LOG_TO_ITEM.getOrDefault(SweepDetailsListener.lastTreeType, RED_CONCRETE);
		addComponent(new IcoTextComponent(logItem, Text.translatable("skyblocker.galatea.hud.sweepDetails.treeType", SweepDetailsListener.lastTreeType)));
		addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.toughness", SweepDetailsListener.toughness)));

		Text sweepAmount;
		final int greenColor = 0xFF00FF00;
		final int redColor = 0xFFFF5555;
		final int defaultColor = 0xFFFFFFFF;
		if (SweepDetailsListener.maxSweep > SweepDetailsListener.lastSweep) {
			MutableText lastSweep = Text.literal(FORMATTER.format(SweepDetailsListener.lastSweep)).withColor(redColor);
			Text thisSweep = Text.literal(FORMATTER.format(SweepDetailsListener.maxSweep)).withColor(greenColor);
			sweepAmount = lastSweep.append(Text.literal(" (").withColor(defaultColor)).append(thisSweep).append(Text.literal(")").withColor(defaultColor));
		} else {
			sweepAmount = Text.literal(FORMATTER.format(SweepDetailsListener.maxSweep)).withColor(greenColor);
		}
		addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.sweep", sweepAmount)));

		addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.logs", Text.literal(SweepDetailsListener.logs).withColor(greenColor))));

		if (SweepDetailsListener.axePenalty) {
			addComponent(new IcoTextComponent(new ItemStack(Items.BARRIER), Text.translatable("skyblocker.galatea.hud.sweepDetails.throwPenalty", SweepDetailsListener.axePenaltyAmount + "%")));
		}

		if (SweepDetailsListener.stylePenalty) {
			addComponent(new IcoTextComponent(new ItemStack(Items.BARRIER), Text.translatable("skyblocker.galatea.hud.sweepDetails.stylePenalty", SweepDetailsListener.stylePenaltyAmount + "%")));
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
