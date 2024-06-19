package de.hysky.skyblocker.utils.container;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.jetbrains.annotations.NotNull;

public interface AbstractContainerMatcher {
    /**
     * Tests if the given screen should be handled by this matcher.
     * @return {@code true} if this matcher should apply to the given screen, {@code false} otherwise
     */
    boolean test(@NotNull HandledScreen<?> screen);

    /**
     * @return {@code true} if this matcher is enabled, {@code false} otherwise
     */
    boolean isEnabled();
}
