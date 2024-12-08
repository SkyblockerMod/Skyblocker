package de.hysky.skyblocker.skyblock.museum;

import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class Donation {
	private final String category;
	private final String id;
	private final List<Pair<String, PriceData>> set;
	private final List<String> upgrades;
	private List<String> downgrades;
	private List<Pair<String, Integer>> countsTowards;// downgrades not donated
	private PriceData priceData;
	private Pair<String, Double> discount;
	private final int xp;
	private int totalXp;
	private double xpCoinsRatio;

	public Donation(String category, String id, List<Pair<String, PriceData>> set, int xp, List<String> upgrades) {
		this.category = category;
		this.id = id;
		this.set = set;
		this.xp = xp;
		this.upgrades = upgrades;
	}

	public int getTotalXp() {
		return totalXp;
	}

	public void setTotalXp(int totalXp) {
		this.totalXp = totalXp;
	}

	public List<Pair<String, Integer>> getCountsTowards() {
		return countsTowards;
	}

	public void setCountsTowards(List<Pair<String, Integer>> countsTowards) {
		this.countsTowards = countsTowards;
	}

	public PriceData getPriceData() {
		return priceData;
	}

	public void setPriceData() {
		this.priceData = new PriceData(this);
	}

	public void setDowngrades() {
		List<String> downgrades = new ArrayList<>();
		for (List<String> list : MuseumItemCache.ORDERED_UPGRADES) {
			int armorIndex = list.indexOf(id);
			if (armorIndex > 0) {
				for (int i = armorIndex - 1; i >= 0; i--) {
					downgrades.add(list.get(i));
				}
			}
		}
		this.downgrades = downgrades;
	}

	public Pair<String, Double> getDiscount() {
		return discount;
	}

	public void setDiscount(Pair<String, Double> discount) {
		this.discount = discount;
	}

	public boolean hasDiscount() {
		return discount != null && discount.getRight() > 0d;
	}

	public List<String> getDowngrades() {
		return downgrades;
	}

	public double getXpCoinsRatio() {
		return xpCoinsRatio;
	}

	public void setXpCoinsRatio(double xpCoinsRatio) {
		this.xpCoinsRatio = xpCoinsRatio;
	}

	public String getCategory() {
		return category;
	}

	public String getId() {
		return id;
	}


	public boolean isSet() {
		return !set.isEmpty();
	}

	public List<Pair<String, PriceData>> getSet() {
		return set;
	}

	public int getXp() {
		return xp;
	}

	public List<String> getUpgrades() {
		return upgrades;
	}

	public boolean isCraftable() {
		return this.priceData.getCraftCost() > 0;
	}

	public boolean hasLBinPrice() {
		return this.priceData.getLBinPrice() > 0;
	}

	public boolean hasPrice() {
		return this.priceData.getEffectivePrice() > 0;
	}
}
