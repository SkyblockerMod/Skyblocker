package de.hysky.skyblocker.skyblock.museum;

import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;

public class PriceData {
	private final double lBinPrice;
	private final double craftCost;
	private double effectivePrice;

	public PriceData(double lBinPrice, double craftCost) {
		this.lBinPrice = lBinPrice;
		this.craftCost = craftCost;
	}

	public PriceData(Donation donation) {
		if (donation.isSet()) {
			double totalLBinPrice = 0, totalCraftCost = 0;
			for (ObjectObjectMutablePair<String, PriceData> piece : donation.getSet()) {
				double lBinPrice = ItemUtils.getItemPrice(piece.left()).leftDouble();
				double craftCost = ItemUtils.getCraftCost(piece.left());

				totalLBinPrice += lBinPrice;
				totalCraftCost += craftCost;

				piece.right(new PriceData(lBinPrice, craftCost));
			}
			this.lBinPrice = totalLBinPrice;
			this.craftCost = totalCraftCost;
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
