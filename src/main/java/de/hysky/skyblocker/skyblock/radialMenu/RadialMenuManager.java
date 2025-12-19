package de.hysky.skyblocker.skyblock.radialMenu;



import de.hysky.skyblocker.skyblock.radialMenu.menus.BagsMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.BankMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.BoosterCookieMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.CollectionsMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.FastTravelMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.HuntingMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.ProfileManagementMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.SackOfSacksMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.SacksMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.SkillsMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.SkyblockMenu;
import de.hysky.skyblocker.skyblock.radialMenu.menus.Storage;
import de.hysky.skyblocker.skyblock.radialMenu.menus.WardrobeMenu;
import org.jspecify.annotations.Nullable;

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
			new ProfileManagementMenu(),
			new SackOfSacksMenu(),
			new SacksMenu(),
			new SkillsMenu(),
			new SkyblockMenu(),
			new Storage(),
			new WardrobeMenu()
	};

	public static boolean isMenuExistsFromTitle(String title) {
		return getMenuStream().anyMatch(menu -> menu.titleMatches(title));
	}

	public static @Nullable RadialMenu getMenuFromTitle(String title) {
		return getMenuStream().filter(menu -> menu.titleMatches(title)).findFirst().orElse(null);
	}

	public static Stream<RadialMenu> getMenuStream() {
		return Arrays.stream(menus);
	}
}
