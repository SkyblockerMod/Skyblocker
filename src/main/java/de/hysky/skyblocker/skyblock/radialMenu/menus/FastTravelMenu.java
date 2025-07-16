package de.hysky.skyblocker.skyblock.radialMenu.menus;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;


public class FastTravelMenu extends RegexMenu {

	public FastTravelMenu() {
		super("(fast travel)|(.* warps)", "fastTravel");
	}

	public Text getTitle(Text title) {
		return title;
	}

	public boolean itemMatches(int slotId, ItemStack stack) {
		//hide advance mode and paper icons button
		if (slotId == 50 || slotId == 53) return false;
		//check for warps you can't use and don't add them
		if (stack.getItem().equals(Items.BEDROCK) || stack.getItem().equals(Items.PLAYER_HEAD) && stack.getComponents().get(DataComponentTypes.PROFILE).id().get().toString().equals("7b4a8060-5963-30cd-95df-d4e574fd7795")) {
			return false;
		}
		return stack.getItem() != Items.BLACK_STAINED_GLASS_PANE;
	}
}
