package de.hysky.skyblocker.skyblock.radialMenu.menus;

import java.util.regex.Pattern;

import org.intellij.lang.annotations.Language;

public abstract class RegexMenu extends BasicMenu {
	private final Pattern pattern;

	public RegexMenu(@Language("RegExp") String regex, String id) {
		super(regex, id);
		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public boolean titleMatches(String title) {
		return this.getEnabled() && this.pattern.matcher(title).matches();
	}
}
