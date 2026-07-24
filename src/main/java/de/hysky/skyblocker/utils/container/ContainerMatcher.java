package de.hysky.skyblocker.utils.container;

import java.util.function.Predicate;
import net.minecraft.client.gui.screens.Screen;

public interface ContainerMatcher extends Predicate<Screen> {
	/**
	 * Tests if the given screen should be handled by this matcher.
	 * @return {@code true} if this matcher should apply to the given screen, {@code false} otherwise
	 */
	@Override
	boolean test(Screen screen);

	/**
	 * @return {@code true} if this matcher is enabled, {@code false} otherwise
	 */
	boolean isEnabled();
}
