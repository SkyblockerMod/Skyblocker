package de.hysky.skyblocker.utils.container;

import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;

public interface ContainerMatcher {
    /**
     * Tests if the given screen should be handled by this matcher.
     * @return {@code true} if this matcher should apply to the given screen, {@code false} otherwise
     */
    boolean test(@NotNull Screen screen);

    /**
     * @return {@code true} if this matcher is enabled, {@code false} otherwise
     */
    boolean isEnabled();
}
