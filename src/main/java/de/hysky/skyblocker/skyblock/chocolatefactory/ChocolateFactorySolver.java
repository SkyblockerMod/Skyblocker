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

public class ChocolateFactorySolver extends ContainerSolver {
	private static final Pattern CPS_PATTERN = Pattern.compile("([\\d,.]+) Chocolate per second");
	private static final Pattern CPS_INCREASE_PATTERN = Pattern.compile("\\+([\\d,]+) Chocolate per second");
	private static final Pattern COST_PATTERN = Pattern.compile("Cost ([\\d,]+) Chocolate");
	private static final Pattern HIRE_PATTERN = Pattern.compile("(HIRE|PROMOTE) ➜ \\[\\d+] \\S+ *");
	private static final Pattern TOTAL_MULTIPLIER_PATTERN = Pattern.compile("Total Multiplier: ([\\d.]+)x");
	private static final Pattern MULTIPLIER_INCREASE_PATTERN = Pattern.compile("\\+([\\d.]+)x Chocolate per second");

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
		final Int2DoubleMap cpsIncreaseFactors = new Int2DoubleLinkedOpenHashMap(6); //There are only 5 rabbits on the screen + 1 for the coach
		for (int i = 29; i <= 33; i++) { // The 5 rabbits slots are in 29, 30, 31, 32 and 33.
			ItemStack item = slots.get(i);
			if (item.getItem() != Items.PLAYER_HEAD || item.isEmpty()) continue;

			String lore = getConcattedLore(item);
			if (lore.isBlank()) continue;

			OptionalDouble cpsIncreaseFactor = getRabbitCPSIncreaseFactor(lore); // The 5 usual rabbits
			if (cpsIncreaseFactor.isEmpty()) continue; //Something went wrong, skip this item
			cpsIncreaseFactors.put(i, cpsIncreaseFactor.getAsDouble());
		}
		OptionalDouble coachCpsIncreaseFactor = getCoachCPSIncreaseFactor(slots.get(45), slots.get(42)); // The coach

		if (!coachCpsIncreaseFactor.isEmpty()) cpsIncreaseFactors.put(42, coachCpsIncreaseFactor.getAsDouble());

