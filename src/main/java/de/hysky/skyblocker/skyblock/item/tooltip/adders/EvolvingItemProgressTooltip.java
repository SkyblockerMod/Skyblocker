package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class EvolvingItemProgressTooltip extends SimpleTooltipAdder {
	final List<String> evolutionItems = List.of("DARK_CACAO_TRUFFLE", "MOBY_DUCK", "ROSEWATER_FLASK", "DISCRITE", "NEW_BOTTLE_OF_JYRRE", "TRAINING_WEIGHTS");

	public EvolvingItemProgressTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		if (!evolutionItems.contains(stack.getSkyblockId())) return;

		CompoundTag customData = ItemUtils.getCustomData(stack);
		int secondsHeld = switch (stack.getSkyblockId()) {
			case "DISCRITE" -> customData.getIntOr("rift_discrite_seconds", 0);
			case "NEW_BOTTLE_OF_JYRRE" -> customData.getIntOr("bottle_of_jyrre_seconds", 0);
			case "TRAINING_WEIGHTS" -> customData.getIntOr("trainingWeightsHeldTime", 0);
			default -> customData.getIntOr("seconds_held", 0);
		};
		int maxSeconds = switch (stack.getSkyblockId()) {
			case "TRAINING_WEIGHTS" -> 5500*60; // 5,500 minutes
			default -> 3600*300; // 300 hours
		};

		lines.add(Component.empty()
				.append(Component.literal(String.format("%-23s", "Age: ")).withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.format("%3.1f", (float) secondsHeld/3600)).withStyle(ChatFormatting.RED))
				.append(Component.literal("/").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.format("%3.1fh ", (float) maxSeconds/3600)).withStyle(ChatFormatting.RED))
				.append(Component.literal("(").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(String.format("%2.1f%%", 100.0*secondsHeld/maxSeconds)).withStyle(ChatFormatting.RED))
				.append(Component.literal(")").withStyle(ChatFormatting.GRAY)));
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemTooltip.enableEvolvingItemProgress;
	}
}
