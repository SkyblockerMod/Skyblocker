package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.List;
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
	public static final Set<Location> LOCATIONS = Set.of(Location.GALATEA, Location.THE_PARK);

	public SweepDetailsHudWidget() {
		super(Text.translatable("skyblocker.galatea.hud.sweepDetails"), 0xFF6E37CC, "sweepDetails", LOCATIONS);
		update();
	}

	@Override
	public void updateContent() {
		if (!SweepDetailsListener.active || System.currentTimeMillis() > SweepDetailsListener.lastMatch + 1_000) {
			SweepDetailsListener.active = false;
			addComponent(new IcoTextComponent(new ItemStack(Items.STONE_AXE), Text.translatable("skyblocker.galatea.hud.sweepDetails.inactive")));
			return;
		}

		ItemStack logItem = LOG_TO_ITEM.getOrDefault(SweepDetailsListener.lastTreeType, RED_CONCRETE);
		addComponent(new IcoTextComponent(logItem, Text.translatable("skyblocker.galatea.hud.sweepDetails.treeType", SweepDetailsListener.lastTreeType)));
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
			addComponent(new IcoTextComponent(new ItemStack(Items.BARRIER), Text.translatable("skyblocker.galatea.hud.sweepDetails.throwPenalty", SweepDetailsListener.axePenaltyAmount + "%")));
		}

		if (SweepDetailsListener.stylePenalty) {
			addComponent(new IcoTextComponent(new ItemStack(Items.BARRIER), Text.translatable("skyblocker.galatea.hud.sweepDetails.stylePenalty", SweepDetailsListener.stylePenaltyAmount + "%")));
			addComponent(new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.correctStyle", SweepDetailsListener.correctStyle)));
		}
	}

	@Override
	protected List<Component> getConfigComponents() {
		return List.of(
				new IcoTextComponent(new ItemStack(Items.STRIPPED_SPRUCE_LOG), Text.translatable("skyblocker.galatea.hud.sweepDetails.treeType", "Fig")),
				new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.toughness", 3.5)),
				new PlainTextComponent(Text.translatable("skyblocker.galatea.hud.sweepDetails.sweep", 314.15))
		);
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}
}
