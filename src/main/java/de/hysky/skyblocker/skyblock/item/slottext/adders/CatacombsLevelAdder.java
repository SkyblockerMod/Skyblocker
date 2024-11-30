package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.RomanNumerals;
import de.hysky.skyblocker.utils.container.SlotTextAdder;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//This class is split into 3 inner classes as there are multiple screens for showing catacombs levels, each with different slot ids or different style of showing the level.
//It's still kept in 1 main class for organization purposes.
public class CatacombsLevelAdder {

	private static final SlotTextAdder.ConfigInformation CONFIG_INFORMATION = new SlotTextAdder.ConfigInformation(
			"catacombs_level",
			"skyblocker.config.uiAndVisuals.slotText.catacombsLevel");

	private CatacombsLevelAdder() {
	}

	public static class Dungeoneering extends SimpleSlotTextAdder {
		private static final Pattern LEVEL_PATTERN = Pattern.compile(".*?(?:(?<arabic>\\d+)|(?<roman>\\S+))? ?✯?");
		public Dungeoneering() {
			super("^Dungeoneering", CONFIG_INFORMATION);
		}

		@Override
		public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
			switch (slotId) {
				case 12, 29, 30, 31, 32, 33 -> {
					Matcher matcher = LEVEL_PATTERN.matcher(stack.getName().getString());
					if (!matcher.matches()) return List.of();
					String arabic = matcher.group("arabic");
					String roman = matcher.group("roman");
					if (arabic == null && roman == null) return SlotText.bottomLeftList(Text.literal("0").formatted(Formatting.RED));
					String level;
					if (arabic != null) {
						if (!NumberUtils.isDigits(arabic)) return List.of(); //Sanity check
						level = arabic;
					} else { // roman != null
						if (!RomanNumerals.isValidRomanNumeral(roman)) return List.of(); //Sanity check
						level = String.valueOf(RomanNumerals.romanToDecimal(roman));
					}

					return SlotText.bottomLeftList(Text.literal(level).withColor(0xFFDDC1));
				}
				default -> {
					return List.of();
				}
			}
		}
	}

	public static class DungeonClasses extends SimpleSlotTextAdder {

		public DungeonClasses() {
			super("^Dungeon Classes", CONFIG_INFORMATION); //Applies to both screens as they are same in both the placement and the style of the level text.
		}

		@Override
		public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
			switch (slotId) {
				case 11, 12, 13, 14, 15 -> {
					String level = getBracketedLevelFromName(stack);
					if (!NumberUtils.isDigits(level)) return List.of();
					return SlotText.bottomLeftList(Text.literal(level).withColor(0xFFDDC1));
				}
				default -> {
					return List.of();
				}
			}
		}
	}

	public static class ReadyUp extends SimpleSlotTextAdder {

		public ReadyUp() {
			super("^Ready Up", CONFIG_INFORMATION);
		}

		@Override
		public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
			switch (slotId) {
				case 29, 30, 31, 32, 33 -> {
					String level = getBracketedLevelFromName(stack);
					if (!NumberUtils.isDigits(level)) return List.of();
					return SlotText.bottomLeftList(Text.literal(level).withColor(0xFFDDC1));
				}
				default -> {
					return List.of();
				}
			}
		}
	}

	public static String getBracketedLevelFromName(ItemStack itemStack) {
		String name = itemStack.getName().getString();
		if (!name.startsWith("[Lvl ")) return null;
		int index = name.indexOf(']');
		if (index == -1) return null;
		return name.substring(5, index);
	}
}
