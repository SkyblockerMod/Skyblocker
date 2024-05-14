package de.hysky.skyblocker.skyblock.chocolatefactory;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.apache.commons.math3.analysis.function.Min;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChocolateFactorySolver extends ContainerSolver {
	private static final Pattern CPS_PATTERN = Pattern.compile("([\\d,.]+) Chocolate per second");
	private static final Pattern CPS_INCREASE_PATTERN = Pattern.compile("\\+([\\d,]+) Chocolate per second");
	private static final Pattern COST_PATTERN = Pattern.compile("Cost ([\\d,]+) Chocolate");
	private static final Pattern TOTAL_MULTIPLIER_PATTERN = Pattern.compile("Total Multiplier: ([\\d.]+)x");
	private static final Pattern MULTIPLIER_INCREASE_PATTERN = Pattern.compile("\\+([\\d.]+)x Chocolate per second");
	private static final Pattern TOTAL_CHOCOLATE_PATTERN = Pattern.compile("([\\d,]+) Chocolate");

	public ChocolateFactorySolver() {
		super("^Chocolate Factory$");
	}

	@Override
	protected boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.chocolateFactory.enableChocolateFactoryHelper;
	}

	@Override
	protected List<ColorHighlight> getColors(String[] groups, Int2ObjectMap<ItemStack> slots) {
		final Int2ObjectMap<Rabbit> cpsIncreaseFactors = new Int2ObjectLinkedOpenHashMap<>(6);
		for (int i = 29; i <= 33; i++) { // The 5 rabbits slots are in 29, 30, 31, 32 and 33.
			ItemStack item = slots.get(i);
			if (item.getItem() != Items.PLAYER_HEAD || item.isEmpty()) continue;

			int finalI = i; //Java, pfft.
			getRabbit(item).ifPresent(rabbit -> cpsIncreaseFactors.put(finalI, rabbit));
		}

		//Coach is in slot 42 while the factory info item is in slot 45.
		getCoach(slots.get(45), slots.get(42)).ifPresent(coach -> cpsIncreaseFactors.put(42, coach));
		if (cpsIncreaseFactors.isEmpty()) return List.of(); //Something went wrong.

		OptionalLong totalChocolate = getTotalChocolate(slots.get(13));

		List<Int2ObjectMap.Entry<Rabbit>> sorted = cpsIncreaseFactors.int2ObjectEntrySet()
		                                                             .stream() //Compare cost/cpsIncrease rather than cpsIncrease/cost to avoid getting close to 0 and losing precision.
		                                                             .sorted(Comparator.comparingDouble(entry -> entry.getValue().cost() / entry.getValue().cpsIncrease())) //Ascending order, lower = better
		                                                             .dropWhile(entry -> entry.getValue().cost == 0)
		                                                             .collect(Collectors.toCollection(LinkedList::new));

		Int2ObjectMap.Entry<Rabbit> bestEntry = sorted.removeFirst();
		if (totalChocolate.isEmpty() || bestEntry.getValue().cost < totalChocolate.getAsLong()) return List.of(ColorHighlight.green(bestEntry.getIntKey()));

		for (Int2ObjectMap.Entry<Rabbit> entry : sorted) {
			if (entry.getValue().cost > totalChocolate.getAsLong()) continue;
			return List.of(ColorHighlight.green(entry.getIntKey()), ColorHighlight.yellow(bestEntry.getIntKey()));
		}
		return List.of(ColorHighlight.green(bestEntry.getIntKey()));
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

	private Optional<Rabbit> getCoach(ItemStack cpsItem, ItemStack coachItem) {
		if (!coachItem.isOf(Items.PLAYER_HEAD) || !cpsItem.isOf(Items.COCOA_BEANS)) return Optional.empty();
		String cpsItemLore = getConcattedLore(cpsItem);

		Matcher cpsMatcher = CPS_PATTERN.matcher(cpsItemLore);
		OptionalDouble currentCps = getDoubleFromMatcher(cpsMatcher);
		if (currentCps.isEmpty()) return Optional.empty();

		Matcher multiplierMatcher = TOTAL_MULTIPLIER_PATTERN.matcher(cpsItemLore);
		OptionalDouble totalMultiplier = getDoubleFromMatcher(multiplierMatcher, cpsMatcher.end());
		if (totalMultiplier.isEmpty()) return Optional.empty();

		String coachLore = getConcattedLore(coachItem);

		Matcher multiplierIncreaseMatcher = MULTIPLIER_INCREASE_PATTERN.matcher(coachLore);
		OptionalDouble currentCpsMultiplier = getDoubleFromMatcher(multiplierIncreaseMatcher);
		if (currentCpsMultiplier.isEmpty()) return Optional.empty();

		OptionalDouble nextCpsMultiplier = getDoubleFromMatcher(multiplierIncreaseMatcher);
		if (nextCpsMultiplier.isEmpty()) { //This means that the coach isn't hired yet.
			nextCpsMultiplier = currentCpsMultiplier; //So the first instance of the multiplier is actually the amount we'll get upon upgrading.
			currentCpsMultiplier = OptionalDouble.of(0.0); //And so, we can re-assign values to the variables to make the calculation more readable.
		}

		Matcher costMatcher = COST_PATTERN.matcher(coachLore);
		OptionalInt cost = getIntFromMatcher(costMatcher, multiplierIncreaseMatcher.end()); //Cost comes after the multiplier line
		if (cost.isEmpty()) return Optional.empty();

		return Optional.of(new Rabbit(currentCps.getAsDouble() / totalMultiplier.getAsDouble() * (nextCpsMultiplier.getAsDouble() - currentCpsMultiplier.getAsDouble()), cost.getAsInt()));
	}

	private Optional<Rabbit> getRabbit(ItemStack item) {
		String lore = getConcattedLore(item);
		Matcher cpsMatcher = CPS_INCREASE_PATTERN.matcher(lore);
		OptionalInt currentCps = getIntFromMatcher(cpsMatcher);
		if (currentCps.isEmpty()) return Optional.empty();
		OptionalInt nextCps = getIntFromMatcher(cpsMatcher);
		if (nextCps.isEmpty()) {
			nextCps = currentCps; //This means that the rabbit isn't hired yet.
			currentCps = OptionalInt.of(0); //So the first instance of the cps is actually the amount we'll get upon hiring.
		}

		Matcher costMatcher = COST_PATTERN.matcher(lore);
		OptionalInt cost = getIntFromMatcher(costMatcher, cpsMatcher.end()); //Cost comes after the cps line
		if (cost.isEmpty()) return Optional.empty();
		return Optional.of(new Rabbit(nextCps.getAsInt() - currentCps.getAsInt(), cost.getAsInt()));
	}

	private OptionalLong getTotalChocolate(ItemStack item) {
		if (item.isEmpty() || item.getItem() != Items.PLAYER_HEAD) return OptionalLong.empty();
		Matcher matcher = TOTAL_CHOCOLATE_PATTERN.matcher(item.getName().getString());
		if (!matcher.find()) return OptionalLong.empty();
		return OptionalLong.of(Long.parseLong(matcher.group(1).replace(",", "")));
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

	private record Rabbit(double cpsIncrease, int cost) {
	}
}
