package de.hysky.skyblocker.utils.render.gui;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractContainerMatcher {
	private final Predicate<HandledScreen<?>> screenPredicate;
	/**
	 * This will be filled in with the groups from the title pattern if a regex approach was used.
	 */
	protected @Nullable String[] groups;

	protected AbstractContainerMatcher() {
		this(screen -> true);
	}

	protected AbstractContainerMatcher(@NotNull String titlePattern) {
		this(Pattern.compile(titlePattern));
	}

	protected AbstractContainerMatcher(@NotNull Pattern titlePattern) {
		screenPredicate = screen -> {
			Matcher matcher = titlePattern.matcher(screen.getTitle().getString());
			if (matcher.matches()) {
				groups = new String[matcher.groupCount()];
				for (int i = 0; i < groups.length; i++) {
					groups[i] = matcher.group(i + 1);
				}
				return true;
			}
			return false;
		};
	}

	protected AbstractContainerMatcher(@NotNull Predicate<HandledScreen<?>> screenPredicate) {
		this.screenPredicate = screenPredicate;
	}

	/**
	 * This method will be called after screen initialization.
	 * This could be used to check for certain things in the screen and do stuff based on them by overriding this method.
	 *
	 * @param screen The screen that was just opened.
	 * @return {@code true} if this container matcher should apply on this screen, {@code false} otherwise.
	 */
	public boolean test(HandledScreen<?> screen) {
		return screenPredicate.test(screen);
	}
}
