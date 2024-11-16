package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class EnchantmentLevelAdder extends SimpleSlotTextAdder {
	private static final Object2ObjectMap<String, String> ID_ABBREVIATION_MAP = new Object2ObjectOpenHashMap<>();

	static {
		// Normal enchants (A - Z)
		ID_ABBREVIATION_MAP.put("angler", "AN");
		ID_ABBREVIATION_MAP.put("aqua_affinity", "AA");
		ID_ABBREVIATION_MAP.put("aiming", "DT"); // Dragon tracer

		ID_ABBREVIATION_MAP.put("bane_of_arthropods", "BA");
		ID_ABBREVIATION_MAP.put("big_brain", "BB");
		ID_ABBREVIATION_MAP.put("blast_protection", "BP");
		ID_ABBREVIATION_MAP.put("blessing", "BL");

		ID_ABBREVIATION_MAP.put("caster", "CA");
		ID_ABBREVIATION_MAP.put("cayenne", "CY");
		ID_ABBREVIATION_MAP.put("champion", "CH");
		ID_ABBREVIATION_MAP.put("chance", "CN");
		ID_ABBREVIATION_MAP.put("charm", "CM");
		ID_ABBREVIATION_MAP.put("cleave", "CL");
		ID_ABBREVIATION_MAP.put("compact", "CO");
		ID_ABBREVIATION_MAP.put("corruption", "CP");
		ID_ABBREVIATION_MAP.put("counter_strike", "CS");
		ID_ABBREVIATION_MAP.put("critical", "CR");
		ID_ABBREVIATION_MAP.put("cubism", "CU");

		ID_ABBREVIATION_MAP.put("cultivating", "CT");
		ID_ABBREVIATION_MAP.put("dedication", "DD");
		ID_ABBREVIATION_MAP.put("delicate", "DE");

		ID_ABBREVIATION_MAP.put("depth_strider", "DS");
		ID_ABBREVIATION_MAP.put("divine_gift", "DG");
		ID_ABBREVIATION_MAP.put("dragon_hunter", "DH");

		ID_ABBREVIATION_MAP.put("efficiency", "EF");
		ID_ABBREVIATION_MAP.put("ender_slayer", "ES");
		ID_ABBREVIATION_MAP.put("execute", "EX");
		ID_ABBREVIATION_MAP.put("experience", "XP");
		ID_ABBREVIATION_MAP.put("expertise", "EP");

		ID_ABBREVIATION_MAP.put("feather_falling", "FF");
		ID_ABBREVIATION_MAP.put("ferocious_mana", "FM");
		ID_ABBREVIATION_MAP.put("fire_aspect", "FA");
		ID_ABBREVIATION_MAP.put("fire_protection", "FP");
		ID_ABBREVIATION_MAP.put("first_strike", "FS");
		ID_ABBREVIATION_MAP.put("flame", "FL");
		ID_ABBREVIATION_MAP.put("fortune", "FO");
		ID_ABBREVIATION_MAP.put("frail", "FR");
		ID_ABBREVIATION_MAP.put("frost_walker", "FW");

		ID_ABBREVIATION_MAP.put("giant_killer", "GK");
		ID_ABBREVIATION_MAP.put("great_spook", "GS");
		ID_ABBREVIATION_MAP.put("green_thumb", "GT");
		ID_ABBREVIATION_MAP.put("growth", "GR");

		ID_ABBREVIATION_MAP.put("hardened_mana", "HM");
		ID_ABBREVIATION_MAP.put("harvesting", "HV");
		ID_ABBREVIATION_MAP.put("hecatomb", "HE");

		ID_ABBREVIATION_MAP.put("ice_cold", "IC");
		ID_ABBREVIATION_MAP.put("impaling", "IM");
		ID_ABBREVIATION_MAP.put("infinite_quiver", "IQ");

		ID_ABBREVIATION_MAP.put("knockback", "KB");

		ID_ABBREVIATION_MAP.put("lapidary", "LP");
		ID_ABBREVIATION_MAP.put("lethality", "LE");
		ID_ABBREVIATION_MAP.put("life_steal", "LS");
		ID_ABBREVIATION_MAP.put("looting", "LO");
		ID_ABBREVIATION_MAP.put("luck", "LU");
		ID_ABBREVIATION_MAP.put("luck_of_the_sea", "LA");
		ID_ABBREVIATION_MAP.put("lure", "LR");

		ID_ABBREVIATION_MAP.put("magnet", "MG");
		ID_ABBREVIATION_MAP.put("mana_steal", "MS");
		ID_ABBREVIATION_MAP.put("mana_vampire", "MV");

		ID_ABBREVIATION_MAP.put("overload", "OV");

		ID_ABBREVIATION_MAP.put("paleontologist", "PA");
		ID_ABBREVIATION_MAP.put("pesterminator", "PS");
		ID_ABBREVIATION_MAP.put("piercing", "PI");
		ID_ABBREVIATION_MAP.put("piscary", "PC");
		ID_ABBREVIATION_MAP.put("power", "PW");
		ID_ABBREVIATION_MAP.put("pristine", "PM"); // Prismatic
		ID_ABBREVIATION_MAP.put("projectile_protection", "PP");
		ID_ABBREVIATION_MAP.put("prosecute", "PO");
		ID_ABBREVIATION_MAP.put("prosperity", "PE");
		ID_ABBREVIATION_MAP.put("protection", "PR");
		ID_ABBREVIATION_MAP.put("punch", "PU");

		ID_ABBREVIATION_MAP.put("quantum", "QU");

		ID_ABBREVIATION_MAP.put("rainbow", "RA");
		ID_ABBREVIATION_MAP.put("reflection", "RF");
		ID_ABBREVIATION_MAP.put("rejuvenate", "RJ");
		ID_ABBREVIATION_MAP.put("replenish", "RP");
		ID_ABBREVIATION_MAP.put("respiration", "RE");
		ID_ABBREVIATION_MAP.put("respite", "RS");

		ID_ABBREVIATION_MAP.put("scavenger", "SC");
		ID_ABBREVIATION_MAP.put("sharpness", "SH");
		ID_ABBREVIATION_MAP.put("silk_touch", "ST");
		ID_ABBREVIATION_MAP.put("smarty_pants", "SP");
		ID_ABBREVIATION_MAP.put("smelting_touch", "SE");
		ID_ABBREVIATION_MAP.put("smite", "SI");
		ID_ABBREVIATION_MAP.put("smoldering", "SL");
		ID_ABBREVIATION_MAP.put("snipe", "SN");
		ID_ABBREVIATION_MAP.put("spiked_hook", "SK");
		ID_ABBREVIATION_MAP.put("strong_mana", "SM");
		ID_ABBREVIATION_MAP.put("sugar_rush", "SR");
		ID_ABBREVIATION_MAP.put("sunder", "SU");
		ID_ABBREVIATION_MAP.put("syphon", "SY");

		ID_ABBREVIATION_MAP.put("tabasco", "TA");
		ID_ABBREVIATION_MAP.put("thorns", "TN");
		ID_ABBREVIATION_MAP.put("thunderbolt", "TB");
		ID_ABBREVIATION_MAP.put("thunderlord", "TL");
		ID_ABBREVIATION_MAP.put("titan_killer", "TK");
		ID_ABBREVIATION_MAP.put("toxophilite", "TX");
		ID_ABBREVIATION_MAP.put("transylvanian", "TY");
		ID_ABBREVIATION_MAP.put("triple_strike", "TS");
		ID_ABBREVIATION_MAP.put("true_protection", "TP");

		// Turbo books
		ID_ABBREVIATION_MAP.put("turbo_cacti", "TI");
		ID_ABBREVIATION_MAP.put("turbo_cane", "TC");
		ID_ABBREVIATION_MAP.put("turbo_carrot", "TR");
		ID_ABBREVIATION_MAP.put("turbo_coco", "TO");
		ID_ABBREVIATION_MAP.put("turbo_melon", "TE");
		ID_ABBREVIATION_MAP.put("turbo_mushrooms", "TM");
		ID_ABBREVIATION_MAP.put("turbo_potato", "TT");
		ID_ABBREVIATION_MAP.put("turbo_pumpkin", "TU");
		ID_ABBREVIATION_MAP.put("turbo_warts", "TW");
		ID_ABBREVIATION_MAP.put("turbo_wheat", "TH");

		ID_ABBREVIATION_MAP.put("vampirism", "VP");
		ID_ABBREVIATION_MAP.put("venomous", "VE");
		ID_ABBREVIATION_MAP.put("vicious", "VI");

		// check if all abbreviations are unique, if not report duplicates
		if (Debug.debugEnabled() && ID_ABBREVIATION_MAP.size() != (int) ID_ABBREVIATION_MAP.values().stream().distinct().count()) {
			Map<String, String> otherDirection = new Object2ObjectOpenHashMap<>();
			for (Map.Entry<String, String> entry : ID_ABBREVIATION_MAP.entrySet()) {
				final String put = otherDirection.put(entry.getValue(), entry.getKey());
				if (put != null) {
					throw new IllegalArgumentException("Duplicate key [%s, %s] for value %s".formatted(put, entry.getKey(), entry.getValue()));
				}
			}
		}

		// Ultimate enchants - Added after check since some of these are duplicated, tho due to color difference this is fine
		ID_ABBREVIATION_MAP.put("ultimate_bank", "B");
		ID_ABBREVIATION_MAP.put("ultimate_bobbin_time", "BT");
		ID_ABBREVIATION_MAP.put("ultimate_chimera", "CH");
		ID_ABBREVIATION_MAP.put("ultimate_combo", "CO");
		ID_ABBREVIATION_MAP.put("ultimate_reiterate", "D"); // Duplex
		ID_ABBREVIATION_MAP.put("ultimate_fatal_tempo", "FT");
		ID_ABBREVIATION_MAP.put("ultimate_flash", "F");
		ID_ABBREVIATION_MAP.put("ultimate_flowstate", "FL");
		ID_ABBREVIATION_MAP.put("ultimate_habanero_tactics", "HT");
		ID_ABBREVIATION_MAP.put("ultimate_inferno", "I");
		ID_ABBREVIATION_MAP.put("ultimate_last_stand", "LS");
		ID_ABBREVIATION_MAP.put("ultimate_legion", "L");
		ID_ABBREVIATION_MAP.put("ultimate_no_pain_no_gain", "NP");
		ID_ABBREVIATION_MAP.put("ultimate_one_for_all", "O");
		ID_ABBREVIATION_MAP.put("ultimate_refrigerate", "RF");
		ID_ABBREVIATION_MAP.put("ultimate_rend", "R");
		ID_ABBREVIATION_MAP.put("ultimate_soul_eater", "SE");
		ID_ABBREVIATION_MAP.put("ultimate_swarm", "SW");
		ID_ABBREVIATION_MAP.put("the_one", "TO");
		ID_ABBREVIATION_MAP.put("ultimate_jerry", "UJ");
		ID_ABBREVIATION_MAP.put("ultimate_wise", "UW");
		ID_ABBREVIATION_MAP.put("ultimate_wisdom", "W");
	}

	public EnchantmentLevelAdder() {
		super();
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (!stack.isOf(Items.ENCHANTED_BOOK)) return List.of();
		String name = stack.getName().getString();
		if (name.equals("Enchanted Book")) {
			NbtCompound nbt = ItemUtils.getCustomData(stack);
			if (nbt.isEmpty() || !nbt.contains("enchantments", NbtElement.COMPOUND_TYPE)) return List.of();
			NbtCompound enchantments = nbt.getCompound("enchantments");
			if (enchantments.getSize() != 1) return List.of(); //Only makes sense to display the level when there's one enchant.
			final String enchantmentId = enchantments.getKeys().iterator().next();
			int level = enchantments.getInt(enchantmentId);

			final SlotText enchantmentLevel = SlotText.bottomLeft(Text.literal(String.valueOf(level)).withColor(0xFFDDC1));

			if (ID_ABBREVIATION_MAP.containsKey(enchantmentId)) {
				return List.of(
						SlotText.topRight(getAbbreviation(enchantmentId)),
						enchantmentLevel
				);
			}
			return List.of(enchantmentLevel);
		} else { //In bazaar, the books have the enchantment level in the name
			int level = getEnchantLevelFromString(name);
			if (level == 0) return List.of();
			return SlotText.bottomLeftList(Text.literal(String.valueOf(level)).withColor(0xFFDDC1));
		}
	}

	private Text getAbbreviation(String enchantmentId) {
		if (!ID_ABBREVIATION_MAP.containsKey(enchantmentId)) {
			return Text.empty();
		}

		final int color;
		if (enchantmentId.startsWith("ultimate_") || enchantmentId.equalsIgnoreCase("the_one")) {
			color = Formatting.LIGHT_PURPLE.getColorValue();
		} else {
			color = Formatting.BLUE.getColorValue();
		}

		return Text.literal(ID_ABBREVIATION_MAP.get(enchantmentId)).withColor(color);
	}

	private static int getEnchantLevelFromString(String str) {
		String romanNumeral = str.substring(str.lastIndexOf(' ') + 1); //+1 because we don't need the space itself
		return RomanNumerals.romanToDecimal(romanNumeral); //Temporary line. The method will be moved out later.
	}
}
