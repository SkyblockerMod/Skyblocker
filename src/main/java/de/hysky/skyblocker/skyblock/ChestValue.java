package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.mixins.accessors.AbstractContainerScreenAccessor;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;
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
	 * <p>Note: Essence within the Croesus tooltip won't list the amount if you only got one essence.
	 */
	public static final Pattern ESSENCE_PATTERN = Pattern.compile("(?<type>[A-Za-z]+) Essence(?: x(?<amount>\\d+))?");
	/**
	 * Pattern to match shards from the Croesus tooltips and in the chest menus.
	 *
	 * <p>Note: Shards within the Croesus tooltip won't list the amount if you only got one shard.
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
			if (Utils.isOnSkyblock() && screen instanceof ContainerScreen genericContainerScreen) {
				Component title = screen.getTitle();
				String titleString = title.getString();
				RewardChestType chestType = DUNGEON_CHESTS.contains(titleString) ? RewardChestType.DUNGEON : KUUDRA_CHESTS.contains(titleString) ? RewardChestType.KUUDRA : null;

				if (chestType != null) {
					if (SkyblockerConfigManager.get().dungeons.dungeonChestProfit.enableProfitCalculator) {
						ScreenEvents.afterTick(screen).register(ignored -> {
							Component dungeonChestProfit = getRewardChestProfit(genericContainerScreen.getMenu(), chestType);
							if (dungeonChestProfit != null)
								addValueToContainer(genericContainerScreen, dungeonChestProfit, title);
						});
					}
				} else if (SkyblockerConfigManager.get().uiAndVisuals.chestValue.enableChestValue && !titleString.equals("SkyBlock Menu")) {
					ScreenType screenType = determineScreenType(titleString);
					Screens.getButtons(screen).add(Button
							.builder(Component.literal("$"), buttonWidget -> {
								Screens.getButtons(screen).remove(buttonWidget);
								ScreenEvents.afterTick(screen).register(ignored -> {
									Component chestValue = getChestValue(genericContainerScreen.getMenu(), screenType);
									if (chestValue != null) {
										addValueToContainer(genericContainerScreen, chestValue, title);
									}
								});
							})
							.bounds(((AbstractContainerScreenAccessor) genericContainerScreen).getX() + ((AbstractContainerScreenAccessor) genericContainerScreen).getImageWidth() - 16, ((AbstractContainerScreenAccessor) genericContainerScreen).getY() + 4, 12, 12)
							.tooltip(Tooltip.create(getButtonTooltipText(screenType)))
							.build()
					);
				}
			}
		});
	}

	private static @Nullable Component getRewardChestProfit(ChestMenu handler, RewardChestType chestType) {
		try {
			double profit = 0;
			boolean hasIncompleteData = false, usedKismet = false;
			List<Slot> slots = handler.slots.subList(0, handler.getRowCount() * 9);

			//If the item stack for the "Open Reward Chest" button or the Kismet button hasn't been sent to the client yet
			if (slots.get(31).getItem().isEmpty() || slots.get(50).getItem().isEmpty()) {
				return null;
			}

			for (Slot slot : slots) {
				ItemStack stack = slot.getItem();
				if (stack.isEmpty()) {
					continue;
				}

				String name = stack.getHoverName().getString();
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
						DoubleBooleanPair priceData = ItemUtils.getItemPrice(skyblockApiId);

						// Apply Kuudra Pet bonus
						if (type.equals("CRIMSON")) {
							amount = (int) (amount * computeCrimsonEssenceMultiplier());
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
					usedKismet = !StringUtils.isBlank(ItemUtils.getLoreLineContains(stack, "You already rerolled a chest!"));
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

		return percentBonus / 100f + 1f;
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

	private static @Nullable Component getChestValue(ChestMenu handler, ScreenType screenType) {
		try {
			double value = 0;
			boolean hasIncompleteData = false;

			List<Slot> slots = switch (screenType) {
				case ScreenType.MINION -> getMinionSlots(handler);
				case ScreenType.SACK -> handler.slots.subList(10, (handler.getRowCount() * 9) - 10); // Skip the glass pane rows so we don't have to iterate over them
				case ScreenType.STASH -> handler.slots.subList(0, (handler.getRowCount() - 1) * 9); // Stash uses the bottom row for the menu, so we skip it
				case ScreenType.OTHER -> handler.slots.subList(0, handler.getRowCount() * 9);
			};

			for (Slot slot : slots) {
				ItemStack stack = slot.getItem();
				if (stack.isEmpty()) continue;

				String coinsLine;
				if (screenType == ScreenType.MINION && slot.index == 28 && stack.is(Items.HOPPER) && (coinsLine = ItemUtils.getLoreLineIf(stack, s -> s.contains("Held Coins:"))) != null) {
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
						List<String> lines = stack.skyblocker$getLoreStrings();
						yield ItemUtils.getItemCountInSack(stack, lines).orElse(0); // If this is in a sack and the item is not a stored item, we can just skip it
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

	private static List<Slot> getMinionSlots(ChestMenu handler) {
		return handler.slots.subList(0, handler.getRowCount() * 9).stream().filter(slot -> {
			int x = slot.index % 9;
			int y = slot.index / 9;
			return x > 2 && x < 8 && y > 1 && y < 5 || slot.index == 28;
		}).toList();
	}

	static Component getProfitText(long profit, boolean hasIncompleteData) {
		return Component.literal((profit > 0 ? " +" : ' ') + Formatters.INTEGER_NUMBERS.format(profit) + " Coins").withStyle(getProfitColor(hasIncompleteData, profit));
	}

	static ChatFormatting getProfitColor(boolean hasIncompleteData, long profit) {
		DungeonsConfig.DungeonChestProfit config = SkyblockerConfigManager.get().dungeons.dungeonChestProfit;
		if (hasIncompleteData) return config.incompleteColor;
		if (Math.abs(profit) < config.neutralThreshold) return config.neutralColor;
		if (profit > 0) return config.profitColor;
		return config.lossColor;
	}

	static Component getValueText(long value, boolean hasIncompleteData) {
		UIAndVisualsConfig.ChestValue config = SkyblockerConfigManager.get().uiAndVisuals.chestValue;
		return Component.literal(' ' + Formatters.INTEGER_NUMBERS.format(value) + " Coins").withStyle(hasIncompleteData ? config.incompleteColor : config.color);
	}

	private static void addValueToContainer(ContainerScreen genericContainerScreen, Component chestValue, Component title) {
		Screens.getButtons(genericContainerScreen).removeIf(ChestValueTextWidget.class::isInstance);
		int backgroundWidth = ((AbstractContainerScreenAccessor) genericContainerScreen).getImageWidth();
		int y = ((AbstractContainerScreenAccessor) genericContainerScreen).getY();
		int x = ((AbstractContainerScreenAccessor) genericContainerScreen).getX();
		((ScreenAccessor) genericContainerScreen).setTitle(Component.empty());
		Font textRenderer = Minecraft.getInstance().font;
		int chestValueWidth = Math.min(textRenderer.width(chestValue), Math.max((backgroundWidth - 8) / 2 - 2, backgroundWidth - 8 - textRenderer.width(title)));

		StringWidget chestValueWidget = new ChestValueTextWidget(chestValueWidth, textRenderer.lineHeight, chestValue, textRenderer);
		chestValueWidget.setPosition(x + backgroundWidth - chestValueWidget.getWidth() - 4, y + 6);
		Screens.getButtons(genericContainerScreen).add(chestValueWidget);

		ChestValueTextWidget chestTitleWidget = new ChestValueTextWidget(backgroundWidth - 8 - chestValueWidth - 2, textRenderer.lineHeight, title.copy().withStyle(Style.EMPTY.withColor(4210752)), textRenderer);
		chestTitleWidget.setPosition(x + 8, y + 6);
		Screens.getButtons(genericContainerScreen).add(chestTitleWidget);
	}

	private static ScreenType determineScreenType(String rawTitleString) {
		if (rawTitleString.toLowerCase(Locale.ENGLISH).endsWith("sack")) return ScreenType.SACK;
		if (MINION_PATTERN.matcher(rawTitleString.trim()).find()) return ScreenType.MINION;
		if ("View Stash".equalsIgnoreCase(rawTitleString)) return ScreenType.STASH;
		return ScreenType.OTHER;
	}

	private static Component getButtonTooltipText(ScreenType screenType) {
		return switch (screenType) {
			case ScreenType.MINION -> Component.translatable("skyblocker.containerValue.minionValue.@Tooltip");
			case ScreenType.OTHER -> Component.translatable("skyblocker.containerValue.chestValue.@Tooltip");
			case ScreenType.STASH -> Component.translatable("skyblocker.containerValue.stashValue.@Tooltip");
			case ScreenType.SACK -> Component.translatable("skyblocker.containerValue.sackValue.@Tooltip");
		};
	}

	private static class ChestValueTextWidget extends StringWidget {
		private ChestValueTextWidget(int width, int height, Component message, Font textRenderer) {
			super(width, height, message.copy().withStyle(Style.EMPTY.withShadowColor(0)), textRenderer);
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
