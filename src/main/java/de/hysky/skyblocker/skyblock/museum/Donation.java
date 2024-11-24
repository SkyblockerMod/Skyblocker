package de.hysky.skyblocker.skyblock.museum;

import de.hysky.skyblocker.skyblock.item.tooltip.adders.CraftPriceTooltip;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.data.NEUItem;

import java.util.List;

public class Donation {
	private final String category;
	private final String id;
	private final List<ArmorPiece> set;
	private final int xp;
	private final List<String> upgrades;
	private double price;
	private double craftCost;
	private double effectivePrice;

	public Donation(String category, String id, List<ArmorPiece> set, int xp, List<String> upgrades) {
		this.category = category;
		this.id = id;
		this.set = set;
		this.xp = xp;
		this.upgrades = upgrades;
	}

	public void initPriceData() {
		this.price = 0;
		this.craftCost = 0;
		if (isArmorSet()) {
			for (ArmorPiece piece : getSet()) {
				double price = ItemUtils.getItemPrice(piece.getId()).leftDouble();
				double craftCost = getCraftCost(piece.getId());
				piece.setPrice(price);
				piece.setCraftCost(craftCost);
				this.price += price;
				this.craftCost += craftCost;
			}
		} else {
			this.price = ItemUtils.getItemPrice(id).leftDouble();
			this.craftCost = getCraftCost(id);
		}
	}

	public String getCategory() {
		return category;
	}

	public String getId() {
		return id;
	}

	public List<ArmorPiece> getSet() {
		return set;
	}

	public boolean isArmorSet() {
		return !set.isEmpty();
	}

	public int getXp() {
		return xp;
	}

	public List<String> getUpgrades() {
		return upgrades;
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

	public double getPrice() {
		return price;
	}

	public boolean hasPrice() {
		return effectivePrice > 0;
	}

	public boolean hasLBinPrice() {
		return price > 0;
	}

	public boolean isCraftable() {
		return craftCost > 0;
	}

	private double getCraftCost(String id) {
		NEUItem neuItem = NEURepoManager.NEU_REPO.getItems().getItemBySkyblockId(id);
		if (neuItem != null && !neuItem.getRecipes().isEmpty()) {
			return CraftPriceTooltip.getItemCost(neuItem.getRecipes().getFirst(), 0);
		}
		return 0;
	}
}
