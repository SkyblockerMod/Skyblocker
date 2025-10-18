package de.hysky.skyblocker.utils.container;

import de.hysky.skyblocker.skyblock.ChestValue;
import net.minecraft.client.gui.screen.Screen;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A regex implementation of {@link ContainerMatcher} that matches the title of the screen.
 */
public abstract class RegexContainerMatcher implements ContainerMatcher {
	/**
	 * The title of the screen must match this pattern for this to be applied. Null means it will be applied to all screens.
	 *
	 * @implNote Don't end your regex with a {@code $} as {@link ChestValue} appends text to the end of the title,
	 * so the regex will stop matching if the player uses chest value.
	 */
	@Nullable
	public final Pattern titlePattern;

	@Nullable
	protected String[] groups = null;

	@Override
	public boolean test(@NotNull Screen screen) {
		return test(screen.getTitle().getString());
	}

	public boolean test(@NotNull String title) {
		if (titlePattern == null) return true;
		Matcher matcher = titlePattern.matcher(title);
		if (matcher.matches()) {
			int groupCount = matcher.groupCount();
			if (groupCount >= 1) { //No need to initialize the array if there are no groups
				groups = new String[groupCount];
				for (int i = 0; i < groupCount; i++) {
					groups[i] = matcher.group(i + 1); // +1 because first group is the whole match, which is useless
				}
			}
			return true;
		}
		return false;
	}

	protected RegexContainerMatcher() {
		this((Pattern) null);
	}

	protected RegexContainerMatcher(@NotNull @Language("RegExp") String titlePattern) {
		this(Pattern.compile(titlePattern));
	}

	protected RegexContainerMatcher(@Nullable Pattern titlePattern) {
		this.titlePattern = titlePattern;
	}

	public @Nullable Pattern getTitlePattern() {
		return titlePattern;
	}
}
