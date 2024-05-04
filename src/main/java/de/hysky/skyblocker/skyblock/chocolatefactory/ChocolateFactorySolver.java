package de.hysky.skyblocker.skyblock.chocolatefactory;

import de.hysky.skyblocker.skyblock.experiment.ExperimentSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.*;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Solver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChocolateFactorySolver extends ContainerSolver {
	private static final Pattern CPS_PATTERN = Pattern.compile("\\+([\\d,]+) Chocolate per second");
	private static final Pattern COST_PATTERN = Pattern.compile("Cost ([\\d,]+) Chocolate");

	public ChocolateFactorySolver() {
		super("^Chocolate Factory$");
	}

	@Override
	protected boolean isEnabled() {
		return true; //Todo: add a config option and check if it's enabled from there
	}

	@Override
	protected List<ColorHighlight> getColors(String[] groups, Map<Integer, ItemStack> slots) {
		Int2DoubleMap cpsIncreaseFactors = new Int2DoubleLinkedOpenHashMap(5); //There are only 5 rabbits on the screen.
		for (Map.Entry<Integer, ItemStack> entry : slots.entrySet()) {
			ItemStack item = entry.getValue();
			if (item.getItem() != Items.PLAYER_HEAD || !item.hasNbt() || item.isEmpty()) continue;

			String lore = getLore(item);
			if (lore.isBlank()) continue;

			OptionalDouble cpsIncreaseFactor = getCPSIncreaseFactor(lore);
			if (cpsIncreaseFactor.isEmpty()) continue; //Something went wrong, skip this item
			cpsIncreaseFactors.put(entry.getKey().intValue(), cpsIncreaseFactor.getAsDouble());
		}
		Optional<Int2DoubleMap.Entry> bestSlot = cpsIncreaseFactors.int2DoubleEntrySet().stream().max(Map.Entry.comparingByValue());
		if (bestSlot.isEmpty()) return List.of(); //No valid slots found, somehow. This means something went wrong, despite all the checks thus far.
		return List.of(ColorHighlight.green(bestSlot.get().getIntKey()));
	}

	private String getLore(ItemStack item) {
		NbtCompound display = item.getSubNbt(ItemStack.DISPLAY_KEY);
		if (display == null || display.isEmpty() || !display.contains(ItemStack.LORE_KEY, NbtElement.LIST_TYPE)) return "";
		NbtList lore = display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
		return lore.stream()
		           .map(NbtElement::asString)
		           .map(Text.Serialization::fromJson) //Serialize the nbt string into a text to get the content string
		           .filter(Objects::nonNull)
		           .map(Text::getString)
		           .collect(Collectors.joining(" ")); //Join all lore lines into one string for ease of regexing
					//The space is so that the regex pattern still matches even if the word is split into 2 lines,
					//as normally the line end and line start contain no spaces and would not match the pattern when concatenated
	}

	/**
	 * The "CPS increase factor" here is the increase in CPS per chocolate spent.
	 * The highest value among the choices is the best one to pick.
	 *
	 * @param lore The lore of the item
	 * @return The CPS increase factor of the item, or an empty optional if it couldn't be found
	 */
	private OptionalDouble getCPSIncreaseFactor(String lore) {
		Matcher cpsMatcher = CPS_PATTERN.matcher(lore);
		if (!cpsMatcher.find()) return OptionalDouble.empty();
		int currentCps = Integer.parseInt(cpsMatcher.group(1).replace(",", ""));
		if (!cpsMatcher.find()) return OptionalDouble.empty(); //If there is no second match, we can't get the CPS increase
		int nextCps = Integer.parseInt(cpsMatcher.group(1).replace(",", ""));

		Matcher costMatcher = COST_PATTERN.matcher(lore);
		if (!costMatcher.find(cpsMatcher.end())) return OptionalDouble.empty(); //Cost is always at the end of the string, so we can start check from the end of the last match
		int cost = Integer.parseInt(costMatcher.group(1).replace(",", ""));
		return OptionalDouble.of((nextCps - currentCps) / (double) cost);
	}

	//Publicize this method so that the HandledScreenMixin can call it, as the values change on upgrade (when clicked) and there is a need for recalculation
	public void markDirty() {
		super.markHighlightsDirty();
	}
}
