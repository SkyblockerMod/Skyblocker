package de.hysky.skyblocker.skyblock.item.slottext;

import de.hysky.skyblocker.utils.container.RegexContainerMatcher;
import de.hysky.skyblocker.utils.container.SlotTextAdder;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Simple implementation of a slot text adder.
 * Extend this class and add it to {@link SlotTextManager#adders} to add text to any arbitrary slot.
 */
public abstract class SimpleSlotTextAdder extends RegexContainerMatcher implements SlotTextAdder {
	protected final @Nullable ConfigInformation configInformation;

	/**
	 * Utility constructor that will compile the given string into a pattern with no config
	 *
	 * @see #SimpleSlotTextAdder(Pattern)
	 */
	protected SimpleSlotTextAdder(@NotNull @Language("RegExp") String titlePattern) {
		this(titlePattern, null);
	}

	/**
	 * Creates a SlotTextAdder that will be applied to screens with titles that match the given pattern with no config
	 *
	 * @param titlePattern The pattern to match the screen title against.
	 */
	protected SimpleSlotTextAdder(@NotNull Pattern titlePattern) {
		this(titlePattern, null);
	}

	/**
	 * Creates a SlotTextAdder that will be applied to all screens with no config
	 */
	protected SimpleSlotTextAdder() {
		this((ConfigInformation) null);
	}

	/**
	 * Utility constructor that will compile the given string into a pattern.
	 * The adder will be able to be turned off/on in the config using info provided by the {@link de.hysky.skyblocker.utils.container.SlotTextAdder.ConfigInformation}
	 *
	 * @see #SimpleSlotTextAdder(Pattern)
	 */
	protected SimpleSlotTextAdder(@NotNull @Language("RegExp") String titlePattern, @Nullable ConfigInformation configInformation) {
		super(titlePattern);
		this.configInformation = configInformation;
	}

	/**
	 * Creates a SlotTextAdder that will be applied to screens with titles that match the given pattern.
	 * The adder will be able to be turned off/on in the config using info provided by the {@link de.hysky.skyblocker.utils.container.SlotTextAdder.ConfigInformation}
	 *
	 * @param titlePattern The pattern to match the screen title against.
	 */
	protected SimpleSlotTextAdder(@NotNull Pattern titlePattern, @Nullable ConfigInformation configInformation) {
		super(titlePattern);
		this.configInformation = configInformation;
	}

	/**
	 * Creates a SlotTextAdder that will be applied to all screens.
	 * The adder will be able to be turned off/on in the config using info provided by the {@link de.hysky.skyblocker.utils.container.SlotTextAdder.ConfigInformation}
	 */
	protected SimpleSlotTextAdder(@Nullable ConfigInformation configInformation) {
		super();
		this.configInformation = configInformation;
	}

	@Override
	@Nullable
	public ConfigInformation getConfigInformation() {
		return configInformation;
	}
}
