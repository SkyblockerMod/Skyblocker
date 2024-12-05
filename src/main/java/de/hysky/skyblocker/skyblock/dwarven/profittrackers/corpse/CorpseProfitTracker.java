package de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse;

import com.mojang.brigadier.Command;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ChatEvents;
import de.hysky.skyblocker.events.ItemPriceUpdateEvent;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.dwarven.CorpseType;
import de.hysky.skyblocker.skyblock.dwarven.profittrackers.AbstractProfitTracker;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.profile.ProfiledData;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public static final List<String> PRICELESS_ITEMS = List.of(GLACITE_POWDER, OPAL_CRYSTAL, ONYX_CRYSTAL, AQUAMARINE_CRYSTAL, PERIDOT_CRYSTAL, CITRINE_CRYSTAL, RUBY_CRYSTAL, JASPER_CRYSTAL);

	public static final CorpseProfitTracker INSTANCE = new CorpseProfitTracker();

	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Corpse Profit Tracker");
	private static final Pattern CORPSE_PATTERN = Pattern.compile(" {2}(LAPIS|UMBER|TUNGSTEN|VANGUARD) CORPSE LOOT! *");
	private static final Pattern HOTM_XP_PATTERN = Pattern.compile(" {4}\\+[\\d,]+ HOTM Experience");
	private static final Object2ObjectArrayMap<String, String> NAME2ID_MAP = new Object2ObjectArrayMap<>(50);

	private ObjectArrayList<CorpseLoot> currentProfileRewards = new ObjectArrayList<>();
	private final ProfiledData<ObjectArrayList<CorpseLoot>> allRewards = new ProfiledData<>(getRewardFilePath("corpse-profits.json"), CorpseLoot.CODEC.listOf().xmap(ObjectArrayList::new, Function.identity()));
	private boolean insideRewardMessage = false;
	@Nullable
	private CorpseLoot lastCorpseLoot = null;

	private CorpseProfitTracker() {} // Singleton

	@Init
	public static void init() {
		ChatEvents.RECEIVE_STRING.register(INSTANCE::onChatMessage);

		INSTANCE.allRewards.init();

		SkyblockEvents.PROFILE_INIT.register(INSTANCE::onProfileInit);
		SkyblockEvents.PROFILE_CHANGE.register(INSTANCE::onProfileChange);

		// @formatter:off // Don't you hate it when your format style for chained method calls makes a chain like this incredibly ugly?
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(
			literal(SkyblockerMod.NAMESPACE)
				.then(literal("rewardTrackers")
					.then(literal("corpse")
						.then(literal("list")
							.executes(ctx -> {
								Scheduler.queueOpenScreen(new CorpseProfitScreen(ctx.getSource().getClient().currentScreen));
								return Command.SINGLE_SUCCESS;
							})
						)
						.then(literal("reset")
							.executes(ctx -> {
								INSTANCE.currentProfileRewards.clear();
								INSTANCE.allRewards.save();
								ctx.getSource().sendFeedback(Constants.PREFIX.get().append(Text.literal("Corpse profit history has been reset for the current profile.").formatted(Formatting.GREEN)));
								return Command.SINGLE_SUCCESS;
							})
						)
					)
				)
		)); // @formatter:on
		ItemPriceUpdateEvent.ON_PRICE_UPDATE.register(INSTANCE::recalculateAll);
	}

	private void onProfileChange(String prevProfileId, String newProfileId) {
		onProfileInit(newProfileId);
	}

	private void onProfileInit(String profileId) {
		if (!isEnabled()) return;
		currentProfileRewards = allRewards.computeIfAbsent(ObjectArrayList::new);
		recalculateAll();
	}

	public boolean isEnabled() {
		return SkyblockerConfigManager.get().mining.glacite.enableCorpseProfitTracker;
	}

	private void onChatMessage(String message) {
		if (Utils.getLocation() != Location.GLACITE_MINESHAFT || !INSTANCE.isEnabled()) return;
		// Reward messages end with a separator like so
		if (insideRewardMessage && message.equals("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) {
			if (lastCorpseLoot == null) {
				LOGGER.error("Received a reward message end without a corresponding start. Report this!");
				return;
			}
			currentProfileRewards.add(lastCorpseLoot);
			if (!lastCorpseLoot.isPriceDataComplete()) {
				MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
						Constants.PREFIX.get().append(Text.literal("Something went wrong with corpse profit calculation. Check logs for more information.").formatted(Formatting.GOLD))
				);
			} else {
				MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
						Constants.PREFIX.get()
						                .append("Corpse profit: ")
						                .append(Text.literal(String.valueOf(lastCorpseLoot.profit)).formatted(lastCorpseLoot.profit > 0 ? Formatting.GREEN : Formatting.RED))
				);
			}
			lastCorpseLoot = null;
			insideRewardMessage = false;
			return;
		}
		Matcher matcher = CORPSE_PATTERN.matcher(message);
		if (!insideRewardMessage && matcher.matches()) {
			String corpse = matcher.group(1);
			CorpseType type;
			try {
				type = CorpseType.valueOf(corpse.toUpperCase()); // toUpperCase is not strictly necessary here, but it's good practice
				lastCorpseLoot = new CorpseLoot(
						type,
						new ObjectArrayList<>(),
						Instant.now()
				);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Unknown corpse type `{}` for message: `{}`. Report this!", corpse, message);
				return;
			}

			try {
				lastCorpseLoot.profit -= type.getKeyPrice(); //Negated since the key price is a cost, not a reward
			} catch (IllegalStateException e) { // This is thrown when the key price is not found
				LOGGER.warn("No key price found for corpse type `{}`. Profit calculation will not be accurate, therefore it will not be sent to chat. It will still be added to the corpse history.", corpse);
				lastCorpseLoot.markPriceDataIncomplete();
			}
			insideRewardMessage = true;
			return;
		}

		if (!insideRewardMessage || lastCorpseLoot == null || !matcher.usePattern(REWARD_PATTERN).matches()) return;

		String itemName = matcher.group(1);
		int amount = NumberUtils.toInt(matcher.group(2).replace(",", ""), 1);
		if (matcher.usePattern(HOTM_XP_PATTERN).matches()) return; // Ignore HOTM XP messages.
		lastCorpseLoot.addLoot(itemName, amount);
	}

	private void recalculateAll() {
		for (CorpseLoot corpseLoot : currentProfileRewards) {
			corpseLoot.profit = 0;
			corpseLoot.markPriceDataComplete(); // Reset the flag
			for (Reward reward : corpseLoot.rewards()) {
				if (PRICELESS_ITEMS.contains(reward.itemId)) continue;

				DoubleBooleanPair price = ItemUtils.getItemPrice(reward.itemId);
				if (!price.rightBoolean()) {
					LOGGER.warn("No price found for item `{}`.", reward.itemId);
					corpseLoot.markPriceDataIncomplete();
					continue;
				}
				corpseLoot.profit += price.leftDouble() * reward.amount;
				reward.pricePerUnit(price.leftDouble());
			}
			try {
				corpseLoot.profit -= corpseLoot.corpseType.getKeyPrice();
			} catch (IllegalStateException e) {
				LOGGER.warn("No key price found for corpse type `{}`. Profit calculation will not be accurate.", corpseLoot.corpseType);
				corpseLoot.markPriceDataIncomplete();
			}
		}
	}

	public static final class CorpseLoot {
		public static final Codec<CorpseLoot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				CorpseType.CODEC.fieldOf("corpseType").forGetter(CorpseLoot::corpseType),
				Reward.CODEC.listOf().fieldOf("rewards").forGetter(CorpseLoot::rewards),
				Codec.LONG.xmap(Instant::ofEpochMilli, Instant::toEpochMilli).fieldOf("timestamp").forGetter(CorpseLoot::timestamp),
				Codec.DOUBLE.fieldOf("profit").forGetter(CorpseLoot::profit)
		).apply(instance, CorpseLoot::new));

		private final @NotNull CorpseType corpseType;
		private final @NotNull List<Reward> rewards;
		private final @NotNull Instant timestamp;
		private double profit;
		private boolean isPriceDataComplete = true;

		private CorpseLoot(@NotNull CorpseType corpseType, @NotNull List<Reward> rewards, @NotNull Instant timestamp, double profit) {
			this.corpseType = corpseType;
			this.rewards = rewards;
			this.timestamp = timestamp;
			this.profit = profit;
		}

		private CorpseLoot(@NotNull CorpseType corpseType, @NotNull List<Reward> rewards, @NotNull Instant timestamp) {
			this(corpseType, rewards, timestamp, 0);
		}

		public @NotNull CorpseType corpseType() { return corpseType; }

		public @NotNull List<Reward> rewards() { return rewards; }

		public @NotNull Instant timestamp() { return timestamp; }

		public double profit() { return profit; }

		public void addLoot(@NotNull String itemName, int amount) {
			String itemId = getItemId(itemName);
			if (itemId.isEmpty()) {
				LOGGER.error("No matching item id for name `{}`. Report this!", itemName);
				return;
			}
			Reward reward = new Reward(amount, itemId);
			rewards.add(reward);
			if (PRICELESS_ITEMS.contains(itemId)) return;

			DoubleBooleanPair price = ItemUtils.getItemPrice(itemId);
			if (!price.rightBoolean()) {
				LOGGER.warn("No price found for item `{}`.", itemId);
				// Only fired once per corpse
				if (isPriceDataComplete) LOGGER.warn("Profit calculation will not be accurate due to missing item price, therefore it will not be sent to chat. It will still be added to the corpse history.");
				markPriceDataIncomplete();
				return;
			}
			profit += price.leftDouble() * amount;
			reward.pricePerUnit(price.leftDouble());
		}

		public boolean isPriceDataComplete() { return isPriceDataComplete; }

		public void markPriceDataIncomplete() { isPriceDataComplete = false; }

		public void markPriceDataComplete() { isPriceDataComplete = true; }

		private static @NotNull String getItemId(String itemName) {
			return NAME2ID_MAP.getOrDefault(itemName, "");
		}
	}

	public static class Reward {
		public static final Codec<Reward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("amount").forGetter(Reward::amount),
				Codec.STRING.fieldOf("itemId").forGetter(Reward::itemId),
				Codec.DOUBLE.fieldOf("pricePerUnit").forGetter(Reward::pricePerUnit)
		).apply(instance, Reward::new));

		private final String itemId;
		private int amount;
		private double pricePerUnit;

		public Reward(int amount, String itemId, double pricePerUnit) {
			this.amount = amount;
			this.itemId = itemId;
			this.pricePerUnit = pricePerUnit;
		}

		public Reward(int amount, String itemId) {
			this(amount, itemId, 0);
		}

		public int amount() {
			return amount;
		}

		public void amount(int amount) {
			this.amount = amount;
		}

		public String itemId() {
			return itemId;
		}

		public double pricePerUnit() {
			return pricePerUnit;
		}

		public void pricePerUnit(double pricePerUnit) {
			this.pricePerUnit = pricePerUnit;
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
		NAME2ID_MAP.put("Ice Cold I", "ENCHANTMENT_ICE_COLD_1");
		NAME2ID_MAP.put("Dwarven O's Metallic Minis", "DWARVEN_OS_METALLIC_MINIS");
		NAME2ID_MAP.put("Shattered Locket", "SHATTERED_PENDANT");

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
