package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.mixins.accessors.HandledScreenAccessor;
import de.hysky.skyblocker.mixins.accessors.ScreenAccessor;
import de.hysky.skyblocker.skyblock.crimson.CrimsonFaction;
import de.hysky.skyblocker.skyblock.crimson.kuudra.Kuudra;
import de.hysky.skyblocker.skyblock.crimson.kuudra.KuudraProfileData;
import de.hysky.skyblocker.skyblock.hunting.Attribute;
import de.hysky.skyblocker.skyblock.hunting.Attributes;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RegexUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChestValue {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChestValue.class);
	private static final Set<String> DUNGEON_CHESTS = Set.of("Wood Chest", "Gold Chest", "Diamond Chest", "Emerald Chest", "Obsidian Chest", "Bedrock Chest");
	public static final Pattern DUNGEON_CHEST_COIN_COST_PATTERN = Pattern.compile("^([0-9,]+) Coins$");
	// Hypixel does include the word "Chest" twice in the screen titles (:
	private static final Set<String> KUUDRA_CHESTS = Set.of("Free Chest", "Free Chest Chest", "Paid Chest", "Paid Chest Chest");
	private static final Map<String, String> KUUDRA_KEYS = Map.of(
			"Kuudra Key", "KUUDRA_TIER_KEY",
			"Hot Kuudra Key", "KUUDRA_HOT_TIER_KEY",
			"Burning Kuudra Key", "KUUDRA_BURNING_TIER_KEY",
			"Fiery Kuudra Key", "KUUDRA_FIERY_TIER_KEY",
			"Infernal Kuudra Key", "KUUDRA_INFERNAL_TIER_KEY"
			);
	/**
	 * Pattern to match the essence count from Croesus tooltips or the chest menus.
	 *
	 * Note: Essence within the Croesus tooltip won't list the amount if you only got one essence.
	 */
	public static final Pattern ESSENCE_PATTERN = Pattern.compile("(?<type>[A-Za-z]+) Essence(?: x(?<amount>\\d+))?");
	/**
	 * Pattern to match shards from the Croesus tooltips and in the chest menus.
	 *
	 * Note: Shards within the Croesus tooltip won't list the amount if you only got one shard.
	 */
	public static final Pattern SHARD_PATTERN = Pattern.compile("[A-Za-z ]+ Shard(?: x(?<amount>\\d+))?");
	/** Pattern to match Kuudra Teeth. Only needed for Croesus profit. */
	public static final Pattern KUUDRA_TEETH_PATTERN = Pattern.compile("Kuudra Teeth(?: x(?<amount>\\d+))?");
	/** Pattern to match Heavy Pearls. Only needed for Croesus profit. */
	public static final Pattern HEAVY_PEARL_PATTERN = Pattern.compile("Heavy Pearl(?: x(?<amount>\\d+))?");
	private static final Pattern MINION_PATTERN = Pattern.compile("Minion (I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII)$");

	@Init
	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (Utils.isOnSkyblock() && screen instanceof GenericContainerScreen genericContainerScreen) {
				Text title = screen.getTitle();
				String titleString = title.getString();
				RewardChestType chestType = DUNGEON_CHESTS.contains(titleString) ? RewardChestType.DUNGEON : KUUDRA_CHESTS.contains(titleString) ? RewardChestType.KUUDRA : null;

				if (chestType != null) {
					if (SkyblockerConfigManager.get().dungeons.dungeonChestProfit.enableProfitCalculator) {
						ScreenEvents.afterTick(screen).register(ignored -> {
							Text dungeonChestProfit = getRewardChestProfit(genericContainerScreen.getScreenHandler(), chestType);
							if (dungeonChestProfit != null)
								addValueToContainer(genericContainerScreen, dungeonChestProfit, title);
						});
					}
				} else if (SkyblockerConfigManager.get().uiAndVisuals.chestValue.enableChestValue && !titleString.equals("SkyBlock Menu")) {
					ScreenType screenType = determineScreenType(titleString);
					Screens.getButtons(screen).add(ButtonWidget
							.builder(Text.literal("$"), buttonWidget -> {
								Screens.getButtons(screen).remove(buttonWidget);
								ScreenEvents.afterTick(screen).register(ignored -> {
									Text chestValue = getChestValue(genericContainerScreen.getScreenHandler(), screenType);
									if (chestValue != null) {
										addValueToContainer(genericContainerScreen, chestValue, title);
									}
								});
							})
							.dimensions(((HandledScreenAccessor) genericContainerScreen).getX() + ((HandledScreenAccessor) genericContainerScreen).getBackgroundWidth() - 16, ((HandledScreenAccessor) genericContainerScreen).getY() + 4, 12, 12)
							.tooltip(Tooltip.of(getButtonTooltipText(screenType)))
							.build()
					);
				}
			}
		});
	}

	private static @Nullable Text getRewardChestProfit(GenericContainerScreenHandler handler, RewardChestType chestType) {
		try {
			double profit = 0;
			boolean hasIncompleteData = false, usedKismet = false;
			List<Slot> slots = handler.slots.subList(0, handler.getRows() * 9);

			//If the item stack for the "Open Reward Chest" button or the Kismet button hasn't been sent to the client yet
			if (slots.get(31).getStack().isEmpty() || slots.get(50).getStack().isEmpty()) {
				return null;
			}

			for (Slot slot : slots) {
				ItemStack stack = slot.getStack();
				if (stack.isEmpty()) {
					continue;
				}

				String name = stack.getName().getString();
				String skyblockApiId = stack.getSkyblockApiId();

				//Regular item price
				// Implicitly excludes the "Reroll Shard" item in Kuudra chests which is a Wheel of Fate from the profit calculation
				if (!skyblockApiId.isEmpty() && !(name.contains("Essence") || name.contains("Shard"))) {
					DoubleBooleanPair priceData = ItemUtils.getItemPrice(skyblockApiId);

					//Add the item price to the profit
					profit += priceData.leftDouble() * stack.getCount();
					hasIncompleteData |= !priceData.rightBoolean();

					continue;
				}

				//Essence price
				if (name.contains("Essence") && SkyblockerConfigManager.get().dungeons.dungeonChestProfit.includeEssence) {
					Matcher matcher = ESSENCE_PATTERN.matcher(name);

					if (matcher.matches()) {
						String type = matcher.group("type").toUpperCase(Locale.ENGLISH);
						// Defaults to 1 due to the comment about the regex
						int amount = RegexUtils.parseOptionalIntFromMatcher(matcher, "amount").orElse(1);
						DoubleBooleanPair priceData = ItemUtils.getItemPrice("ESSENCE_" + type);

						// Apply Kuudra Pet bonus
						if (type.equals("CRIMSON")) {
							amount *= computeCrimsonEssenceMultiplier();
						}

						//Add the price of the essence to the profit
						profit += priceData.leftDouble() * amount;
						hasIncompleteData |= !priceData.rightBoolean();

						continue;
					}
				}

				// Shard Prices
				// Excludes the "Reroll Shard" button which uses a Wheel of Fate as its icon in Kuudra chests
				if (name.contains("Shard") && !name.contains("Reroll")) {
					Matcher matcher = SHARD_PATTERN.matcher(name);

					if (matcher.matches()) {
						// June 2025: I do not believe it is possible to get more than 1 in a single chest but in the interest of future-proofing we will handle it anyway
						// Nov 2025: You can now get multiple shards in a single chest
						int shards = RegexUtils.parseOptionalIntFromMatcher(matcher, "amount").orElse(1);
						Attribute attribute = Attributes.getAttributeFromItemName(stack);
						if (attribute == null) {
							LOGGER.warn("[Skyblocker Profit Calculator] Encountered unknown shard {}", name);
							continue;
						}
						String shardApiId = attribute.apiId();
						DoubleBooleanPair priceData = ItemUtils.getItemPrice(shardApiId);

						//Add the price of the shard to the profit
						profit += priceData.leftDouble() * shards;
						hasIncompleteData |= !priceData.rightBoolean();

						continue;
					}
				}

				// Determine the cost of the chest:
				if (name.contains("Open Reward Chest")) {
					switch (chestType) {
						// If not found (wood chest or already opened chest), it will be 0
						case DUNGEON -> {
							Matcher matcher = ItemUtils.getLoreLineIfContainsMatch(stack, DUNGEON_CHEST_COIN_COST_PATTERN);
							if (matcher == null) continue;
							String foundString = matcher.group(1).replaceAll("\\D", "");
							if (!NumberUtils.isCreatable(foundString)) continue;
							profit -= Integer.parseInt(foundString);
						}

						case KUUDRA -> {
							String key = ItemUtils.getLoreLineIf(stack, KUUDRA_KEYS::containsKey);
							if (key == null) continue;
							DoubleBooleanPair keyCost = computeKuudraKeyPrice(key);

							profit -= keyCost.leftDouble();
							hasIncompleteData |= !keyCost.rightBoolean();
						}
					}

					continue;
				}

				//Determine if a kismet was used or not
				if (name.contains("Reroll Chest")) {
					usedKismet = !StringUtils.isBlank(searchLoreFor(stack, "You already rerolled a chest!"));
				}
			}

			if (SkyblockerConfigManager.get().dungeons.dungeonChestProfit.includeKismet && usedKismet) {
				DoubleBooleanPair kismetPriceData = ItemUtils.getItemPrice("KISMET_FEATHER");

				profit -= kismetPriceData.leftDouble();
				hasIncompleteData |= !kismetPriceData.rightBoolean();
			}

			return getProfitText((long) profit, hasIncompleteData);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Profit Calculator] Failed to calculate reward chest profit for {}! ", chestType, e);
		}

		return null;
	}

	/**
	 * The Kuudra pet has a passive always enabled perk that grants bonus crimson essence.
	 */
	public static float computeCrimsonEssenceMultiplier() {
		PetInfo kuudraPet = Kuudra.getKuudraProfileData().kuudraPet();
		float percentBonus = switch (kuudraPet.tier()) {
			case SkyblockItemRarity.COMMON -> 10f;
			case SkyblockItemRarity.UNCOMMON, SkyblockItemRarity.RARE -> 15f;
			case SkyblockItemRarity.EPIC, SkyblockItemRarity.LEGENDARY -> 20f;
			default -> 10f;
		} * (kuudraPet.level() / 100f);
		float multiplier = (percentBonus / 100f) + 1f;

		return multiplier;
	}

	public static DoubleBooleanPair computeKuudraKeyPrice(String kuudraKeyName) {
		// The cost of Kuudra Keys involves a base cost in coins (which varies depending on the player), an amount of ingredients
		// in either Enchanted Mycelium or Enchanted Red Sand (said amount being the same regardless of which ingredient is wanted), and 2 Nether Stars.
		String kuudraKeyId = KUUDRA_KEYS.get(kuudraKeyName);

		if (kuudraKeyId == null) {
			throw new IllegalArgumentException("Expected a Kuudra Key variant but got: " + kuudraKeyName);
		}

		int baseCost = Kuudra.getKuudraProfileData().kuudraKeyPrices().getOrDefault(kuudraKeyId, KuudraProfileData.EMPTY.kuudraKeyPrices().get(kuudraKeyId));
		String ingredient = switch (Kuudra.getKuudraProfileData().faction()) {
			case CrimsonFaction.MAGE -> "ENCHANTED_MYCELIUM";
			case CrimsonFaction.BARBARIAN -> "ENCHANTED_RED_SAND";
		};
		int ingredientAmount = switch (kuudraKeyId) {
			case "KUUDRA_TIER_KEY" -> 2;
			case "KUUDRA_HOT_TIER_KEY" -> 4;
			case "KUUDRA_BURNING_TIER_KEY" -> 16;
			case "KUUDRA_FIERY_TIER_KEY" -> 40;
			case "KUUDRA_INFERNAL_TIER_KEY" -> 80;
			// The get reward chest method checks if the lore has one of these exact values so this should never happen
			default -> throw new IllegalArgumentException("Expected a Kuudra Key variant but got: " + kuudraKeyName);
		};

		double price = 0;
		boolean hasCompleteData = true;
		DoubleBooleanPair ingredientPriceData = ItemUtils.getItemPrice(ingredient);
		DoubleBooleanPair netherStarPriceData = ItemUtils.getItemPrice("CORRUPTED_NETHER_STAR");

		price += baseCost;
		price += ingredientPriceData.leftDouble() * ingredientAmount;
		price += netherStarPriceData.leftDouble() * 2;

		hasCompleteData &= ingredientPriceData.rightBoolean();
		hasCompleteData &= netherStarPriceData.rightBoolean();

		return DoubleBooleanPair.of(price, hasCompleteData);
	}

	private static @Nullable Text getChestValue(GenericContainerScreenHandler handler, @NotNull ScreenType screenType) {
		try {
			double value = 0;
			boolean hasIncompleteData = false;

			List<Slot> slots = switch (screenType) {
				case ScreenType.MINION -> getMinionSlots(handler);
				case ScreenType.SACK -> handler.slots.subList(10, (handler.getRows() * 9) - 10); // Skip the glass pane rows so we don't have to iterate over them
				case ScreenType.STASH -> handler.slots.subList(0, (handler.getRows() - 1) * 9); // Stash uses the bottom row for the menu, so we skip it
				case ScreenType.OTHER -> handler.slots.subList(0, handler.getRows() * 9);
			};

			for (Slot slot : slots) {
				ItemStack stack = slot.getStack();
				if (stack.isEmpty()) continue;

				String coinsLine;
				if (screenType == ScreenType.MINION && slot.id == 28 && stack.isOf(Items.HOPPER) && (coinsLine = ItemUtils.getLoreLineIf(stack, s -> s.contains("Held Coins:"))) != null) {
					String source = coinsLine.split(":")[1];
					try {
						value += NumberFormat.getNumberInstance(java.util.Locale.US).parse(source.trim()).doubleValue();
					} catch (ParseException e) {
						LOGGER.warn("[Skyblocker] Failed to parse `{}`", source);
					}
					continue;
				}

				String id = stack.getSkyblockApiId();

				int count = switch (screenType) {
					case ScreenType.SACK -> {
						List<Text> lines = ItemUtils.getLore(stack);
						yield ItemUtils.getItemCountInSack(stack, lines, true).orElse(0); // If this is in a sack and the item is not a stored item, we can just skip it
					}
					case ScreenType.STASH -> ItemUtils.getItemCountInStash(stack).orElse(0);
					case ScreenType.OTHER, ScreenType.MINION -> stack.getCount();
				};

				if (count == 0) continue;

				if (!id.isEmpty()) {
					DoubleBooleanPair priceData = ItemUtils.getItemPrice(id);

					if (!priceData.rightBoolean()) hasIncompleteData = true;

					value += NetworthCalculator.getItemNetworth(stack, count).price();
				}
			}

			return getValueText((long) value, hasIncompleteData);
		} catch (Exception e) {
			LOGGER.error("[Skyblocker Value Calculator] Failed to calculate dungeon chest value! ", e);
		}

		return null;
	}

	private static @NotNull List<Slot> getMinionSlots(GenericContainerScreenHandler handler) {
		return handler.slots.subList(0, handler.getRows() * 9).stream().filter(slot -> {
			int x = slot.id % 9;
			int y = slot.id / 9;
			return x > 2 && x < 8 && y > 1 && y < 5 || slot.id == 28;
		}).toList();
	}

	/**
	 * Searches for a specific string of characters in the name and lore of an item
	 */
	private static String searchLoreFor(ItemStack stack, String searchString) {
		return ItemUtils.getLoreLineIf(stack, line -> line.contains(searchString));
	}

	static Text getProfitText(long profit, boolean hasIncompleteData) {
		return Text.literal((profit > 0 ? " +" : ' ') + Formatters.INTEGER_NUMBERS.format(profit) + " Coins").formatted(getProfitColor(hasIncompleteData, profit));
	}

	@NotNull
	static Formatting getProfitColor(boolean hasIncompleteData, long profit) {
		DungeonsConfig.DungeonChestProfit config = SkyblockerConfigManager.get().dungeons.dungeonChestProfit;
		if (hasIncompleteData) return config.incompleteColor;
		if (Math.abs(profit) < config.neutralThreshold) return config.neutralColor;
		if (profit > 0) return config.profitColor;
		return config.lossColor;
	}

	@NotNull
	static Text getValueText(long value, boolean hasIncompleteData) {
		UIAndVisualsConfig.ChestValue config = SkyblockerConfigManager.get().uiAndVisuals.chestValue;
		return Text.literal(' ' + Formatters.INTEGER_NUMBERS.format(value) + " Coins").formatted(hasIncompleteData ? config.incompleteColor : config.color);
	}

	private static void addValueToContainer(GenericContainerScreen genericContainerScreen, Text chestValue, Text title) {
		Screens.getButtons(genericContainerScreen).removeIf(ChestValueTextWidget.class::isInstance);
		int backgroundWidth = ((HandledScreenAccessor) genericContainerScreen).getBackgroundWidth();
		int y = ((HandledScreenAccessor) genericContainerScreen).getY();
		int x = ((HandledScreenAccessor) genericContainerScreen).getX();
		((ScreenAccessor) genericContainerScreen).setTitle(Text.empty());
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		int chestValueWidth = Math.min(textRenderer.getWidth(chestValue), Math.max((backgroundWidth - 8) / 2 - 2, backgroundWidth - 8 - textRenderer.getWidth(title)));

		TextWidget chestValueWidget = new ChestValueTextWidget(chestValueWidth, textRenderer.fontHeight, chestValue, textRenderer);
		chestValueWidget.setPosition(x + backgroundWidth - chestValueWidget.getWidth() - 4, y + 6);
		Screens.getButtons(genericContainerScreen).add(chestValueWidget);

		ChestValueTextWidget chestTitleWidget = new ChestValueTextWidget(backgroundWidth - 8 - chestValueWidth - 2, textRenderer.fontHeight, title.copy().fillStyle(Style.EMPTY.withColor(4210752)), textRenderer);
		chestTitleWidget.setPosition(x + 8, y + 6);
		Screens.getButtons(genericContainerScreen).add(chestTitleWidget);
	}

	@NotNull
	private static ScreenType determineScreenType(String rawTitleString) {
		if ("sack".contains(rawTitleString.toLowerCase(Locale.ENGLISH))) return ScreenType.SACK;
		if (MINION_PATTERN.matcher(rawTitleString.trim()).find()) return ScreenType.MINION;
		if ("View Stash".equalsIgnoreCase(rawTitleString)) return ScreenType.STASH;
		return ScreenType.OTHER;
	}

	@NotNull
	private static Text getButtonTooltipText(ScreenType screenType) {
		return switch (screenType) {
			case ScreenType.MINION -> Text.translatable("skyblocker.containerValue.minionValue.@Tooltip");
			case ScreenType.OTHER -> Text.translatable("skyblocker.containerValue.chestValue.@Tooltip");
			case ScreenType.STASH -> Text.translatable("skyblocker.containerValue.stashValue.@Tooltip");
			case ScreenType.SACK -> Text.translatable("skyblocker.containerValue.sackValue.@Tooltip");
		};
	}

	private static class ChestValueTextWidget extends TextWidget {
		private ChestValueTextWidget(int width, int height, Text message, TextRenderer textRenderer) {
			super(width, height, message.copy().fillStyle(Style.EMPTY.withShadowColor(0)), textRenderer);
			setMaxWidth(getWidth(), TextOverflow.SCROLLING);
		}
	}

	private enum ScreenType {
		MINION,
		SACK,
		STASH,
		OTHER
	}

	private enum RewardChestType {
		DUNGEON,
		KUUDRA
	}
}
