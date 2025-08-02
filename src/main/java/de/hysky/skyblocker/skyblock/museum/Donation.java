package de.hysky.skyblocker.skyblock.museum;

import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Donation {
	private final String category;
	private final String id;
	private final List<ObjectObjectMutablePair<String, PriceData>> set;
	private final List<String> downgrades = new ArrayList<>();
	private final int xp;
	private List<ObjectIntPair<String>> countsTowards;
	private PriceData priceData;
	private ObjectDoublePair<String> discount;
	private int totalXp;
	private double xpCoinsRatio;

	public Donation(String category, String id, List<ObjectObjectMutablePair<String, PriceData>> set, int xp) {
		this.category = category;
		this.id = id;
		this.set = set;
		this.xp = xp;
	}

	public int getTotalXp() {
		return totalXp;
	}

	public void setTotalXp(int totalXp) {
		this.totalXp = totalXp;
	}

	public List<ObjectIntPair<String>> getCountsTowards() {
		return countsTowards;
	}

	public void setCountsTowards(List<ObjectIntPair<String>> countsTowards) {
		this.countsTowards = countsTowards;
	}

	public PriceData getPriceData() {
		return priceData;
	}

	public void setPriceData() {
		this.priceData = new PriceData(this);
	}

	@Nullable
	public ObjectDoublePair<String> getDiscount() {
		return discount;
	}

	public void setDiscount(ObjectDoublePair<String> discount) {
		this.discount = discount;
	}

	public boolean hasDiscount() {
		return discount != null && discount.rightDouble() > 0d;
	}

	public void addDowngrade(String downgrade) {
		this.downgrades.add(downgrade);
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

	public List<ObjectObjectMutablePair<String, PriceData>> getSet() {
		return set;
	}

	public int getXp() {
		return xp;
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
