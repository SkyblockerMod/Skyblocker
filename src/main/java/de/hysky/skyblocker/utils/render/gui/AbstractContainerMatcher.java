package de.hysky.skyblocker.utils.render.gui;

import de.hysky.skyblocker.skyblock.ChestValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public abstract class AbstractContainerMatcher {
    /**
     * The title of the screen must match this pattern for this to be applied. Null means it will be applied to all screens.
     * @implNote Don't end your regex with a {@code $} as {@link ChestValue} appends text to the end of the title,
     * so the regex will stop matching if the player uses chest value.
     */
    @Nullable
    public final Pattern titlePattern;

    protected AbstractContainerMatcher() {
        this((Pattern) null);
    }

    protected AbstractContainerMatcher(@NotNull String titlePattern) {
        this(Pattern.compile(titlePattern));
    }

    protected AbstractContainerMatcher(@Nullable Pattern titlePattern) {
        this.titlePattern = titlePattern;
    }
}
