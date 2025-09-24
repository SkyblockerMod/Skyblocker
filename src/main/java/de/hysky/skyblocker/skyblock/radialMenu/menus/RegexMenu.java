package de.hysky.skyblocker.skyblock.radialMenu.menus;

import org.intellij.lang.annotations.Language;

import java.util.regex.Pattern;

public abstract class RegexMenu extends BasicMenu {
	private final Pattern pattern;

	public RegexMenu(@Language("RegExp") String regex, String id) {
		super(regex, id);
		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

	public boolean titleMatches(String title) {
		return this.getEnabled() && this.pattern.matcher(title).matches();
	}
}
