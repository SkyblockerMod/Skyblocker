package de.hysky.skyblocker.skyblock.chocolatefactory;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChocolateFactorySolver extends ContainerSolver {
	private static final Pattern CPS_PATTERN = Pattern.compile("\\+([\\d,]+) Chocolate per second");
	private static final Pattern COST_PATTERN = Pattern.compile("Cost ([\\d,]+) Chocolate");
	private static final Pattern HIRE_PATTERN = Pattern.compile("(HIRE|PROMOTE) ➜ \\[\\d+] \\S+ *");

	public ChocolateFactorySolver() {
		super("^Chocolate Factory$");
	}

	@Override
	protected boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.chocolateFactory.enableChocolateFactoryHelper;
	}

	@Override
	protected void start(GenericContainerScreen screen) {
		markHighlightsDirty(); //Recalculate highlights when the screen is opened, which happens when upgrading rabbits
	}

	@Override
	protected List<ColorHighlight> getColors(String[] groups, Int2ObjectMap<ItemStack> slots) {
		Int2DoubleMap cpsIncreaseFactors = new Int2DoubleLinkedOpenHashMap(5); //There are only 5 rabbits on the screen.
		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {
			if (entry.getIntKey() < 29 || entry.getIntKey() > 33) continue; //Only check the rabbit slots (29,30,31,32,33)
			ItemStack item = entry.getValue();
			if (item.getItem() != Items.PLAYER_HEAD || item.isEmpty()) continue;

			String lore = getConcattedLore(item);
			if (lore.isBlank()) continue;

			OptionalDouble cpsIncreaseFactor = getCPSIncreaseFactor(lore);
			if (cpsIncreaseFactor.isEmpty()) continue; //Something went wrong, skip this item
			cpsIncreaseFactors.put(entry.getIntKey(), cpsIncreaseFactor.getAsDouble());
		}
		Optional<Int2DoubleMap.Entry> bestSlot = cpsIncreaseFactors.int2DoubleEntrySet().stream().max(Map.Entry.comparingByValue());
		if (bestSlot.isEmpty()) return List.of(); //No valid slots found, somehow. This means something went wrong, despite all the checks thus far.
		return List.of(ColorHighlight.green(bestSlot.get().getIntKey()));
	}

	private String getConcattedLore(ItemStack item) {
		return ItemUtils.getLore(item)
		                .stream()
		                .map(Text::getString)
		                .collect(Collectors.joining(" ")); //Join all lore lines into one string for ease of regexing
//					     The space is so that the regex pattern still matches even if the word is split into 2 lines,
//					     as normally the line end and the line start contain no spaces and would not match the pattern when concatenated
	}

	/**
	 * The "CPS increase factor" here is the increase in CPS per chocolate spent.
	 * The highest value among the choices is the best one to pick.
	 *
	 * @param lore The lore of the item
	 * @return The CPS increase factor of the item, or an empty optional if it couldn't be found
	 */
	private OptionalDouble getCPSIncreaseFactor(String lore) {
		Matcher hireMatcher = HIRE_PATTERN.matcher(lore);
		if (!hireMatcher.find()) return OptionalDouble.empty(); //Not a hireable/promotable rabbit. Could be a locked or maxed rabbit.

		switch (hireMatcher.group(1)) {
			case "HIRE" -> {
				Matcher cpsMatcher = CPS_PATTERN.matcher(lore);
				OptionalInt cps = getValueFromMatcher(cpsMatcher, hireMatcher.end()); //Cps line is right after the hire line
				if (cps.isEmpty()) return OptionalDouble.empty();

				Matcher costMatcher = COST_PATTERN.matcher(lore);
				OptionalInt cost = getValueFromMatcher(costMatcher, cpsMatcher.end()); //Cost comes after the cps line
				if (cost.isEmpty()) return OptionalDouble.empty();
				return OptionalDouble.of(cps.getAsInt() / (double) cost.getAsInt());
			}
			case "PROMOTE" -> {
				Matcher cpsMatcher = CPS_PATTERN.matcher(lore);
				OptionalInt currentCps = getValueFromMatcher(cpsMatcher); //Current cps is before the hire line
				if (currentCps.isEmpty()) return OptionalDouble.empty();
				OptionalInt nextCps = getValueFromMatcher(cpsMatcher, hireMatcher.end()); //Next cps is right after the hire line
				if (nextCps.isEmpty()) return OptionalDouble.empty();

				Matcher costMatcher = COST_PATTERN.matcher(lore);
				OptionalInt cost = getValueFromMatcher(costMatcher, cpsMatcher.end()); //Cost comes after the cps line
				if (cost.isEmpty()) return OptionalDouble.empty();
				return OptionalDouble.of((nextCps.getAsInt() - currentCps.getAsInt()) / (double) cost.getAsInt());
			}
			default -> { return OptionalDouble.empty(); }
		}
	}

	private OptionalInt getValueFromMatcher(Matcher matcher) {
		return getValueFromMatcher(matcher, matcher.hasMatch() ? matcher.end() : 0);
	}

	private OptionalInt getValueFromMatcher(Matcher matcher, int startingIndex) {
		if (!matcher.find(startingIndex)) return OptionalInt.empty();
		return OptionalInt.of(Integer.parseInt(matcher.group(1).replace(",", "")));
	}
}