		Optional<Int2DoubleMap.Entry> bestSlot = cpsIncreaseFactors.int2DoubleEntrySet().stream().max(Map.Entry.comparingByValue());
		if (bestSlot.isEmpty()) return List.of(); //No valid slots found, somehow. This means something went wrong, despite all the checks thus far.
		return List.of(ColorHighlight.green(bestSlot.get().getIntKey()));
	}

	/**
	 * Utility method.
	 */
	private String getConcattedLore(ItemStack item) {
		return concatenateLore(ItemUtils.getLore(item));
	}

	/**
	 * Concatenates the lore of an item into one string.
	 * This is useful in case some pattern we're looking for is split into multiple lines, which would make it harder to regex.
	 */
	private String concatenateLore(List<Text> lore) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < lore.size(); i++) {
			stringBuilder.append(lore.get(i).getString());
			if (i != lore.size() - 1) stringBuilder.append(" ");
		}
		return stringBuilder.toString();
	}

	private OptionalDouble getCoachCPSIncreaseFactor(ItemStack cpsItem, ItemStack coachItem) {
		String cpsItemLore = getConcattedLore(cpsItem);

		Matcher cpsMatcher = CPS_PATTERN.matcher(cpsItemLore);
		OptionalDouble currentCps = getDoubleFromMatcher(cpsMatcher);
		if (currentCps.isEmpty()) return OptionalDouble.empty();

		Matcher multiplierMatcher = TOTAL_MULTIPLIER_PATTERN.matcher(cpsItemLore);
		OptionalDouble totalMultiplier = getDoubleFromMatcher(multiplierMatcher, cpsMatcher.end());
		if (totalMultiplier.isEmpty()) return OptionalDouble.empty();

		String coachLore = getConcattedLore(coachItem);

		Matcher multiplierIncreaseMatcher = MULTIPLIER_INCREASE_PATTERN.matcher(coachLore);
		OptionalDouble currentCpsMultiplier = getDoubleFromMatcher(multiplierIncreaseMatcher);
		if (currentCpsMultiplier.isEmpty()) return OptionalDouble.empty();
		OptionalDouble nextCpsMultiplier = getDoubleFromMatcher(multiplierIncreaseMatcher);
		if (nextCpsMultiplier.isEmpty()) { //This means that the coach isn't hired yet.
			nextCpsMultiplier = currentCpsMultiplier; //So the first instance of the multiplier is actually the amount we'll get upon upgrading.
			currentCpsMultiplier = OptionalDouble.of(0.0); //And so, we can re-assign values to the variables to make the calculation more readable.
		}

		return OptionalDouble.of((currentCps.getAsDouble()/totalMultiplier.getAsDouble()) * (nextCpsMultiplier.getAsDouble() - currentCpsMultiplier.getAsDouble()));
	}

	/**
	 * The "CPS increase factor" here is the increase in CPS per chocolate spent.
	 * The highest value among the choices is the best one to pick.
	 *
	 * @param lore The lore of the item
	 * @return The CPS increase factor of the item, or an empty optional if it couldn't be found
	 */
	private OptionalDouble getRabbitCPSIncreaseFactor(String lore) {
		Matcher hireMatcher = HIRE_PATTERN.matcher(lore);
		if (!hireMatcher.find()) return OptionalDouble.empty(); //Not a hireable/promotable rabbit. Could be a locked or maxed rabbit.

		switch (hireMatcher.group(1)) {
			case "HIRE" -> {
				Matcher cpsMatcher = CPS_INCREASE_PATTERN.matcher(lore);
				OptionalInt cps = getIntFromMatcher(cpsMatcher, hireMatcher.end()); //Cps line is right after the hire line
				if (cps.isEmpty()) return OptionalDouble.empty();

				Matcher costMatcher = COST_PATTERN.matcher(lore);
				OptionalInt cost = getIntFromMatcher(costMatcher, cpsMatcher.end()); //Cost comes after the cps line
				if (cost.isEmpty()) return OptionalDouble.empty();
				return OptionalDouble.of(cps.getAsInt() / (double) cost.getAsInt());
			}
			case "PROMOTE" -> {
				Matcher cpsMatcher = CPS_INCREASE_PATTERN.matcher(lore);
				OptionalInt currentCps = getIntFromMatcher(cpsMatcher); //Current cps is before the hire line
				if (currentCps.isEmpty()) return OptionalDouble.empty();
				OptionalInt nextCps = getIntFromMatcher(cpsMatcher, hireMatcher.end()); //Next cps is right after the hire line
				if (nextCps.isEmpty()) return OptionalDouble.empty();

				Matcher costMatcher = COST_PATTERN.matcher(lore);
				OptionalInt cost = getIntFromMatcher(costMatcher, cpsMatcher.end()); //Cost comes after the cps line
				if (cost.isEmpty()) return OptionalDouble.empty();
				return OptionalDouble.of((nextCps.getAsInt() - currentCps.getAsInt()) / (double) cost.getAsInt());
			}
			default -> { return OptionalDouble.empty(); }
		}
	}

	private OptionalInt getIntFromMatcher(Matcher matcher) {
		return getIntFromMatcher(matcher, matcher.hasMatch() ? matcher.end() : 0);
	}

	private OptionalInt getIntFromMatcher(Matcher matcher, int startingIndex) {
		if (!matcher.find(startingIndex)) return OptionalInt.empty();
		return OptionalInt.of(Integer.parseInt(matcher.group(1).replace(",", "")));
	}

	private OptionalDouble getDoubleFromMatcher(Matcher matcher) {
		return getDoubleFromMatcher(matcher, matcher.hasMatch() ? matcher.end() : 0);
	}

	private OptionalDouble getDoubleFromMatcher(Matcher matcher, int startingIndex) {
		if (!matcher.find(startingIndex)) return OptionalDouble.empty();
		return OptionalDouble.of(Double.parseDouble(matcher.group(1).replace(",", "")));
	}
}
