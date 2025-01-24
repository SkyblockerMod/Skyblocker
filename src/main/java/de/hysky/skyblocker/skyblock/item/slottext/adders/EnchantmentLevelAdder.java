package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
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
import java.util.Optional;

public class EnchantmentLevelAdder extends SimpleSlotTextAdder {
	private static final Object2ObjectMap<String, String> ENCHANTMENT_ABBREVIATION_MAP = new Object2ObjectOpenHashMap<>();
	private static final Map<String, String> ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP = new Object2ObjectOpenHashMap<>();

	static {
		// Normal enchants (A - Z)
		ENCHANTMENT_ABBREVIATION_MAP.put("angler", "AN");
		ENCHANTMENT_ABBREVIATION_MAP.put("aqua_affinity", "AA");
		ENCHANTMENT_ABBREVIATION_MAP.put("aiming", "DT"); // Dragon tracer

		ENCHANTMENT_ABBREVIATION_MAP.put("bane_of_arthropods", "BA");
		ENCHANTMENT_ABBREVIATION_MAP.put("big_brain", "BB");
		ENCHANTMENT_ABBREVIATION_MAP.put("blast_protection", "BP");
		ENCHANTMENT_ABBREVIATION_MAP.put("blessing", "BL");

		ENCHANTMENT_ABBREVIATION_MAP.put("caster", "CA");
		ENCHANTMENT_ABBREVIATION_MAP.put("cayenne", "CY");
		ENCHANTMENT_ABBREVIATION_MAP.put("champion", "CH");
		ENCHANTMENT_ABBREVIATION_MAP.put("chance", "CN");
		ENCHANTMENT_ABBREVIATION_MAP.put("charm", "CM");
		ENCHANTMENT_ABBREVIATION_MAP.put("cleave", "CL");
		ENCHANTMENT_ABBREVIATION_MAP.put("compact", "CO");
		ENCHANTMENT_ABBREVIATION_MAP.put("corruption", "CP");
		ENCHANTMENT_ABBREVIATION_MAP.put("counter_strike", "CS");
		ENCHANTMENT_ABBREVIATION_MAP.put("critical", "CR");
		ENCHANTMENT_ABBREVIATION_MAP.put("cubism", "CU");

		ENCHANTMENT_ABBREVIATION_MAP.put("cultivating", "CT");
		ENCHANTMENT_ABBREVIATION_MAP.put("dedication", "DD");
		ENCHANTMENT_ABBREVIATION_MAP.put("delicate", "DE");

		ENCHANTMENT_ABBREVIATION_MAP.put("depth_strider", "DS");
		ENCHANTMENT_ABBREVIATION_MAP.put("divine_gift", "DG");
		ENCHANTMENT_ABBREVIATION_MAP.put("dragon_hunter", "DH");

		ENCHANTMENT_ABBREVIATION_MAP.put("efficiency", "EF");
		ENCHANTMENT_ABBREVIATION_MAP.put("ender_slayer", "ES");
		ENCHANTMENT_ABBREVIATION_MAP.put("execute", "EX");
		ENCHANTMENT_ABBREVIATION_MAP.put("experience", "XP");
		ENCHANTMENT_ABBREVIATION_MAP.put("expertise", "EP");

		ENCHANTMENT_ABBREVIATION_MAP.put("feather_falling", "FF");
		ENCHANTMENT_ABBREVIATION_MAP.put("ferocious_mana", "FM");
		ENCHANTMENT_ABBREVIATION_MAP.put("fire_aspect", "FA");
		ENCHANTMENT_ABBREVIATION_MAP.put("fire_protection", "FP");
		ENCHANTMENT_ABBREVIATION_MAP.put("first_strike", "FS");
		ENCHANTMENT_ABBREVIATION_MAP.put("flame", "FL");
		ENCHANTMENT_ABBREVIATION_MAP.put("fortune", "FO");
		ENCHANTMENT_ABBREVIATION_MAP.put("frail", "FR");
		ENCHANTMENT_ABBREVIATION_MAP.put("frost_walker", "FW");

		ENCHANTMENT_ABBREVIATION_MAP.put("giant_killer", "GK");
		ENCHANTMENT_ABBREVIATION_MAP.put("great_spook", "GS");
		ENCHANTMENT_ABBREVIATION_MAP.put("green_thumb", "GT");
		ENCHANTMENT_ABBREVIATION_MAP.put("growth", "GR");

		ENCHANTMENT_ABBREVIATION_MAP.put("hardened_mana", "HM");
		ENCHANTMENT_ABBREVIATION_MAP.put("harvesting", "HV");
		ENCHANTMENT_ABBREVIATION_MAP.put("hecatomb", "HE");

		ENCHANTMENT_ABBREVIATION_MAP.put("ice_cold", "IC");
		ENCHANTMENT_ABBREVIATION_MAP.put("impaling", "IM");
		ENCHANTMENT_ABBREVIATION_MAP.put("infinite_quiver", "IQ");

		ENCHANTMENT_ABBREVIATION_MAP.put("knockback", "KB");

		ENCHANTMENT_ABBREVIATION_MAP.put("lapidary", "LP");
		ENCHANTMENT_ABBREVIATION_MAP.put("lethality", "LE");
		ENCHANTMENT_ABBREVIATION_MAP.put("life_steal", "LS");
		ENCHANTMENT_ABBREVIATION_MAP.put("looting", "LO");
		ENCHANTMENT_ABBREVIATION_MAP.put("luck", "LU");
		ENCHANTMENT_ABBREVIATION_MAP.put("luck_of_the_sea", "LA");
		ENCHANTMENT_ABBREVIATION_MAP.put("lure", "LR");

		ENCHANTMENT_ABBREVIATION_MAP.put("magnet", "MG");
		ENCHANTMENT_ABBREVIATION_MAP.put("mana_steal", "MS");
		ENCHANTMENT_ABBREVIATION_MAP.put("mana_vampire", "MV");

		ENCHANTMENT_ABBREVIATION_MAP.put("overload", "OV");

		ENCHANTMENT_ABBREVIATION_MAP.put("paleontologist", "PA");
		ENCHANTMENT_ABBREVIATION_MAP.put("pesterminator", "PS");
		ENCHANTMENT_ABBREVIATION_MAP.put("piercing", "PI");
		ENCHANTMENT_ABBREVIATION_MAP.put("piscary", "PC");
		ENCHANTMENT_ABBREVIATION_MAP.put("power", "PW");
		ENCHANTMENT_ABBREVIATION_MAP.put("pristine", "PM"); // Prismatic
		ENCHANTMENT_ABBREVIATION_MAP.put("projectile_protection", "PP");
		ENCHANTMENT_ABBREVIATION_MAP.put("prosecute", "PO");
		ENCHANTMENT_ABBREVIATION_MAP.put("prosperity", "PE");
		ENCHANTMENT_ABBREVIATION_MAP.put("protection", "PR");
		ENCHANTMENT_ABBREVIATION_MAP.put("punch", "PU");

		ENCHANTMENT_ABBREVIATION_MAP.put("quantum", "QU");

		ENCHANTMENT_ABBREVIATION_MAP.put("rainbow", "RA");
		ENCHANTMENT_ABBREVIATION_MAP.put("reflection", "RF");
		ENCHANTMENT_ABBREVIATION_MAP.put("rejuvenate", "RJ");
		ENCHANTMENT_ABBREVIATION_MAP.put("replenish", "RP");
		ENCHANTMENT_ABBREVIATION_MAP.put("respiration", "RE");
		ENCHANTMENT_ABBREVIATION_MAP.put("respite", "RS");

		ENCHANTMENT_ABBREVIATION_MAP.put("scavenger", "SC");
		ENCHANTMENT_ABBREVIATION_MAP.put("sharpness", "SH");
		ENCHANTMENT_ABBREVIATION_MAP.put("silk_touch", "ST");
		ENCHANTMENT_ABBREVIATION_MAP.put("smarty_pants", "SP");
		ENCHANTMENT_ABBREVIATION_MAP.put("smelting_touch", "SE");
		ENCHANTMENT_ABBREVIATION_MAP.put("smite", "SI");
		ENCHANTMENT_ABBREVIATION_MAP.put("smoldering", "SL");
		ENCHANTMENT_ABBREVIATION_MAP.put("snipe", "SN");
		ENCHANTMENT_ABBREVIATION_MAP.put("spiked_hook", "SK");
		ENCHANTMENT_ABBREVIATION_MAP.put("strong_mana", "SM");
		ENCHANTMENT_ABBREVIATION_MAP.put("sugar_rush", "SR");
		ENCHANTMENT_ABBREVIATION_MAP.put("sunder", "SU");
		ENCHANTMENT_ABBREVIATION_MAP.put("syphon", "SY");

		ENCHANTMENT_ABBREVIATION_MAP.put("tabasco", "TA");
		ENCHANTMENT_ABBREVIATION_MAP.put("thorns", "TN");
		ENCHANTMENT_ABBREVIATION_MAP.put("thunderbolt", "TB");
		ENCHANTMENT_ABBREVIATION_MAP.put("thunderlord", "TL");
		ENCHANTMENT_ABBREVIATION_MAP.put("titan_killer", "TK");
		ENCHANTMENT_ABBREVIATION_MAP.put("toxophilite", "TX");
		ENCHANTMENT_ABBREVIATION_MAP.put("transylvanian", "TY");
		ENCHANTMENT_ABBREVIATION_MAP.put("triple_strike", "TS");
		ENCHANTMENT_ABBREVIATION_MAP.put("true_protection", "TP");

		// Turbo books
		ENCHANTMENT_ABBREVIATION_MAP.put("turbo_cacti", "TI");
		ENCHANTMENT_ABBREVIATION_MAP.put("turbo_cane", "TC");
		ENCHANTMENT_ABBREVIATION_MAP.put("turbo_carrot", "TR");
		ENCHANTMENT_ABBREVIATION_MAP.put("turbo_coco", "TO");
		ENCHANTMENT_ABBREVIATION_MAP.put("turbo_melon", "TE");
		ENCHANTMENT_ABBREVIATION_MAP.put("turbo_mushrooms", "TM");
		ENCHANTMENT_ABBREVIATION_MAP.put("turbo_potato", "TT");
		ENCHANTMENT_ABBREVIATION_MAP.put("turbo_pumpkin", "TU");
		ENCHANTMENT_ABBREVIATION_MAP.put("turbo_warts", "TW");
		ENCHANTMENT_ABBREVIATION_MAP.put("turbo_wheat", "TH");

		ENCHANTMENT_ABBREVIATION_MAP.put("vampirism", "VP");
		ENCHANTMENT_ABBREVIATION_MAP.put("venomous", "VE");
		ENCHANTMENT_ABBREVIATION_MAP.put("vicious", "VI");

		// check if all abbreviations are unique, if not report duplicates
		if (Debug.debugEnabled() && ENCHANTMENT_ABBREVIATION_MAP.size() != (int) ENCHANTMENT_ABBREVIATION_MAP.values().stream().distinct().count()) {
			Map<String, String> otherDirection = new Object2ObjectOpenHashMap<>();
			for (Map.Entry<String, String> entry : ENCHANTMENT_ABBREVIATION_MAP.entrySet()) {
				final String put = otherDirection.put(entry.getValue(), entry.getKey());
				if (put != null) {
					throw new IllegalArgumentException("Duplicate key [%s, %s] for value %s".formatted(put, entry.getKey(), entry.getValue()));
				}
			}
		}

		// Ultimate enchants - Added after check since some of these are duplicated, tho due to color difference this is fine
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_bank", "B");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_bobbin_time", "BT");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_chimera", "CH");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_combo", "CO");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_reiterate", "D"); // Duplex
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_fatal_tempo", "FT");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_flash", "F");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_flowstate", "FL");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_habanero_tactics", "HT");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_inferno", "I");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_last_stand", "LS");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_legion", "L");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_no_pain_no_gain", "NP");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_one_for_all", "O");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_refrigerate", "RF");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_rend", "R");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_soul_eater", "SE");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_swarm", "SW");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("the_one", "TO");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_jerry", "UJ");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_wise", "UW");
		ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.put("ultimate_wisdom", "W");
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

			return getAbbreviation(enchantmentId)
					.map(text -> List.of(SlotText.topRight(text), enchantmentLevel))
					.orElseGet(() -> List.of(enchantmentLevel));
		} else { //In bazaar, the books have the enchantment level in the name
			int level = getEnchantLevelFromString(name);
			return level != 0 ? SlotText.bottomLeftList(Text.literal(String.valueOf(level)).withColor(0xFFDDC1)) : List.of();
		}
	}

	private Optional<Text> getAbbreviation(String enchantmentId) {
		if (ENCHANTMENT_ABBREVIATION_MAP.containsKey(enchantmentId)) {
			return Optional.of(Text.literal(ENCHANTMENT_ABBREVIATION_MAP.get(enchantmentId)).withColor(Formatting.BLUE.getColorValue()));
		} else if (ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.containsKey(enchantmentId)) {
			return Optional.of(Text.literal(ULTIMATE_ENCHANTMENT_ABBREVIATION_MAP.get(enchantmentId)).withColor(Formatting.LIGHT_PURPLE.getColorValue()));
		}
		return Optional.empty();
	}

	private static int getEnchantLevelFromString(String str) {
		return RomanNumerals.romanToDecimal(str.substring(str.lastIndexOf(' ') + 1)); //+1 because we don't need the space itself
	}
}
