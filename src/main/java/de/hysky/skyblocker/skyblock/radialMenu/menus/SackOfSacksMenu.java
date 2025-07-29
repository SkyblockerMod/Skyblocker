package de.hysky.skyblocker.skyblock.radialMenu.menus;

public class SackOfSacksMenu extends BasicMenu {
	public SackOfSacksMenu() {
		super("sack of sacks", "sacks");
	}
	public int remapClickSlotButton(int originalButton, int slotId) {
		if (slotId == 29 || slotId == 30 || slotId == 31) {
			return originalButton;
		}
		return 1;
	}
}
