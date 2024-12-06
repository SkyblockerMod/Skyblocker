package de.hysky.skyblocker.skyblock.item.tooltip.adders;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class StackingEnchantProgressTooltip extends SimpleTooltipAdder {
	private static final Set<String> STACKING_ENCHANT_IDS = Set.of("expertise", "compact", "cultivating", "champion", "hecatomb", "toxophilite");
	private static final StackingEnchantInfo EXPERTISE_INFO = new StackingEnchantInfo("Expertise", "expertise_kills", "kills", 0, 50, 100, 250, 500, 1000, 2500, 5500, 10_000, 15_000);
	private static final StackingEnchantInfo COMPACT_INFO = new StackingEnchantInfo("Compact", "compact_blocks", "blocks", 0, 100, 500, 1500, 5000, 15_000, 50_000, 150_000, 500_000, 1_000_000);
	private static final StackingEnchantInfo CULTIVATING_INFO = new StackingEnchantInfo("Cultivating", "farmed_cultivating", "crops", 0, 1000, 5000, 25_000, 100_000, 300_000, 1_500_000, 5_000_000, 20_000_000, 100_000_000);
	private static final StackingEnchantInfo CHAMPION_INFO = new StackingEnchantInfo("Champion", "champion_combat_xp", "Combat XP", 0, 50_000, 100_000, 250_000, 500_000, 1_000_000, 1_500_000, 2_000_000, 2_500_000, 3_000_000);
	private static final StackingEnchantInfo HECATOMB_INFO = new StackingEnchantInfo("Hecatomb", "hecatomb_s_runs", "S runs", 0, 2, 5, 10, 20, 30, 40, 60, 80, 100);
	private static final StackingEnchantInfo TOXOPHILITE_INFO = new StackingEnchantInfo("Toxophilite", "toxophilite_combat_xp", "Combat XP", 0, 50_000, 100_000, 250_000, 500_000, 1_000_000, 1_500_000, 2_000_000, 2_500_000, 3_000_000);
	private static final NumberFormat FORMAT = NumberFormat.getInstance(Locale.ENGLISH);

	public StackingEnchantProgressTooltip(int priority) {
		super(priority);
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		NbtCompound customData = ItemUtils.getCustomData(stack);

		if (customData.contains("enchantments", NbtElement.COMPOUND_TYPE)) {
			NbtCompound enchantments = customData.getCompound("enchantments");
			StackingEnchantInfo stackingEnchantInfo = null;
			int stackingEnchantLevel = 0;

			for (String enchantment : enchantments.getKeys()) {
				if (STACKING_ENCHANT_IDS.contains(enchantment)) {
					stackingEnchantInfo = switch (enchantment) {
						case "expertise" -> EXPERTISE_INFO;
						case "compact" -> COMPACT_INFO;
						case "cultivating" -> CULTIVATING_INFO;
						case "champion" -> CHAMPION_INFO;
						case "hecatomb" -> HECATOMB_INFO;
						case "toxophilite" -> TOXOPHILITE_INFO;

						default -> throw new IllegalStateException("Unexpected stacking enchant: " + enchantment);
					};
					stackingEnchantLevel = enchantments.getInt(enchantment);

					break;
				}
			}

			if (stackingEnchantInfo != null && stackingEnchantLevel > 0 && stackingEnchantLevel < 10) {
				int progress = customData.getInt(stackingEnchantInfo.field());
				int needed = stackingEnchantInfo.ladder()[stackingEnchantLevel];
				Text text = Text.empty()
						.append(Text.literal(stackingEnchantInfo.name() + " ").formatted(Formatting.GRAY))
						.append(Text.translatable("enchantment.level." + (stackingEnchantLevel + 1)).formatted(Formatting.GRAY))
						.append(Text.literal(": ").formatted(Formatting.GRAY))
						.append(Text.literal(FORMAT.format(progress)).formatted(Formatting.RED))
						.append(Text.literal("/").formatted(Formatting.GRAY))
						.append(Text.literal(FORMAT.format(needed)).formatted(Formatting.RED))
						.append(Text.literal(" " + stackingEnchantInfo.unit()).formatted(Formatting.GRAY));

				lines.add(text);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemTooltip.enableStackingEnchantProgress;
	}

	private record StackingEnchantInfo(String name, String field, String unit, int... ladder) {
		StackingEnchantInfo {
			if (ladder.length != 10) throw new IllegalStateException("Ladder must have 10 entries but had only " + ladder.length + "!");
		}
	}
}
