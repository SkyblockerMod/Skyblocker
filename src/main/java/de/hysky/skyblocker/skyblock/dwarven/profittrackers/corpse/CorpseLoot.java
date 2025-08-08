package de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.skyblock.dwarven.CorpseType;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.doubles.DoubleBooleanPair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

public final class CorpseLoot {
	public static final Codec<CorpseLoot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CorpseType.CODEC.fieldOf("corpseType").forGetter(CorpseLoot::corpseType),
			Reward.CODEC.listOf().fieldOf("rewards").forGetter(CorpseLoot::rewards),
			Codec.LONG.xmap(Instant::ofEpochMilli, Instant::toEpochMilli).fieldOf("timestamp").forGetter(CorpseLoot::timestamp),
			Codec.DOUBLE.fieldOf("profit").forGetter(CorpseLoot::profit)
	).apply(instance, CorpseLoot::new));
	public static final Logger LOGGER = LoggerFactory.getLogger(CorpseLoot.class);

	private final @NotNull CorpseType corpseType;
	private final @NotNull List<Reward> rewards;
	private final @NotNull Instant timestamp;
	private double profit;
	private boolean isPriceDataComplete = true;

	CorpseLoot(@NotNull CorpseType corpseType, @NotNull List<Reward> rewards, @NotNull Instant timestamp, double profit) {
		this.corpseType = corpseType;
		this.rewards = rewards;
		this.timestamp = timestamp;
		this.profit = profit;
	}

	CorpseLoot(@NotNull CorpseType corpseType, @NotNull List<Reward> rewards, @NotNull Instant timestamp) {
		this(corpseType, rewards, timestamp, 0);
	}

	public @NotNull CorpseType corpseType() { return corpseType; }

	public @NotNull List<Reward> rewards() { return rewards; }

	public @NotNull Instant timestamp() { return timestamp; }

	public double profit() { return profit; }

	public void profit(double profit) { this.profit = profit; }

	public void addLoot(@NotNull String itemName, int amount) {
		String itemId = getItemId(itemName);
		if (itemId.isEmpty()) {
			LOGGER.error("No matching item id for name `{}`. Report this!", itemName);
			return;
		}
		Reward reward = new Reward(amount, itemId);
		rewards.add(reward);
		if (CorpseProfitTracker.PRICELESS_ITEMS.contains(itemId)) return;

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
		return CorpseProfitTracker.getName2IdMap().getOrDefault(itemName, "");
	}
}
