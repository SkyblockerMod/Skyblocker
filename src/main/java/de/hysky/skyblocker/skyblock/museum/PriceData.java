package de.hysky.skyblocker.skyblock.museum;

import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.util.Pair;

public class PriceData {
	private double lBinPrice = 0;
	private double craftCost = 0;
	private double effectivePrice;

	public PriceData(double lBinPrice, double craftCost) {
		this.lBinPrice = lBinPrice;
		this.craftCost = craftCost;
	}

	public PriceData(Donation donation) {
		if (donation.isSet()) {
			for (Pair<String, PriceData> piece : donation.getSet()) {
				double lBinPrice = ItemUtils.getItemPrice(piece.getLeft()).leftDouble();
				double craftCost = ItemUtils.getCraftCost(piece.getLeft());

				this.lBinPrice += lBinPrice;
				this.craftCost += craftCost;

				piece.setRight(new PriceData(lBinPrice, craftCost));
			}
		} else {
			this.lBinPrice = ItemUtils.getItemPrice(donation.getId()).leftDouble();
			this.craftCost = ItemUtils.getCraftCost(donation.getId());
		}
	}

	public double getLBinPrice() {
		return lBinPrice;
	}

	public double getCraftCost() {
		return craftCost;
	}

	public double getEffectivePrice() {
		return effectivePrice;
	}

	public void setEffectivePrice(double effectivePrice) {
		this.effectivePrice = effectivePrice;
	}
}
