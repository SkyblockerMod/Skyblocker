package de.hysky.skyblocker.skyblock.radialMenu.menus;

public class SackOfSacksMenu extends BasicMenu {
	public SackOfSacksMenu() {
		super("sack of sacks", "sacks");
	}

	@Override
	public int remapClickSlotButton(int originalButton, int slotId) {
		//only send normal input on control slots
		if (slotId == 29 || slotId == 30 || slotId == 31) {
			return originalButton;
		}
		return 1;
	}
}
