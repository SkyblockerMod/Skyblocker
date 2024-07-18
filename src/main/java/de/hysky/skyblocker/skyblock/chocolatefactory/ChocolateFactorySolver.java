package de.hysky.skyblocker.skyblock.chocolatefactory;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.LineSmoothener;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RegexUtils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
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
	private static final Pattern TIME_TOWER_MULTIPLIER_PATTERN = Pattern.compile("by \\+([\\d.]+)x for \\dh\\.");
	private static final NumberFormat DECIMAL_FORMAT = NumberFormat.getInstance(Locale.US);

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

	// TODO: Convert to instance fields in #788
	private static final ObjectArrayList<Rabbit> cpsIncreaseFactors = new ObjectArrayList<>(8);
	private static long totalChocolate = -1L;
	private static double totalCps = -1.0;
	private static double totalCpsMultiplier = -1.0;
	private static long requiredUntilNextPrestige = -1L;
	private static boolean canPrestige = false;
	private static boolean reachedMaxPrestige = false;
	private static double timeTowerMultiplier = -1.0;
	private static boolean isTimeTowerMaxed = false;
	private static boolean isTimeTowerActive = false;
	private static int bestUpgrade = -1;
	private static int bestAffordableUpgrade = -1;

	private static StraySound ding = StraySound.NONE;
	private static int dingTick = 0;

	static {
		DECIMAL_FORMAT.setMinimumFractionDigits(0);
		DECIMAL_FORMAT.setMaximumFractionDigits(1);
	}

	public ChocolateFactorySolver() {
		super("^Chocolate Factory$"); //There are multiple screens that fit the pattern `^Chocolate Factory`, so the $ is required
		ClientTickEvents.START_CLIENT_TICK.register(ChocolateFactorySolver::onTick);
	}

	private static void onTick(MinecraftClient client) {
		if (ding != StraySound.NONE) {
			dingTick = (++dingTick) % (ding == StraySound.NORMAL ? 5 : 3);
			if (dingTick == 0) {
				client.getSoundManager().play(PositionedSoundInstance.master(ding == StraySound.NORMAL ? SoundEvents.BLOCK_NOTE_BLOCK_PLING.value() : SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), 1.f, 1.f));
			}
		}
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
		bestUpgrade = bestRabbit.slot;
		if (bestRabbit.cost <= totalChocolate) {
			highlights.add(ColorHighlight.green(bestRabbit.slot));
			return highlights;
		}
		highlights.add(ColorHighlight.yellow(bestRabbit.slot));

		for (Rabbit rabbit : cpsIncreaseFactors.subList(1, cpsIncreaseFactors.size())) {
			if (rabbit.cost <= totalChocolate) {
				bestAffordableUpgrade = rabbit.slot;
				highlights.add(ColorHighlight.green(rabbit.slot));
				break;
			}
		}

		return highlights;
	}

	private void updateFactoryInfo(Int2ObjectMap<ItemStack> slots) {
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
		String cpsItemLore = getConcatenatedLore(slots.get(CPS_SLOT));
		Matcher cpsMatcher = CPS_PATTERN.matcher(cpsItemLore);
		RegexUtils.getDoubleFromMatcher(cpsMatcher).ifPresent(d -> totalCps = d);
		Matcher multiplierMatcher = TOTAL_MULTIPLIER_PATTERN.matcher(cpsItemLore);
		RegexUtils.getDoubleFromMatcher(multiplierMatcher, cpsMatcher.hasMatch() ? cpsMatcher.end() : 0).ifPresent(d -> totalCpsMultiplier = d);

		//Prestige item is in slot 28
		String prestigeLore = getConcatenatedLore(slots.get(PRESTIGE_SLOT));
		Matcher prestigeMatcher = PRESTIGE_REQUIREMENT_PATTERN.matcher(prestigeLore);
		OptionalLong currentChocolate = RegexUtils.getLongFromMatcher(prestigeMatcher);
		if (currentChocolate.isPresent()) {
			String requirement = prestigeMatcher.group(2); //If the first one matched, we can assume the 2nd one is also matched since it's one whole regex
			//Since the last character is either M or B we can just try to replace both characters. Only the correct one will actually replace anything.
			String amountString = requirement.replace("M", "000000").replace("B", "000000000");
			if (NumberUtils.isParsable(amountString)) {
				requiredUntilNextPrestige = Long.parseLong(amountString) - currentChocolate.getAsLong();
			}
			canPrestige = false;
		} else if (prestigeLore.endsWith("Click to prestige!")) {
			canPrestige = true;
			reachedMaxPrestige = false;
		} else if (prestigeLore.endsWith("You have reached max prestige!")) {
			canPrestige = false;
			reachedMaxPrestige = true;
		}

		//Time Tower is in slot 39
		isTimeTowerMaxed = StringUtils.substringAfterLast(slots.get(TIME_TOWER_SLOT).getName().getString(), ' ').equals("XV");
		String timeTowerLore = getConcatenatedLore(slots.get(TIME_TOWER_SLOT));
		Matcher timeTowerMultiplierMatcher = TIME_TOWER_MULTIPLIER_PATTERN.matcher(timeTowerLore);
		RegexUtils.getDoubleFromMatcher(timeTowerMultiplierMatcher).ifPresent(d -> timeTowerMultiplier = d);
		Matcher timeTowerStatusMatcher = TIME_TOWER_STATUS_PATTERN.matcher(timeTowerLore);
		if (timeTowerStatusMatcher.find(timeTowerMultiplierMatcher.hasMatch() ? timeTowerMultiplierMatcher.end() : 0)) {
			isTimeTowerActive = timeTowerStatusMatcher.group(1).equals("ACTIVE");
		}

		//Compare cost/cpsIncrease rather than cpsIncrease/cost to avoid getting close to 0 and losing precision.
		cpsIncreaseFactors.sort(Comparator.comparingDouble(rabbit -> rabbit.cost() / rabbit.cpsIncrease())); //Ascending order, lower = better
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

	private Optional<Rabbit> getCoach(ItemStack coachItem) {
		if (!coachItem.isOf(Items.PLAYER_HEAD)) return Optional.empty();
		String coachLore = getConcatenatedLore(coachItem);

		if (totalCps < 0 || totalCpsMultiplier < 0) return Optional.empty(); //We need these 2 to calculate the increase in cps.

		Matcher multiplierIncreaseMatcher = MULTIPLIER_INCREASE_PATTERN.matcher(coachLore);
		OptionalDouble currentCpsMultiplier = RegexUtils.getDoubleFromMatcher(multiplierIncreaseMatcher);
		if (currentCpsMultiplier.isEmpty()) return Optional.empty();

		OptionalDouble nextCpsMultiplier = RegexUtils.getDoubleFromMatcher(multiplierIncreaseMatcher);
		if (nextCpsMultiplier.isEmpty()) { //This means that the coach isn't hired yet.
			nextCpsMultiplier = currentCpsMultiplier; //So the first instance of the multiplier is actually the amount we'll get upon upgrading.
			currentCpsMultiplier = OptionalDouble.of(0.0); //And so, we can re-assign values to the variables to make the calculation more readable.
		}

		Matcher costMatcher = COST_PATTERN.matcher(coachLore);
		OptionalLong cost = RegexUtils.getLongFromMatcher(costMatcher, multiplierIncreaseMatcher.hasMatch() ? multiplierIncreaseMatcher.end() : 0); //Cost comes after the multiplier line
		if (cost.isEmpty()) return Optional.empty();

		return Optional.of(new Rabbit(totalCps / totalCpsMultiplier * (nextCpsMultiplier.getAsDouble() - currentCpsMultiplier.getAsDouble()), cost.getAsLong(), COACH_SLOT));
	}

	private Optional<Rabbit> getRabbit(ItemStack item, int slot) {
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
		OptionalLong cost = RegexUtils.getLongFromMatcher(costMatcher, cpsMatcher.hasMatch() ? cpsMatcher.end() : 0); //Cost comes after the cps line
		if (cost.isEmpty()) return Optional.empty();
		return Optional.of(new Rabbit((nextCps.getAsInt() - currentCps.getAsInt()) * (totalCpsMultiplier < 0 ? 1 : totalCpsMultiplier), cost.getAsLong(), slot));
	}

	private Optional<ColorHighlight> getPrestigeHighlight() {
		if (reachedMaxPrestige) return Optional.empty();
		if (canPrestige) return Optional.of(ColorHighlight.green(PRESTIGE_SLOT));
		return Optional.of(ColorHighlight.red(PRESTIGE_SLOT));
	}

	private List<ColorHighlight> getStrayRabbitHighlight(Int2ObjectMap<ItemStack> slots) {
		ding = StraySound.NONE;
		final List<ColorHighlight> highlights = new ArrayList<>();
		for (byte i = STRAY_RABBIT_START; i <= STRAY_RABBIT_END; i++) {
			ItemStack item = slots.get(i);
			if (!item.isOf(Items.PLAYER_HEAD)) continue;
			String name = item.getName().getString();
			if (name.equals("CLICK ME!") || name.startsWith("Golden Rabbit - ")) {
				if (SkyblockerConfigManager.get().helpers.chocolateFactory.straySound) ding = name.startsWith("Golden") ? StraySound.GOLDEN : StraySound.NORMAL;
				highlights.add(ColorHighlight.green(i));
			}
		}
		return highlights;
	}

	@Override
	protected void reset() {
		cpsIncreaseFactors.clear();
		totalChocolate = -1L;
		totalCps = -1.0;
		totalCpsMultiplier = -1.0;
		requiredUntilNextPrestige = -1L;
		canPrestige = false;
		reachedMaxPrestige = false;
		timeTowerMultiplier = -1.0;
		isTimeTowerMaxed = false;
		isTimeTowerActive = false;
		bestUpgrade = -1;
		bestAffordableUpgrade = -1;
		ding = StraySound.NONE;
	}

	private enum StraySound {
		NONE,
		NORMAL,
		GOLDEN
	}

	private record Rabbit(double cpsIncrease, long cost, int slot) {}

	public static final class Tooltip extends TooltipAdder {
		public Tooltip() {
			super("^Chocolate Factory$", 0); //The priority doesn't really matter here as this is the only tooltip adder for the Chocolate Factory.
		}

		@Override
		public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
			if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableChocolateFactoryHelper || focusedSlot == null) return;

			int lineIndex = lines.size();
			//This boolean is used to determine if we should add a smooth line to separate the added information from the rest of the tooltip.
			//It should be set to true if there's any information added, false otherwise.
			boolean shouldAddLine = false;

			String lore = concatenateLore(lines);
			Matcher costMatcher = COST_PATTERN.matcher(lore);
			OptionalLong cost = RegexUtils.getLongFromMatcher(costMatcher);
			//Available on all items with a chocolate cost
			if (cost.isPresent()) shouldAddLine |= addUpgradeTimerToLore(lines, cost.getAsLong());

			int index = focusedSlot.id;

			//Prestige item
			if (index == PRESTIGE_SLOT) {
				shouldAddLine |= addPrestigeTimerToLore(lines);
			}
			//Time tower
			else if (index == TIME_TOWER_SLOT) {
				shouldAddLine |= addTimeTowerStatsToLore(lines);
			}
			//Rabbits
			else if (index == COACH_SLOT || (index >= RABBITS_START && index <= RABBITS_END)) {
				shouldAddLine |= addRabbitStatsToLore(lines, index);
			}

			//This is an ArrayList, so this operation is probably not very efficient, but logically it's pretty much the only way I can think of
			if (shouldAddLine) lines.add(lineIndex, LineSmoothener.createSmoothLine());
		}

		private boolean addUpgradeTimerToLore(List<Text> lines, long cost) {
			if (totalChocolate < 0L || totalCps < 0.0) return false;
			lines.add(Text.empty()
					.append(Text.literal("Time until upgrade: ").formatted(Formatting.GRAY))
					.append(formatTime((cost - totalChocolate) / totalCps)));
			return true;
		}

		private boolean addPrestigeTimerToLore(List<Text> lines) {
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

		private boolean addTimeTowerStatsToLore(List<Text> lines) {
			if (totalCps < 0.0 || totalCpsMultiplier < 0.0 || timeTowerMultiplier < 0.0) return false;
			lines.add(Text.literal("Current stats:").formatted(Formatting.GRAY));
			lines.add(Text.empty()
					.append(Text.literal("  CPS increase: ").formatted(Formatting.GRAY))
					.append(Text.literal(DECIMAL_FORMAT.format(totalCps / totalCpsMultiplier * timeTowerMultiplier)).formatted(Formatting.GOLD)));
			lines.add(Text.empty()
					.append(Text.literal("  CPS when active: ").formatted(Formatting.GRAY))
					.append(Text.literal(DECIMAL_FORMAT.format(isTimeTowerActive ? totalCps : totalCps / totalCpsMultiplier * (timeTowerMultiplier + totalCpsMultiplier))).formatted(Formatting.GOLD)));
			if (!isTimeTowerMaxed) {
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

		private boolean addRabbitStatsToLore(List<Text> lines, int slot) {
			if (cpsIncreaseFactors.isEmpty()) return false;
			for (Rabbit rabbit : cpsIncreaseFactors) {
				if (rabbit.slot == slot) {
					lines.add(Text.empty()
							.append(Text.literal("CPS Increase: ").formatted(Formatting.GRAY))
							.append(Text.literal(DECIMAL_FORMAT.format(rabbit.cpsIncrease)).formatted(Formatting.GOLD)));

					lines.add(Text.empty()
							.append(Text.literal("Cost per CPS: ").formatted(Formatting.GRAY))
							.append(Text.literal(DECIMAL_FORMAT.format(rabbit.cost / rabbit.cpsIncrease)).formatted(Formatting.GOLD)));

					if (rabbit.slot == bestUpgrade) {
						if (rabbit.cost <= totalChocolate) {
							lines.add(Text.literal("Best upgrade").formatted(Formatting.GREEN));
						} else {
							lines.add(Text.literal("Best upgrade, can't afford").formatted(Formatting.YELLOW));
						}
					} else if (rabbit.slot == bestAffordableUpgrade && rabbit.cost <= totalChocolate) {
						lines.add(Text.literal("Best upgrade you can afford").formatted(Formatting.GREEN));
					}
					return true;
				}
			}
			return false;
		}

		private MutableText formatTime(double seconds) {
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
	}
}
