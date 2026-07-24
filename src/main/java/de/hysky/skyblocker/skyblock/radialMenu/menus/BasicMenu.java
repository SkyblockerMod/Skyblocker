package de.hysky.skyblocker.skyblock.radialMenu.menus;

import de.hysky.skyblocker.skyblock.radialMenu.RadialMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public abstract class BasicMenu extends RadialMenu {
	final String title;
	final String id;

	public BasicMenu(String title, String id) {
		this.title = title;
		this.id = id;
	}

	@Override
	public Component getTitle(Component title) {
		return title;
	}

	@Override
	public boolean titleMatches(String title) {
		return this.getEnabled() && title.equalsIgnoreCase(this.title);
	}

	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		return stack.getItem() != Items.STAINED_GLASS_PANE.black();
	}

	@Override
	public String getConfigId() {
		return id;
	}

	@Override
	public int remapClickSlotButton(int originalButton, int slotId) {
		return originalButton;
	}

	@Override
	public int clickSlotOffset(int slotId) {
		return 0;
	}

	@Override
	public String[] getNavigationItemNames() {
		return new String[]{"Go Back", "Close"};
	}
}
