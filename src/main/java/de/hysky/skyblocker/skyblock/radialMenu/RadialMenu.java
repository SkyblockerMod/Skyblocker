package de.hysky.skyblocker.skyblock.radialMenu;

import de.hysky.skyblocker.config.ConfigUtils;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.azureaaron.dandelion.api.Option;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public abstract class RadialMenu {

	/**
	 * @param title the original window title
	 * @return the title to show in the radial menu
	 */
	protected abstract Component getTitle(Component title);

	/**
	 * Works out if this radial menu should be shown for a given screen title
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
	 * Lets a Menu remap button clicked to suit it.
	 *
	 * @param originalButton original button clicked by user
	 * @param slotId         the slot to be clicked
	 */
	public abstract int remapClickSlotButton(int originalButton, int slotId);

	/**
	 * Lets a menu offset the clicked slot compared to the one shown in the menu
	 *
	 * @return offset to add to menu
	 */
	public abstract int clickSlotOffset(int slotId);

	/**
	 * @return an array of names of items that are used to navigate the menu. These will be placed at the bottom
	 */
	public abstract String[] getNavigationItemNames();

	protected boolean getEnabled() {
		return SkyblockerConfigManager.get().uiAndVisuals.radialMenu.enabled && SkyblockerConfigManager.get().uiAndVisuals.radialMenu.enabledMenus.getOrDefault(getConfigId(), false);
	}

	public Option<Boolean> getOption(SkyblockerConfig config) {
		return Option.<Boolean>createBuilder()
				.name(Component.translatable("skyblocker.config.uiAndVisuals.radialMenu." + getConfigId()))
				.binding(false,
						() -> config.uiAndVisuals.radialMenu.enabledMenus.getOrDefault(getConfigId(), false),
						newValue -> config.uiAndVisuals.radialMenu.enabledMenus.put(getConfigId(), newValue.booleanValue()))
				.controller(ConfigUtils.createBooleanController())
				.build();
	}
}
