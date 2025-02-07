package de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class Reward {
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
