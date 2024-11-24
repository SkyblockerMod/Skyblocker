package de.hysky.skyblocker.skyblock.museum;

public class ArmorPiece {
	private final String id;
	private double craftCost;
	private double price;
	private double effectivePrice;

	public ArmorPiece(String id) {
		this.id = id;
	}

	public double getEffectivePrice() {
		return effectivePrice;
	}

	public void setEffectivePrice(double effectivePrice) {
		this.effectivePrice = effectivePrice;
	}

	public String getId() {
		return id;
	}

	public double getCraftCost() {
		return craftCost;
	}

	public void setCraftCost(double craftCost) {
		this.craftCost = craftCost;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
