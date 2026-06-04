package de.hysky.skyblocker.skyblock.radialMenu.menus;

public class SacksMenu extends RegexMenu {
	public SacksMenu() {
		super(".* sack", "sacks");
	}

	@Override
	public String[] getNavigationItemNames() {
		return new String[]{"Rune Type Filter", "Close", "Go Back", "Item Tier"};
	}
}
