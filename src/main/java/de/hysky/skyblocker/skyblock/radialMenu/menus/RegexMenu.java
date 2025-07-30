package de.hysky.skyblocker.skyblock.radialMenu.menus;

import de.hysky.skyblocker.skyblock.radialMenu.RadialMenu;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.regex.Pattern;

public abstract class RegexMenu extends RadialMenu {
	private final Pattern pattern;
	private final String id;

	public RegexMenu(String regex, String id) {
		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		this.id = id;
	}

	public Text getTitle(Text title) {
		return title;
	}

	public boolean titleMatches(String title) {
		return this.getEnabled() && this.pattern.matcher(title).matches();
	}

	public boolean itemMatches(int slotId, ItemStack stack) {
		return stack.getItem() != Items.BLACK_STAINED_GLASS_PANE;
	}

	public String getConfigId() {
		return id;
	}

	public int remapClickSlotButton(int originalButton, int slotId) {
		return originalButton;
	}

	public String[] getNavigationItemNames() {
		return new String[]{"Go Back","Close"};
	}

}
