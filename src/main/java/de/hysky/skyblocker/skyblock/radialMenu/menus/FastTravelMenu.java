package de.hysky.skyblocker.skyblock.radialMenu.menus;

import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;


public class FastTravelMenu extends RegexMenu {

	public FastTravelMenu() {
		super("(fast travel)|(.* warps)", "fastTravel");
	}


	@Override
	public boolean itemMatches(int slotId, ItemStack stack) {
		//hide advance mode and paper icons button
		if (slotId == 50 || slotId == 53) return false;
		//check for warps you can't use and don't add them
		if (stack.getItem().equals(Items.BEDROCK) || stack.getItem().equals(Items.PLAYER_HEAD) && ItemUtils.getHeadTexture(stack).equals("eyJ0aW1lc3RhbXAiOjE1ODU3NjY0NDM5MTYsInByb2ZpbGVJZCI6ImIwZDRiMjhiYzFkNzQ4ODlhZjBlODY2MWNlZTk2YWFiIiwicHJvZmlsZU5hbWUiOiJNaW5lU2tpbl9vcmciLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y3NjliYmI5ZmIyMzE2ODA4MTMxZTZjMmQwMmNlMTRlM2FhYjY3NGRlYjRiNTU4YmIyNjVjMzE0MWQxOTliMDgifX19")) {
			return false;
		}
		return stack.getItem() != Items.BLACK_STAINED_GLASS_PANE;
	}
}
