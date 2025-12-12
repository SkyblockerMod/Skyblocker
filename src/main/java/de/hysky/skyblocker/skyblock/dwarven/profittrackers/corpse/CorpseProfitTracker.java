package de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ItemPriceUpdateEvent;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.dwarven.CorpseType;
import de.hysky.skyblocker.skyblock.dwarven.profittrackers.AbstractProfitTracker;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class CorpseProfitTracker extends AbstractProfitTracker {
	// Items without a proper item id or price
	public static final String GLACITE_POWDER = "GLACITE_POWDER";
	public static final String OPAL_CRYSTAL = "OPAL_CRYSTAL";
	public static final String ONYX_CRYSTAL = "ONYX_CRYSTAL";
	public static final String AQUAMARINE_CRYSTAL = "AQUAMARINE_CRYSTAL";
	public static final String PERIDOT_CRYSTAL = "PERIDOT_CRYSTAL";
	public static final String CITRINE_CRYSTAL = "CITRINE_CRYSTAL";
	public static final String RUBY_CRYSTAL = "RUBY_CRYSTAL";
	public static final String JASPER_CRYSTAL = "JASPER_CRYSTAL";
	public static final String ENCHANTMENT_ICE_COLD_1 = "ENCHANTMENT_ICE_COLD_1";	// fix for item repo
	public static final @Unmodifiable List<String> PRICELESS_ITEMS = List.of(GLACITE_POWDER, OPAL_CRYSTAL, ONYX_CRYSTAL, AQUAMARINE_CRYSTAL, PERIDOT_CRYSTAL, CITRINE_CRYSTAL, RUBY_CRYSTAL, JASPER_CRYSTAL);
	// English translation for that forceEnglishCorpseProfitTracker option
	public static final String CORPSE_PROFIT_MESSAGE = "Corpse Profit: %s";

	public static final CorpseProfitTracker INSTANCE = new CorpseProfitTracker();

	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Corpse Profit Tracker");
	private static final Pattern CORPSE_PATTERN = Pattern.compile(" {2}(LAPIS|UMBER|TUNGSTEN|VANGUARD) CORPSE LOOT! *");
	private static final Object2ObjectArrayMap<String, String> NAME2ID_MAP = new Object2ObjectArrayMap<>(50);

	private ObjectArrayList<CorpseLoot> currentProfileRewards = new ObjectArrayList<>();
	private final ProfiledData<ObjectArrayList<CorpseLoot>> allRewards = new ProfiledData<>(getRewardFilePath("corpse-profits.json"), CorpseLoot.CODEC.listOf().xmap(ObjectArrayList::new, Function.identity()));
	private boolean insideRewardMessage = false;
	private @Nullable CorpseLoot lastCorpseLoot = null;

	private CorpseProfitTracker() {} // Singleton

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(INSTANCE::onChatMessage);

		INSTANCE.allRewards.init();

		SkyblockEvents.PROFILE_CHANGE.register(INSTANCE::onProfileChange);

		// @formatter:off // Don't you hate it when your format style for chained method calls makes a chain like this incredibly ugly?
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
			literal(SkyblockerMod.NAMESPACE)
				.then(literal("rewardTrackers")
					.then(literal("corpse")
						.then(literal("list")
							// Optional argument.
							.then(argument("summaryView", BoolArgumentType.bool())
								.executes(ctx -> {
									Scheduler.queueOpenScreen(new CorpseProfitScreen(ctx.getSource().getClient().currentScreen, BoolArgumentType.getBool(ctx, "summaryView")));
									return Command.SINGLE_SUCCESS;
								})
							)
							.executes(ctx -> {
								Scheduler.queueOpenScreen(new CorpseProfitScreen(ctx.getSource().getClient().currentScreen));
								return Command.SINGLE_SUCCESS;
							})
						)
						.then(literal("reset")
							.executes(ctx -> {
								INSTANCE.currentProfileRewards.clear();
								INSTANCE.allRewards.save();
								ctx.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.corpseTracker.historyReset").formatted(Formatting.GREEN)));
								return Command.SINGLE_SUCCESS;
							})
						)
					)
				)
		)); // @formatter:on
		ItemPriceUpdateEvent.ON_PRICE_UPDATE.register(INSTANCE::recalculateAll);
	}

	private void onProfileChange(String prevProfileId, String newProfileId) {
		if (!isEnabled()) return;
		currentProfileRewards = allRewards.computeIfAbsent(ObjectArrayList::new);
		recalculateAll();
	}

	public boolean isEnabled() {
		return SkyblockerConfigManager.get().mining.glacite.enableCorpseProfitTracker;
	}

	@SuppressWarnings("SameReturnValue")
	private boolean onChatMessage(Text text, boolean overlay) {
		if (Utils.getLocation() != Location.GLACITE_MINESHAFTS || !INSTANCE.isEnabled() || overlay) return true;
		String message = text.getString();

		// Reward messages end with a separator like so
		if (insideRewardMessage && message.equals("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) {
			if (lastCorpseLoot == null) {
				LOGGER.error("Received a reward message end without a corresponding start. Report this!");
				return true;
			}
			currentProfileRewards.add(lastCorpseLoot);
			if (!lastCorpseLoot.isPriceDataComplete()) {
				MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
						Constants.PREFIX.get().append(Text.translatable("skyblocker.corpseTracker.somethingWentWrong").formatted(Formatting.GOLD))
				);
			} else {	// if forceEnglishCorpseProfitTracker is FALSE - use normal translation
				if (!SkyblockerConfigManager.get().mining.glacite.forceEnglishCorpseProfitTracker) {
					MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
							Constants.PREFIX.get()
									.append(Text.translatable("skyblocker.corpseTracker.corpseProfit", Text.literal(Formatters.INTEGER_NUMBERS.format(lastCorpseLoot.profit()))
											.formatted(lastCorpseLoot.profit() > 0 ? Formatting.GREEN : Formatting.RED)))
									.styled(style ->
											style.withHoverEvent(new HoverEvent.ShowText(Text.translatable("skyblocker.corpseTracker.hoverText").formatted(Formatting.GREEN)))
													.withClickEvent(new ClickEvent.RunCommand("/skyblocker rewardTrackers corpse list false"))
									)
					);
				} else {	// else, if forceEnglishCorpseProfitTracker is TRUE - force English translation
					MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
							Constants.PREFIX.get()
									.append(Text.literal(String.format(CORPSE_PROFIT_MESSAGE, Formatters.INTEGER_NUMBERS.format(lastCorpseLoot.profit())))
											.formatted(lastCorpseLoot.profit() > 0 ? Formatting.GREEN : Formatting.RED))
									.styled(style ->
											style.withHoverEvent(new HoverEvent.ShowText(Text.translatable("skyblocker.corpseTracker.hoverText").formatted(Formatting.GREEN)))
													.withClickEvent(new ClickEvent.RunCommand("/skyblocker rewardTrackers corpse list false"))
									)
					);
				}
			}
			lastCorpseLoot = null;
			insideRewardMessage = false;
			return true;
		}
		Matcher matcher = CORPSE_PATTERN.matcher(message);
		if (!insideRewardMessage && matcher.matches()) {
			String corpse = matcher.group(1);
			CorpseType type;
			try {
				type = CorpseType.valueOf(corpse.toUpperCase(Locale.ENGLISH)); // toUpperCase is not strictly necessary here, but it's good practice
				lastCorpseLoot = new CorpseLoot(
						type,
						new ObjectArrayList<>(),
						Instant.now()
				);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Unknown corpse type `{}` for message: `{}`. Report this!", corpse, message);
				return true;
			}

			try {
				lastCorpseLoot.profit(lastCorpseLoot.profit() - type.getKeyPrice()); //Negated since the key price is a cost, not a reward
			} catch (IllegalStateException e) { // This is thrown when the key price is not found
				LOGGER.warn("No key price found for corpse type `{}`. Profit calculation will not be accurate, therefore it will not be sent to chat. It will still be added to the corpse history.", corpse);
				lastCorpseLoot.markPriceDataIncomplete();
			}
			insideRewardMessage = true;
			return true;
		}

		if (!insideRewardMessage || lastCorpseLoot == null || !matcher.usePattern(REWARD_PATTERN).matches()) return true;

		String itemName = matcher.group(1);
		int amount = NumberUtils.toInt(matcher.group(2).replace(",", ""), 1);
		if (matcher.usePattern(HOTM_XP_PATTERN).matches()) return true; // Ignore HOTM XP messages.
		lastCorpseLoot.addLoot(itemName, amount);
		return true;
	}

	private void recalculateAll() {
		for (CorpseLoot corpseLoot : currentProfileRewards) {
			corpseLoot.profit(0);
			corpseLoot.markPriceDataComplete(); // Reset the flag
			for (Reward reward : corpseLoot.rewards()) {
				if (PRICELESS_ITEMS.contains(reward.itemId())) continue;

				DoubleBooleanPair price = ItemUtils.getItemPrice(reward.itemId());
				if (!price.rightBoolean()) {
					LOGGER.warn("No price found for item `{}`.", reward.itemId());
					corpseLoot.markPriceDataIncomplete();
					continue;
				}
				corpseLoot.profit(corpseLoot.profit() + price.leftDouble() * reward.amount());
				reward.pricePerUnit(price.leftDouble());
			}
			try {
				corpseLoot.profit(corpseLoot.profit() - corpseLoot.corpseType().getKeyPrice());
			} catch (IllegalStateException e) {
				LOGGER.warn("No key price found for corpse type `{}`. Profit calculation will not be accurate.", corpseLoot.corpseType());
				corpseLoot.markPriceDataIncomplete();
			}
		}
	}

	@UnmodifiableView
	public static List<CorpseLoot> getCurrentProfileRewards() {
		return ObjectLists.unmodifiable(INSTANCE.currentProfileRewards);
	}

	@UnmodifiableView
	public static Object2ObjectMap<String, String> getName2IdMap() {
		return Object2ObjectMaps.unmodifiable(NAME2ID_MAP);
	}

	// TODO: Perhaps make a little something in the skyblocker-assets repo for this in case it needs updating in the future
	static {
		NAME2ID_MAP.put("☠ Flawed Onyx Gemstone", "FLAWED_ONYX_GEM");
		NAME2ID_MAP.put("☠ Fine Onyx Gemstone", "FINE_ONYX_GEM");
		NAME2ID_MAP.put("☠ Flawless Onyx Gemstone", "FLAWLESS_ONYX_GEM");

		NAME2ID_MAP.put("☘ Flawed Peridot Gemstone", "FLAWED_PERIDOT_GEM");
		NAME2ID_MAP.put("☘ Fine Peridot Gemstone", "FINE_PERIDOT_GEM");
		NAME2ID_MAP.put("☘ Flawless Peridot Gemstone", "FLAWLESS_PERIDOT_GEM");

		NAME2ID_MAP.put("☘ Flawed Citrine Gemstone", "FLAWED_CITRINE_GEM");
		NAME2ID_MAP.put("☘ Fine Citrine Gemstone", "FINE_CITRINE_GEM");
		NAME2ID_MAP.put("☘ Flawless Citrine Gemstone", "FLAWLESS_CITRINE_GEM");

		NAME2ID_MAP.put("α Flawed Aquamarine Gemstone", "FLAWED_AQUAMARINE_GEM");
		NAME2ID_MAP.put("α Fine Aquamarine Gemstone", "FINE_AQUAMARINE_GEM");
		NAME2ID_MAP.put("α Flawless Aquamarine Gemstone", "FLAWLESS_AQUAMARINE_GEM");

		NAME2ID_MAP.put("Goblin Egg", "GOBLIN_EGG");
		NAME2ID_MAP.put("Green Goblin Egg", "GOBLIN_EGG_GREEN");
		NAME2ID_MAP.put("Blue Goblin Egg", "GOBLIN_EGG_BLUE");
		NAME2ID_MAP.put("Red Goblin Egg", "GOBLIN_EGG_RED");
		NAME2ID_MAP.put("Yellow Goblin Egg", "GOBLIN_EGG_YELLOW");

		NAME2ID_MAP.put("Enchanted Glacite", "ENCHANTED_GLACITE");
		NAME2ID_MAP.put("Enchanted Umber", "ENCHANTED_UMBER");
		NAME2ID_MAP.put("Enchanted Tungsten", "ENCHANTED_TUNGSTEN");

		NAME2ID_MAP.put("Refined Umber", "REFINED_UMBER");
		NAME2ID_MAP.put("Refined Tungsten", "REFINED_TUNGSTEN");
		NAME2ID_MAP.put("Refined Mithril", "REFINED_MITHRIL");
		NAME2ID_MAP.put("Refined Titanium", "REFINED_TITANIUM");

		NAME2ID_MAP.put("Umber Plate", "UMBER_PLATE");
		NAME2ID_MAP.put("Tungsten Plate", "TUNGSTEN_PLATE");

		NAME2ID_MAP.put("Glacite Amalgamation", "GLACITE_AMALGAMATION");
		NAME2ID_MAP.put("Bejeweled Handle", "BEJEWELED_HANDLE");
		NAME2ID_MAP.put("Umber Key", "UMBER_KEY");
		NAME2ID_MAP.put("Tungsten Key", "TUNGSTEN_KEY");
		NAME2ID_MAP.put("Glacite Jewel", "GLACITE_JEWEL");
		NAME2ID_MAP.put("Suspicious Scrap", "SUSPICIOUS_SCRAP");
		NAME2ID_MAP.put("Enchanted Book (Ice Cold I)", "ENCHANTMENT_ICE_COLD_1");
		NAME2ID_MAP.put("Dwarven O's Metallic Minis", "DWARVEN_OS_METALLIC_MINIS");
		NAME2ID_MAP.put("Shattered Locket", "SHATTERED_PENDANT");
		NAME2ID_MAP.put("Frozen Scute", "FROZEN_SCUTE");

		NAME2ID_MAP.put("Frostbitten Dye", "DYE_FROSTBITTEN");

		//These don't have an associated item id or price, but they are in the map regardless so we know what items are not properly mapped and log them accordingly
		NAME2ID_MAP.put("Opal Crystal", OPAL_CRYSTAL);
		NAME2ID_MAP.put("Onyx Crystal", ONYX_CRYSTAL);
		NAME2ID_MAP.put("Aquamarine Crystal", AQUAMARINE_CRYSTAL);
		NAME2ID_MAP.put("Peridot Crystal", PERIDOT_CRYSTAL);
		NAME2ID_MAP.put("Citrine Crystal", CITRINE_CRYSTAL);
		NAME2ID_MAP.put("Ruby Crystal", RUBY_CRYSTAL);
		NAME2ID_MAP.put("Jasper Crystal", JASPER_CRYSTAL);
		NAME2ID_MAP.put("Glacite Powder", GLACITE_POWDER);
	}
}
