package de.hysky.skyblocker.skyblock.chocolatefactory;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RegexUtils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.item.TooltipType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChocolateFactorySolver extends ContainerSolver {
	private static final Pattern CPS_PATTERN = Pattern.compile("([\\d,.]+) Chocolate per second");
	private static final Pattern CPS_INCREASE_PATTERN = Pattern.compile("\\+([\\d,]+) Chocolate per second");
	private static final Pattern COST_PATTERN = Pattern.compile("Cost ([\\d,]+) Chocolate");
	private static final Pattern TOTAL_MULTIPLIER_PATTERN = Pattern.compile("Total Multiplier: ([\\d.]+)x");
	private static final Pattern MULTIPLIER_INCREASE_PATTERN = Pattern.compile("\\+([\\d.]+)x Chocolate per second");
	private static final Pattern CHOCOLATE_PATTERN = Pattern.compile("^([\\d,]+) Chocolate$");
	private static final ObjectArrayList<Rabbit> cpsIncreaseFactors = new ObjectArrayList<>(6);
	private static long totalChocolate = -1L;
	private static double totalCps = -1.0;
	private static double totalCpsMultiplier = -1.0;
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.#");
	private static ItemStack bestUpgrade = null;
	private static ItemStack bestAffordableUpgrade = null;

	public ChocolateFactorySolver() {
		super("^Chocolate Factory$");
		ItemTooltipCallback.EVENT.register(ChocolateFactorySolver::handleTooltip);
	}

	@Override
	protected boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.chocolateFactory.enableChocolateFactoryHelper;
	}

	@Override
	protected List<ColorHighlight> getColors(String[] groups, Int2ObjectMap<ItemStack> slots) {
		updateFactoryInfo(slots);

		if (totalChocolate <= 0 || cpsIncreaseFactors.isEmpty()) return List.of(); //Something went wrong or there's nothing we can afford.
		Rabbit bestRabbit = cpsIncreaseFactors.getFirst();
		bestUpgrade = bestRabbit.itemStack;
		if (bestRabbit.cost <= totalChocolate) return List.of(ColorHighlight.green(bestRabbit.slot));

		for (Rabbit rabbit : cpsIncreaseFactors.subList(1, cpsIncreaseFactors.size())) {
			if (rabbit.cost <= totalChocolate) {
				bestAffordableUpgrade = rabbit.itemStack;
				return List.of(ColorHighlight.green(rabbit.slot), ColorHighlight.yellow(bestRabbit.slot));
			}
		}

		return List.of(ColorHighlight.yellow(bestRabbit.slot));
	}

	private static void updateFactoryInfo(Int2ObjectMap<ItemStack> slots) {
		cpsIncreaseFactors.clear();

		for (int i = 29; i <= 33; i++) { // The 5 rabbits slots are in 29, 30, 31, 32 and 33.
			ItemStack item = slots.get(i);
			if (item.isOf(Items.PLAYER_HEAD)) {
				getRabbit(item, i).ifPresent(cpsIncreaseFactors::add);
			}
		}

		//Coach is in slot 42
		getCoach(slots.get(42)).ifPresent(cpsIncreaseFactors::add);
		RegexUtils.getLongFromMatcher(CHOCOLATE_PATTERN.matcher(slots.get(13).getName().getString())).ifPresent(l -> totalChocolate = l);

		//Cps item (cocoa bean) is in slot 45
		String cpsItemLore = getConcatenatedLore(slots.get(45));
		Matcher cpsMatcher = CPS_PATTERN.matcher(cpsItemLore);
		RegexUtils.getDoubleFromMatcher(cpsMatcher).ifPresent(d -> totalCps = d);

		Matcher multiplierMatcher = TOTAL_MULTIPLIER_PATTERN.matcher(cpsItemLore);
		RegexUtils.getDoubleFromMatcher(multiplierMatcher, cpsMatcher.hasMatch() ? cpsMatcher.end() : 0).ifPresent(d -> totalCpsMultiplier = d);

		//Compare cost/cpsIncrease rather than cpsIncrease/cost to avoid getting close to 0 and losing precision.
		cpsIncreaseFactors.sort(Comparator.comparingDouble(rabbit -> rabbit.cost() / rabbit.cpsIncrease())); //Ascending order, lower = better
	}

	private static void handleTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> lines) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableChocolateFactoryHelper) return;
		if (!(MinecraftClient.getInstance().currentScreen instanceof GenericContainerScreen screen) || !screen.getTitle().getString().equals("Chocolate Factory") ) return;

		String lore = concatenateLore(lines);
		Matcher costMatcher = COST_PATTERN.matcher(lore);
		OptionalLong cost = RegexUtils.getLongFromMatcher(costMatcher);
		if (cost.isEmpty() || totalChocolate == -1L || totalCps == -1.0) return;

		lines.add(ItemTooltip.createSmoothLine());

		lines.add(Text.literal("")
		              .append(Text.literal("Time until upgrade: ").formatted(Formatting.GRAY))
		              .append(formatTime((cost.getAsLong() - totalChocolate) / totalCps)));

		if (cpsIncreaseFactors.isEmpty()) return;

		for (int j = 0; j < cpsIncreaseFactors.size(); j++) {
			Rabbit rabbit = cpsIncreaseFactors.get(j);
			if (rabbit.itemStack != stack) continue;

			lines.add(Text.literal("")
			              .append(Text.literal("CPS Increase: ").formatted(Formatting.GRAY))
			              .append(Text.literal(DECIMAL_FORMAT.format(rabbit.cpsIncrease)).formatted(Formatting.GOLD)));

			lines.add(Text.literal("")
			              .append(Text.literal("Cost per CPS: ").formatted(Formatting.GRAY))
			              .append(Text.literal(DECIMAL_FORMAT.format(rabbit.cost / rabbit.cpsIncrease)).formatted(Formatting.GOLD)));

			if (rabbit.itemStack == bestUpgrade) {
				if (cost.getAsLong() <= totalChocolate) {
					lines.add(Text.literal("Best upgrade").formatted(Formatting.GREEN));
				} else {
					lines.add(Text.literal("Best upgrade, can't afford").formatted(Formatting.YELLOW));
				}
			} else if (rabbit.itemStack == bestAffordableUpgrade && cost.getAsLong() <= totalChocolate) {
				lines.add(Text.literal("Best upgrade you can afford").formatted(Formatting.GREEN));
			}
		}
	}

	private static MutableText formatTime(double seconds) {
		if (seconds <= 0) return Text.literal("Now").formatted(Formatting.GREEN);

		StringBuilder builder = new StringBuilder();
		if (seconds >= 86400) {
			builder.append((int) (seconds / 86400)).append("d ");
			seconds %= 86400;
		}
		if (seconds >= 3600) {
			builder.append((int) (seconds / 3600)).append("h ");
			seconds %= 3600;
		}
		if (seconds >= 60) {
			builder.append((int) (seconds / 60)).append("m ");
			seconds %= 60;
		}
		if (seconds >= 1) {
			builder.append((int) seconds).append("s");
		}
		return Text.literal(builder.toString()).formatted(Formatting.GOLD);
	}

	/**
	 * Utility method.
	 */
	private static String getConcatenatedLore(ItemStack item) {
		return concatenateLore(ItemUtils.getLore(item));
	}

	/**
	 * Concatenates the lore of an item into one string.
	 * This is useful in case some pattern we're looking for is split into multiple lines, which would make it harder to regex.
	 */
	private static String concatenateLore(List<Text> lore) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < lore.size(); i++) {
			stringBuilder.append(lore.get(i).getString());
			if (i != lore.size() - 1) stringBuilder.append(" ");
		}
		return stringBuilder.toString();
	}

	private static Optional<Rabbit> getCoach(ItemStack coachItem) {
		if (!coachItem.isOf(Items.PLAYER_HEAD)) return Optional.empty();
		String coachLore = getConcatenatedLore(coachItem);

		if (totalCpsMultiplier == -1.0) return Optional.empty(); //We need the total multiplier to calculate the increase in cps.

		Matcher multiplierIncreaseMatcher = MULTIPLIER_INCREASE_PATTERN.matcher(coachLore);
		OptionalDouble currentCpsMultiplier = RegexUtils.getDoubleFromMatcher(multiplierIncreaseMatcher);
		if (currentCpsMultiplier.isEmpty()) return Optional.empty();

		OptionalDouble nextCpsMultiplier = RegexUtils.getDoubleFromMatcher(multiplierIncreaseMatcher);
		if (nextCpsMultiplier.isEmpty()) { //This means that the coach isn't hired yet.
			nextCpsMultiplier = currentCpsMultiplier; //So the first instance of the multiplier is actually the amount we'll get upon upgrading.
			currentCpsMultiplier = OptionalDouble.of(0.0); //And so, we can re-assign values to the variables to make the calculation more readable.
		}

		Matcher costMatcher = COST_PATTERN.matcher(coachLore);
		OptionalInt cost = RegexUtils.getIntFromMatcher(costMatcher, multiplierIncreaseMatcher.hasMatch() ? multiplierIncreaseMatcher.end() : 0); //Cost comes after the multiplier line
		if (cost.isEmpty()) return Optional.empty();

		return Optional.of(new Rabbit(totalCps / totalCpsMultiplier * (nextCpsMultiplier.getAsDouble() - currentCpsMultiplier.getAsDouble()), cost.getAsInt(), 42, coachItem));
	}

	private static Optional<Rabbit> getRabbit(ItemStack item, int slot) {
		String lore = getConcatenatedLore(item);
		Matcher cpsMatcher = CPS_INCREASE_PATTERN.matcher(lore);
		OptionalInt currentCps = RegexUtils.getIntFromMatcher(cpsMatcher);
		if (currentCps.isEmpty()) return Optional.empty();
		OptionalInt nextCps = RegexUtils.getIntFromMatcher(cpsMatcher);
		if (nextCps.isEmpty()) {
			nextCps = currentCps; //This means that the rabbit isn't hired yet.
			currentCps = OptionalInt.of(0); //So the first instance of the cps is actually the amount we'll get upon hiring.
		}

		Matcher costMatcher = COST_PATTERN.matcher(lore);
		OptionalInt cost = RegexUtils.getIntFromMatcher(costMatcher, cpsMatcher.hasMatch() ? cpsMatcher.end() : 0); //Cost comes after the cps line
		if (cost.isEmpty()) return Optional.empty();
		return Optional.of(new Rabbit(nextCps.getAsInt() - currentCps.getAsInt(), cost.getAsInt(), slot, item));
	}

	private record Rabbit(double cpsIncrease, int cost, int slot, ItemStack itemStack) {
	}
}
