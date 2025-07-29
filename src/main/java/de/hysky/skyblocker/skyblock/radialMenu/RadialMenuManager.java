package de.hysky.skyblocker.skyblock.radialMenu;

import de.hysky.skyblocker.skyblock.radialMenu.menus.*;

import java.util.Arrays;
import java.util.stream.Stream;

public class RadialMenuManager {
	private static final RadialMenu[] menus = new RadialMenu[]{
			new BagsMenu(),
			new BankMenu(),
			new BoosterCookieMenu(),
			new CollectionsMenu(),
			new FastTravelMenu(),
			new HuntingMenu(),
			new ProfileManagmentMenu(),
			new SackOfSacksMenu(),
			new SacksMenu(),
			new SkillsMenu(),
			new SkyblockMenu(),
			new Storage(),
			new WardrobeMenu()
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
