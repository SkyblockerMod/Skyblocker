package de.hysky.skyblocker.skyblock.radialMenu;

import de.hysky.skyblocker.skyblock.radialMenu.menus.FastTravelMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.HuntingMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.SkyblockMenu;

import java.util.Arrays;
import java.util.stream.Stream;

public class RadialMenuManager {
	private static final RadialMenu[] menus = new RadialMenu[]{
			new FastTravelMenu(),
			new HuntingMenu(),
			new SkyblockMenu()
	};

	public static boolean isMenuExistsFromTitle(String title) {
		return Arrays.stream(menus).anyMatch(menu -> menu.titleMatches(title));
	}

	public static RadialMenu getMenuFromTitle(String title) {
		return Arrays.stream(menus).filter(menu -> menu.titleMatches(title)).findFirst().orElse(null);
	}

	public static Stream<RadialMenu> getMenuStream() {
		return Arrays.stream(menus);
	}
}
