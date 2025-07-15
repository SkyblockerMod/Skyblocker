package de.hysky.skyblocker.skyblock.radialMenu.menus;

import de.hysky.skyblocker.skyblock.radialMenu.RadialMenu;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.regex.Pattern;

public class FastTravelMenu implements RadialMenu {
	private final Pattern TITLE_PATTERN = Pattern.compile("(fast travel)|(.* warps)");

	@Override
	public Text getTitle(Text title) {
		return title;
	}

	@Override
	public boolean titleMatches(String title) {
		return this.isEnabled() && TITLE_PATTERN.matcher(title).matches();
	}

	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		//check for warps you can't use and don't add them
		if (stack.getItem().equals(Items.PLAYER_HEAD) && stack.getComponents().get(DataComponentTypes.PROFILE).id().get().toString().equals("7b4a8060-5963-30cd-95df-d4e574fd7795")){
			return false;
		}
		return stack.getItem() != Items.BLACK_STAINED_GLASS_PANE;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
