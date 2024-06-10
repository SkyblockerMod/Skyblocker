package de.hysky.skyblocker.skyblock.chocolatefactory;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RegexUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChocolateFactorySolver extends ContainerSolver {
	//Patterns
	private static final Pattern CPS_PATTERN = Pattern.compile("([\\d,.]+) Chocolate per second");
	private static final Pattern CPS_INCREASE_PATTERN = Pattern.compile("\\+([\\d,]+) Chocolate per second");
	private static final Pattern COST_PATTERN = Pattern.compile("Cost ([\\d,]+) Chocolate");
	private static final Pattern TOTAL_MULTIPLIER_PATTERN = Pattern.compile("Total Multiplier: ([\\d.]+)x");
	private static final Pattern MULTIPLIER_INCREASE_PATTERN = Pattern.compile("\\+([\\d.]+)x Chocolate per second");
	private static final Pattern CHOCOLATE_PATTERN = Pattern.compile("^([\\d,]+) Chocolate$");
	private static final Pattern PRESTIGE_REQUIREMENT_PATTERN = Pattern.compile("Chocolate this Prestige: ([\\d,]+) +Requires (\\S+) Chocolate this Prestige!");
	private static final Pattern TIME_TOWER_STATUS_PATTERN = Pattern.compile("Status: (ACTIVE|INACTIVE)");

	private static final ObjectArrayList<Rabbit> cpsIncreaseFactors = new ObjectArrayList<>(8);
	private static long totalChocolate = -1L;
	private static double totalCps = -1.0;
	private static double totalCpsMultiplier = -1.0;
	private static long requiredUntilNextPrestige = -1L;
	private static boolean canPrestige = false;
	private static boolean reachedMaxPrestige = false;
	private static double timeTowerMultiplier = -1.0;
	private static boolean isTimeTowerActive = false;
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	private static ItemStack bestUpgrade = null;
	private static ItemStack bestAffordableUpgrade = null;

	//Slots, for ease of maintenance rather than using magic numbers everywhere.
	private static final byte RABBITS_START = 28;
	private static final byte RABBITS_END = 34;
	private static final byte COACH_SLOT = 42;
	private static final byte CHOCOLATE_SLOT = 13;
	private static final byte CPS_SLOT = 45;
	private static final byte PRESTIGE_SLOT = 27;
	private static final byte TIME_TOWER_SLOT = 39;
	private static final byte STRAY_RABBIT_START = 0;
	private static final byte STRAY_RABBIT_END = 26;

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
		List<ColorHighlight> highlights = new ArrayList<>();

		getPrestigeHighlight().ifPresent(highlights::add);
		highlights.addAll(getStrayRabbitHighlight(slots));

		if (totalChocolate <= 0 || cpsIncreaseFactors.isEmpty()) return highlights; //Something went wrong or there's nothing we can afford.
		Rabbit bestRabbit = cpsIncreaseFactors.getFirst();
		bestUpgrade = bestRabbit.itemStack;
		if (bestRabbit.cost <= totalChocolate) {
			highlights.add(ColorHighlight.green(bestRabbit.slot));
			return highlights;
		}
		highlights.add(ColorHighlight.yellow(bestRabbit.slot));

		for (Rabbit rabbit : cpsIncreaseFactors.subList(1, cpsIncreaseFactors.size())) {
			if (rabbit.cost <= totalChocolate) {
				bestAffordableUpgrade = rabbit.itemStack;
				highlights.add(ColorHighlight.green(rabbit.slot));
				break;
			}
		}

		return highlights;
	}

	private static void updateFactoryInfo(Int2ObjectMap<ItemStack> slots) {
		cpsIncreaseFactors.clear();

		for (int i = RABBITS_START; i <= RABBITS_END; i++) { // The 7 rabbits slots are in 28, 29, 30, 31, 32, 33 and 34.
			ItemStack item = slots.get(i);
			if (item.isOf(Items.PLAYER_HEAD)) {
				getRabbit(item, i).ifPresent(cpsIncreaseFactors::add);
			}
		}

		//Coach is in slot 42
		getCoach(slots.get(COACH_SLOT)).ifPresent(cpsIncreaseFactors::add);

		//The clickable chocolate is in slot 13, holds the total chocolate
		RegexUtils.getLongFromMatcher(CHOCOLATE_PATTERN.matcher(slots.get(CHOCOLATE_SLOT).getName().getString())).ifPresent(l -> totalChocolate = l);

		//Cps item (cocoa bean) is in slot 45
		String cpsItemLore = Utils.getConcatenatedLore(slots.get(45));

		Matcher cpsMatcher = CPS_PATTERN.matcher(cpsItemLore);
		RegexUtils.getDoubleFromMatcher(cpsMatcher).ifPresent(d -> totalCps = d);
		Matcher multiplierMatcher = TOTAL_MULTIPLIER_PATTERN.matcher(cpsItemLore);
		RegexUtils.getDoubleFromMatcher(multiplierMatcher, cpsMatcher.hasMatch() ? cpsMatcher.end() : 0).ifPresent(d -> totalCpsMultiplier = d);

		//Prestige item is in slot 28
		Matcher prestigeMatcher = PRESTIGE_REQUIREMENT_PATTERN.matcher(Utils.getConcatenatedLore(slots.get(28)));
		OptionalLong currentChocolate = RegexUtils.getLongFromMatcher(prestigeMatcher);
		if (currentChocolate.isPresent()) {
			String requirement = prestigeMatcher.group(2); //If the first one matched, we can assume the 2nd one is also matched since it's one whole regex
			//Since the last character is either M or B we can just try to replace both characters. Only the correct one will actually replace anything.
			String amountString = requirement.replace("M", "000000").replace("B", "000000000");
			if (NumberUtils.isParsable(amountString)) {
				requiredUntilNextPrestige = Long.parseLong(amountString) - currentChocolate.getAsLong();
			}
		} else if (prestigeLore.endsWith("Click to prestige!")) {
			canPrestige = true;
			reachedMaxPrestige = false;
		} else if (prestigeLore.endsWith("You have reached max prestige!")) {
			canPrestige = false;
			reachedMaxPrestige = true;
		}

		//Time Tower is in slot 39
		timeTowerMultiplier = romanToDecimal(StringUtils.substringAfterLast(slots.get(39).getName().getString(), ' ')) / 10.0; //The name holds the level, which is multiplier * 10 in roman numerals
		Matcher timeTowerStatusMatcher = TIME_TOWER_STATUS_PATTERN.matcher(Utils.getConcatenatedLore(slots.get(39)));
		if (timeTowerStatusMatcher.find()) {
			isTimeTowerActive = timeTowerStatusMatcher.group(1).equals("ACTIVE");
		}

		//Compare cost/cpsIncrease rather than cpsIncrease/cost to avoid getting close to 0 and losing precision.
		cpsIncreaseFactors.sort(Comparator.comparingDouble(rabbit -> rabbit.cost() / rabbit.cpsIncrease())); //Ascending order, lower = better
	}

	private static void handleTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipType tooltipType, List<Text> lines) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableChocolateFactoryHelper) return;
		if (!(MinecraftClient.getInstance().currentScreen instanceof GenericContainerScreen screen) || !screen.getTitle().getString().equals("Chocolate Factory")) return;

		int lineIndex = lines.size();
		//This boolean is used to determine if we should add a smooth line to separate the added information from the rest of the tooltip.
		//It should be set to true if there's any information added, false otherwise.
		boolean shouldAddLine = false;

		String lore = Utils.concatenateLore(lines);
		Matcher costMatcher = COST_PATTERN.matcher(lore);
		OptionalLong cost = RegexUtils.getLongFromMatcher(costMatcher);
		//Available on all items with a chocolate cost
		if (cost.isPresent()) shouldAddLine = addUpgradeTimerToLore(lines, cost.getAsLong());

		//Prestige item
		if (stack.isOf(Items.DROPPER)) {
			shouldAddLine = addPrestigeTimerToLore(lines) || shouldAddLine;
		}
		//Time tower
		else if (stack.isOf(Items.CLOCK)) {
			shouldAddLine = addTimeTowerStatsToLore(lines) || shouldAddLine;
		}
		//Rabbits
		else if (stack.isOf(Items.PLAYER_HEAD)) {
			shouldAddLine = addRabbitStatsToLore(lines, stack) || shouldAddLine;
		}

		//This is an ArrayList, so this operation is probably not very efficient, but logically it's pretty much the only way I can think of
		if (shouldAddLine) lines.add(lineIndex, ItemTooltip.createSmoothLine());
	}

	private static boolean addUpgradeTimerToLore(List<Text> lines, long cost) {
		if (totalChocolate < 0L || totalCps < 0.0) return false;
		lines.add(Text.empty()
		              .append(Text.literal("Time until upgrade: ").formatted(Formatting.GRAY))
		              .append(formatTime((cost - totalChocolate) / totalCps)));
		return true;
	}

	private static boolean addPrestigeTimerToLore(List<Text> lines) {
		if (totalCps < 0.0 || reachedMaxPrestige) return false;
		if (requiredUntilNextPrestige > 0 && !canPrestige) {
			lines.add(Text.empty()
			              .append(Text.literal("Chocolate until next prestige: ").formatted(Formatting.GRAY))
			              .append(Text.literal(DECIMAL_FORMAT.format(requiredUntilNextPrestige)).formatted(Formatting.GOLD)));
		}
		lines.add(Text.empty() //Keep this outside of the `if` to match the format of the upgrade tooltips, that say "Time until upgrade: Now" when it's possible
		              .append(Text.literal("Time until next prestige: ").formatted(Formatting.GRAY))
		              .append(formatTime(requiredUntilNextPrestige / totalCps)));
		return true;
	}

	private static boolean addTimeTowerStatsToLore(List<Text> lines) {
		if (totalCps < 0.0 || totalCpsMultiplier < 0.0 || timeTowerMultiplier < 0.0) return false;
		lines.add(Text.literal("Current stats:").formatted(Formatting.GRAY));
		lines.add(Text.empty()
		              .append(Text.literal("  CPS increase: ").formatted(Formatting.GRAY))
		              .append(Text.literal(DECIMAL_FORMAT.format(totalCps / totalCpsMultiplier * timeTowerMultiplier)).formatted(Formatting.GOLD)));
		lines.add(Text.empty()
		              .append(Text.literal("  CPS when active: ").formatted(Formatting.GRAY))
		              .append(Text.literal(DECIMAL_FORMAT.format(isTimeTowerActive ? totalCps : totalCps / totalCpsMultiplier * (timeTowerMultiplier + totalCpsMultiplier))).formatted(Formatting.GOLD)));
		if (timeTowerMultiplier < 1.5) {
			lines.add(Text.literal("Stats after upgrade:").formatted(Formatting.GRAY));
			lines.add(Text.empty()
			              .append(Text.literal("  CPS increase: ").formatted(Formatting.GRAY))
			              .append(Text.literal(DECIMAL_FORMAT.format(totalCps / (totalCpsMultiplier) * (timeTowerMultiplier + 0.1))).formatted(Formatting.GOLD)));
			lines.add(Text.empty()
			              .append(Text.literal("  CPS when active: ").formatted(Formatting.GRAY))
			              .append(Text.literal(DECIMAL_FORMAT.format(isTimeTowerActive ? totalCps / totalCpsMultiplier * (totalCpsMultiplier + 0.1) : totalCps / totalCpsMultiplier * (timeTowerMultiplier + 0.1 + totalCpsMultiplier))).formatted(Formatting.GOLD)));
		}
		return true;
	}

	private static boolean addRabbitStatsToLore(List<Text> lines, ItemStack stack) {
		if (cpsIncreaseFactors.isEmpty()) return false;
		boolean changed = false;
		for (Rabbit rabbit : cpsIncreaseFactors) {
			if (rabbit.itemStack != stack) continue;
			changed = true;
			lines.add(Text.empty()
			              .append(Text.literal("CPS Increase: ").formatted(Formatting.GRAY))
			              .append(Text.literal(DECIMAL_FORMAT.format(rabbit.cpsIncrease)).formatted(Formatting.GOLD)));

			lines.add(Text.empty()
			              .append(Text.literal("Cost per CPS: ").formatted(Formatting.GRAY))
			              .append(Text.literal(DECIMAL_FORMAT.format(rabbit.cost / rabbit.cpsIncrease)).formatted(Formatting.GOLD)));

			if (rabbit.itemStack == bestUpgrade) {
				if (rabbit.cost <= totalChocolate) {
					lines.add(Text.literal("Best upgrade").formatted(Formatting.GREEN));
				} else {
					lines.add(Text.literal("Best upgrade, can't afford").formatted(Formatting.YELLOW));
				}
			} else if (rabbit.itemStack == bestAffordableUpgrade && rabbit.cost <= totalChocolate) {
				lines.add(Text.literal("Best upgrade you can afford").formatted(Formatting.GREEN));
			}
		}
		return changed;
	}

	private static MutableText formatTime(double seconds) {
		seconds = Math.ceil(seconds);
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

	private static Optional<Rabbit> getCoach(ItemStack coachItem) {
		if (!coachItem.isOf(Items.PLAYER_HEAD)) return Optional.empty();
		String coachLore = Utils.getConcatenatedLore(coachItem);

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

		return Optional.of(new Rabbit(totalCps / totalCpsMultiplier * (nextCpsMultiplier.getAsDouble() - currentCpsMultiplier.getAsDouble()), cost.getAsInt(), COACH_SLOT, coachItem));
	}

	private static Optional<Rabbit> getRabbit(ItemStack item, int slot) {
		String lore = Utils.getConcatenatedLore(item);
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

	private static Optional<ColorHighlight> getPrestigeHighlight() {
		if (reachedMaxPrestige) return Optional.empty();
		if (canPrestige) return Optional.of(ColorHighlight.green(PRESTIGE_SLOT));
		return Optional.of(ColorHighlight.red(PRESTIGE_SLOT));
	}

	private static List<ColorHighlight> getStrayRabbitHighlight(Int2ObjectMap<ItemStack> slots) {
		final List<ColorHighlight> highlights = new ArrayList<>();
		for (byte i = STRAY_RABBIT_START; i <= STRAY_RABBIT_END; i++) {
			ItemStack item = slots.get(i);
			if (!item.isOf(Items.PLAYER_HEAD)) continue;
			String name = item.getName().getString();
			if (name.equals("CLICK ME!") || name.startsWith("GOLDEN RABBIT")) {
				highlights.add(ColorHighlight.green(i));
			}
		}
		return highlights;
	}

	private record Rabbit(double cpsIncrease, int cost, int slot, ItemStack itemStack) {
	}

	//Perhaps the part below can go to a separate file later on, but I couldn't find a proper name for the class, so they're staying here.
	private static final Map<Character, Integer> romanMap = Map.of(
			'I', 1,
			'V', 5,
			'X', 10,
			'L', 50,
			'C', 100,
			'D', 500,
			'M', 1000
	);

	public static int romanToDecimal(String romanNumeral) {
		int decimal = 0;
		int lastNumber = 0;
		for (int i = romanNumeral.length() - 1; i >= 0; i--) {
			char ch = romanNumeral.charAt(i);
			decimal = romanMap.get(ch) >= lastNumber ? decimal + romanMap.get(ch) : decimal - romanMap.get(ch);
			lastNumber = romanMap.get(ch);
		}
		return decimal;
	}
}
