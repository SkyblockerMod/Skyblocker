package de.hysky.skyblocker.skyblock.item.slottext.adders;

import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EnchantmentLevelAdder extends SimpleSlotTextAdder {
	private static final ConfigInformation CONFIG_INFORMATION = new ConfigInformation(
			"enchantment_level",
			"skyblocker.config.uiAndVisuals.slotText.enchantmentLevel");

	@VisibleForTesting
	static final Object2ObjectMap<String, String> ENCHANTMENT_ABBREVIATIONS = new Object2ObjectOpenHashMap<>();
	@VisibleForTesting
	static final Map<String, String> ULTIMATE_ENCHANTMENT_ABBREVIATIONS = new Object2ObjectOpenHashMap<>();

	static {
		// Normal enchants (A - Z)
		ENCHANTMENT_ABBREVIATIONS.put("absorb", "AB");
		ENCHANTMENT_ABBREVIATIONS.put("angler", "AN");
		ENCHANTMENT_ABBREVIATIONS.put("aqua_affinity", "AA");
		ENCHANTMENT_ABBREVIATIONS.put("arcane", "WS"); // Woodsplitter
		ENCHANTMENT_ABBREVIATIONS.put("aiming", "DT"); // Dragon tracer

		ENCHANTMENT_ABBREVIATIONS.put("bane_of_arthropods", "BOA");
		ENCHANTMENT_ABBREVIATIONS.put("big_brain", "BB");
		ENCHANTMENT_ABBREVIATIONS.put("blast_protection", "BP");
		ENCHANTMENT_ABBREVIATIONS.put("blessing", "BL");

		ENCHANTMENT_ABBREVIATIONS.put("caster", "CAT");
		ENCHANTMENT_ABBREVIATIONS.put("cayenne", "CAY");
		ENCHANTMENT_ABBREVIATIONS.put("champion", "CHM");
		ENCHANTMENT_ABBREVIATIONS.put("chance", "CHN");
		ENCHANTMENT_ABBREVIATIONS.put("charm", "CHR");
		ENCHANTMENT_ABBREVIATIONS.put("cleave", "CL");
		ENCHANTMENT_ABBREVIATIONS.put("compact", "CO");
		ENCHANTMENT_ABBREVIATIONS.put("corruption", "COR");
		ENCHANTMENT_ABBREVIATIONS.put("counter_strike", "CS");
		ENCHANTMENT_ABBREVIATIONS.put("critical", "CR");
		ENCHANTMENT_ABBREVIATIONS.put("cubism", "CB");

		ENCHANTMENT_ABBREVIATIONS.put("cultivating", "CUL");
		ENCHANTMENT_ABBREVIATIONS.put("dedication", "DED");
		ENCHANTMENT_ABBREVIATIONS.put("delicate", "DEL");

		ENCHANTMENT_ABBREVIATIONS.put("depth_strider", "DS");
		ENCHANTMENT_ABBREVIATIONS.put("divine_gift", "DG");
		ENCHANTMENT_ABBREVIATIONS.put("dragon_hunter", "GV"); // Gravity

		ENCHANTMENT_ABBREVIATIONS.put("efficiency", "EF");
		ENCHANTMENT_ABBREVIATIONS.put("ender_slayer", "ES");
		ENCHANTMENT_ABBREVIATIONS.put("execute", "EXC");
		ENCHANTMENT_ABBREVIATIONS.put("experience", "EXP");
		ENCHANTMENT_ABBREVIATIONS.put("expertise", "EPR");

		ENCHANTMENT_ABBREVIATIONS.put("feather_falling", "FF");
		ENCHANTMENT_ABBREVIATIONS.put("ferocious_mana", "FM");
		ENCHANTMENT_ABBREVIATIONS.put("fire_aspect", "FA");
		ENCHANTMENT_ABBREVIATIONS.put("fire_protection", "FPR");
		ENCHANTMENT_ABBREVIATIONS.put("first_strike", "FS");
		ENCHANTMENT_ABBREVIATIONS.put("flame", "FL");
		ENCHANTMENT_ABBREVIATIONS.put("forest_pledge", "FPL");
		ENCHANTMENT_ABBREVIATIONS.put("fortune", "FO");
		ENCHANTMENT_ABBREVIATIONS.put("frail", "FR");

		ENCHANTMENT_ABBREVIATIONS.put("giant_killer", "GK");
		ENCHANTMENT_ABBREVIATIONS.put("great_spook", "GS");
		ENCHANTMENT_ABBREVIATIONS.put("green_thumb", "GT");
		ENCHANTMENT_ABBREVIATIONS.put("growth", "GR");

		ENCHANTMENT_ABBREVIATIONS.put("hardened_mana", "HM");
		ENCHANTMENT_ABBREVIATIONS.put("harvesting", "HV");
		ENCHANTMENT_ABBREVIATIONS.put("hecatomb", "HEC");

		ENCHANTMENT_ABBREVIATIONS.put("ice_cold", "IC");
		ENCHANTMENT_ABBREVIATIONS.put("impaling", "IM");
		ENCHANTMENT_ABBREVIATIONS.put("infinite_quiver", "IQ");

		ENCHANTMENT_ABBREVIATIONS.put("knockback", "KB");

		ENCHANTMENT_ABBREVIATIONS.put("lapidary", "LAP");
		ENCHANTMENT_ABBREVIATIONS.put("lethality", "LE");
		ENCHANTMENT_ABBREVIATIONS.put("life_steal", "LS");
		ENCHANTMENT_ABBREVIATIONS.put("looting", "LO");
		ENCHANTMENT_ABBREVIATIONS.put("luck", "LU");
		ENCHANTMENT_ABBREVIATIONS.put("luck_of_the_sea", "LTS");
		ENCHANTMENT_ABBREVIATIONS.put("lure", "LR");

		ENCHANTMENT_ABBREVIATIONS.put("magnet", "MA");
		ENCHANTMENT_ABBREVIATIONS.put("mana_steal", "MS");
		ENCHANTMENT_ABBREVIATIONS.put("mana_vampire", "MV");

		ENCHANTMENT_ABBREVIATIONS.put("overload", "OV");

		ENCHANTMENT_ABBREVIATIONS.put("paleontologist", "PAL");
		ENCHANTMENT_ABBREVIATIONS.put("pesterminator", "PS");
		ENCHANTMENT_ABBREVIATIONS.put("piercing", "PR");
		ENCHANTMENT_ABBREVIATIONS.put("piscary", "PSC");
		ENCHANTMENT_ABBREVIATIONS.put("power", "POW");
		ENCHANTMENT_ABBREVIATIONS.put("pristine", "PRI"); // Prismatic
		ENCHANTMENT_ABBREVIATIONS.put("projectile_protection", "PP");
		ENCHANTMENT_ABBREVIATIONS.put("PROSECUTE", "PRS");
		ENCHANTMENT_ABBREVIATIONS.put("prosperity", "PSP");
		ENCHANTMENT_ABBREVIATIONS.put("protection", "PRO");
		ENCHANTMENT_ABBREVIATIONS.put("punch", "PU");

		ENCHANTMENT_ABBREVIATIONS.put("quantum", "QUA");
		ENCHANTMENT_ABBREVIATIONS.put("quick_bite", "QB");

		ENCHANTMENT_ABBREVIATIONS.put("rainbow", "RA");
		ENCHANTMENT_ABBREVIATIONS.put("reflection", "REF");
		ENCHANTMENT_ABBREVIATIONS.put("rejuvenate", "RJ");
		ENCHANTMENT_ABBREVIATIONS.put("replenish", "REP");
		ENCHANTMENT_ABBREVIATIONS.put("respiration", "RES");
		ENCHANTMENT_ABBREVIATIONS.put("respite", "RSP");

		ENCHANTMENT_ABBREVIATIONS.put("scavenger", "SCV");
		ENCHANTMENT_ABBREVIATIONS.put("scuba", "SCU");
		ENCHANTMENT_ABBREVIATIONS.put("sharpness", "SH");
		ENCHANTMENT_ABBREVIATIONS.put("silk_touch", "ST");
		ENCHANTMENT_ABBREVIATIONS.put("small_brain", "SB");
		ENCHANTMENT_ABBREVIATIONS.put("smarty_pants", "SP");
		ENCHANTMENT_ABBREVIATIONS.put("smelting_touch", "SMT");
		ENCHANTMENT_ABBREVIATIONS.put("smite", "SMI");
		ENCHANTMENT_ABBREVIATIONS.put("smoldering", "SML");
		ENCHANTMENT_ABBREVIATIONS.put("snipe", "SN");
		ENCHANTMENT_ABBREVIATIONS.put("spiked_hook", "SPH");
		ENCHANTMENT_ABBREVIATIONS.put("stealth", "STE");
		ENCHANTMENT_ABBREVIATIONS.put("strong_mana", "SM");
		ENCHANTMENT_ABBREVIATIONS.put("sugar_rush", "SR");
		ENCHANTMENT_ABBREVIATIONS.put("sunder", "SU");
		ENCHANTMENT_ABBREVIATIONS.put("syphon", "DR"); // Drain

		ENCHANTMENT_ABBREVIATIONS.put("tabasco", "TA");
		ENCHANTMENT_ABBREVIATIONS.put("thorns", "TH");
		ENCHANTMENT_ABBREVIATIONS.put("thunderbolt", "TB");
		ENCHANTMENT_ABBREVIATIONS.put("thunderlord", "TL");
		ENCHANTMENT_ABBREVIATIONS.put("tidal", "TD");
		ENCHANTMENT_ABBREVIATIONS.put("titan_killer", "TK");
		ENCHANTMENT_ABBREVIATIONS.put("toxophilite", "TX");
		ENCHANTMENT_ABBREVIATIONS.put("transylvanian", "TY");
		ENCHANTMENT_ABBREVIATIONS.put("triple_strike", "TS");
		ENCHANTMENT_ABBREVIATIONS.put("true_protection", "TP");

		// Turbo books
		ENCHANTMENT_ABBREVIATIONS.put("turbo_cactus", "TCC");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_cane", "TCN");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_carrot", "TCR");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_coco", "TCO");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_melon", "TME");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_moonflower", "TMO");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_mushrooms", "TMU");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_potato", "TPO");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_pumpkin", "TPU");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_rose", "TRO");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_sunflower", "TSU");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_warts", "TWA");
		ENCHANTMENT_ABBREVIATIONS.put("turbo_wheat", "TWH");

		ENCHANTMENT_ABBREVIATIONS.put("vampirism", "VP");
		ENCHANTMENT_ABBREVIATIONS.put("venomous", "VE");
		ENCHANTMENT_ABBREVIATIONS.put("vicious", "VI");

		// Ultimate enchants - Added after check since some of these are duplicated, tho due to color difference this is fine
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_bank", "B");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_bobbin_time", "BT");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_chimera", "CH");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_combo", "CO");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_reiterate", "D"); // Duplex
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_fatal_tempo", "FT");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_first_impression", "FI");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_flash", "F");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_flowstate", "FL");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_habanero_tactics", "HT");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_inferno", "I");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_last_stand", "LS");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_legion", "L");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_missile", "M");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_no_pain_no_gain", "NP");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_one_for_all", "O");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_refrigerate", "RF");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_rend", "R");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_soul_eater", "SE");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_swarm", "SW");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("the_one", "TO");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_jerry", "UJ");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_wise", "UW");
		ULTIMATE_ENCHANTMENT_ABBREVIATIONS.put("ultimate_wisdom", "W");
	}

	public EnchantmentLevelAdder() {
		super(CONFIG_INFORMATION);
	}

	@Override
	public List<SlotText> getText(@Nullable Slot slot, ItemStack stack, int slotId) {
		if (!stack.isOf(Items.ENCHANTED_BOOK)) return List.of();
		String name = stack.getName().getString();
		if (name.equals("Enchanted Book")) {
			NbtCompound nbt = ItemUtils.getCustomData(stack);
			if (nbt.isEmpty() || !nbt.contains("enchantments")) return List.of();
			NbtCompound enchantments = nbt.getCompoundOrEmpty("enchantments");
			if (enchantments.getSize() != 1) return List.of(); //Only makes sense to display the level when there's one enchant.
			final String enchantmentId = enchantments.getKeys().iterator().next();
			int level = enchantments.getInt(enchantmentId, 0);
			final SlotText enchantmentLevel = SlotText.bottomLeft(Text.literal(String.valueOf(level)).withColor(SlotText.CREAM));

			return getAbbreviation(enchantmentId)
					.map(text -> List.of(SlotText.topRight(text), enchantmentLevel))
					.orElseGet(() -> List.of(enchantmentLevel));
		} else { //In bazaar, the books have the enchantment level in the name
			int level = getEnchantLevelFromString(name);
			return level != 0 ? SlotText.bottomLeftList(Text.literal(String.valueOf(level)).withColor(SlotText.CREAM)) : List.of();
		}
	}

	private Optional<Text> getAbbreviation(String enchantmentId) {
		if (ENCHANTMENT_ABBREVIATIONS.containsKey(enchantmentId)) {
			return Optional.of(Text.literal(ENCHANTMENT_ABBREVIATIONS.get(enchantmentId)).withColor(Formatting.BLUE.getColorValue()));
		} else if (ULTIMATE_ENCHANTMENT_ABBREVIATIONS.containsKey(enchantmentId)) {
			return Optional.of(Text.literal(ULTIMATE_ENCHANTMENT_ABBREVIATIONS.get(enchantmentId)).withColor(Formatting.LIGHT_PURPLE.getColorValue()));
		}
		return Optional.empty();
	}

	private static int getEnchantLevelFromString(String str) {
		return RomanNumerals.romanToDecimal(str.substring(str.lastIndexOf(' ') + 1)); //+1 because we don't need the space itself
	}
}
