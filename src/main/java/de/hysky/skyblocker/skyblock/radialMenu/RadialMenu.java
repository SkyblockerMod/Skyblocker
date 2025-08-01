package de.hysky.skyblocker.skyblock.radialMenu;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.azureaaron.dandelion.systems.Option;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public abstract class RadialMenu {

	/**
	 * @param title the original window title
	 * @return the title to show in the radial menu
	 */
	protected abstract Text getTitle(Text title);

	/**
	 * Works out if this radial menu should be shown
	 *
	 * @param title screen title to compare
	 * @return the menu should be shown
	 */
	protected abstract boolean titleMatches(String title);

	/**
	 * Works out if an item should be added to the menu
	 *
	 * @param slotId the slot the items in
	 * @param stack  the item
	 * @return if it should be added
	 */
	protected abstract boolean itemMatches(int slotId, ItemStack stack);

	public abstract String getConfigId();


	/**
	 * Lets a Menu remap button clicked to suit it. Most will not need to override this function
	 *
	 * @param originalButton original button clicked by user
	 * @param slotId         the slot to be clicked
	 * @return the remaped button
	 */
	public abstract int remapClickSlotButton(int originalButton, int slotId);

	/**
	 * @return an array of names of items that are used to navigate the menu. These will be placed at the bottom
	 */
	public abstract String[] getNavigationItemNames();

	protected boolean getEnabled() {
		return SkyblockerConfigManager.get().uiAndVisuals.radialMenu.enabled && SkyblockerConfigManager.get().uiAndVisuals.radialMenu.enabledMenus.getOrDefault(getConfigId(), false);
	}

	public Option<Boolean> getOption(SkyblockerConfig config) {
		return Option.<Boolean>createBuilder()
				.name(Text.translatable("skyblocker.config.uiAndVisuals.radialMenu." + getConfigId()))
				.binding(false,
						() -> config.uiAndVisuals.radialMenu.enabledMenus.getOrDefault(getConfigId(), false),
						newValue -> config.uiAndVisuals.radialMenu.enabledMenus.put(getConfigId(), newValue.booleanValue()))
				.controller(ConfigUtils.createBooleanController())
				.build();
	}
}
