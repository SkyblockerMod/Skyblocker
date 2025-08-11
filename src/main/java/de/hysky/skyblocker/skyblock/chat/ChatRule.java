package de.hysky.skyblocker.skyblock.chat;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.utils.CollectionUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Data class to contain all the settings for a chat rule
 */
public class ChatRule {
	/**
	 * Codec that can decode both {@link String} and {@link EnumSet} of locations, while encoding only {@link EnumSet} of locations.
	 * <br>
	 * This is necessary due to a change in how the locations are stored in the config.
	 */
	@VisibleForTesting
	static final Codec<EnumSet<Location>> LOCATION_FIXING_CODEC = Codec.either(Location.SET_CODEC, Codec.STRING).xmap(
			either -> either.map(Function.identity(), ChatRule::encodeString),
			Either::left
	);

	private static final Codec<ChatRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("name").forGetter(ChatRule::getName),
			Codec.BOOL.fieldOf("enabled").forGetter(ChatRule::getEnabled),
			Codec.BOOL.fieldOf("isPartialMatch").forGetter(ChatRule::getPartialMatch),
			Codec.BOOL.fieldOf("isRegex").forGetter(ChatRule::getRegex),
			Codec.BOOL.fieldOf("isIgnoreCase").forGetter(ChatRule::getIgnoreCase),
			Codec.STRING.fieldOf("filter").forGetter(ChatRule::getFilter),
			LOCATION_FIXING_CODEC.fieldOf("validLocations").forGetter(ChatRule::getValidLocations),
			Codec.BOOL.fieldOf("hideMessage").forGetter(ChatRule::getHideMessage),
			Codec.BOOL.fieldOf("showActionBar").forGetter(ChatRule::getShowActionBar),
			Codec.BOOL.fieldOf("showAnnouncement").forGetter(ChatRule::getShowAnnouncement),
			Codec.STRING.optionalFieldOf("replaceMessage").forGetter(ChatRule::getReplaceMessageOpt),
			SoundEvent.CODEC.optionalFieldOf("customSound").forGetter(ChatRule::getCustomSoundOpt)
	).apply(instance, ChatRule::new));
	public static final Codec<List<ChatRule>> LIST_CODEC = CODEC.listOf();

	private String name;

	// Inputs
	private boolean enabled;
	private boolean isPartialMatch;
	private boolean isRegex;
	private boolean isIgnoreCase;
	private String filter;
	private EnumSet<Location> validLocations;

	// Outputs
	private boolean hideMessage;
	private boolean showActionBar;
	private boolean showAnnouncement;
	private String replaceMessage;
	private SoundEvent customSound;

	/**
	 * Creates a chat rule with default options.
	 */
	protected ChatRule() {
		this.name = "New Rule";

		this.enabled = true;
		this.isPartialMatch = false;
		this.isRegex = false;
		this.isIgnoreCase = true;
		this.filter = "";
		this.validLocations = EnumSet.noneOf(Location.class);

		this.hideMessage = true;
		this.showActionBar = false;
		this.showAnnouncement = false;
		this.replaceMessage = null;
		this.customSound = null;
	}

	public ChatRule(String name, boolean enabled, boolean isPartialMatch, boolean isRegex, boolean isIgnoreCase, String filter, EnumSet<Location> validLocations, boolean hideMessage, boolean showActionBar, boolean showAnnouncement, @Nullable String replaceMessage, @Nullable SoundEvent customSound) {
		this.name = name;
		this.enabled = enabled;
		this.isPartialMatch = isPartialMatch;
		this.isRegex = isRegex;
		this.isIgnoreCase = isIgnoreCase;
		this.filter = filter;
		this.validLocations = validLocations;
		this.hideMessage = hideMessage;
		this.showActionBar = showActionBar;
		this.showAnnouncement = showAnnouncement;
		this.replaceMessage = replaceMessage;
		this.customSound = customSound;
	}

	private ChatRule(String name, boolean enabled, boolean isPartialMatch, boolean isRegex, boolean isIgnoreCase, String filter, EnumSet<Location> validLocations, boolean hideMessage, boolean showActionBar, boolean showAnnouncement, Optional<String> replaceMessage, Optional<SoundEvent> customSound) {
		this(name, enabled, isPartialMatch, isRegex, isIgnoreCase, filter, validLocations, hideMessage, showActionBar, showAnnouncement, replaceMessage.orElse(null), customSound.orElse(null));
	}

	protected String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected boolean getEnabled() {
		return enabled;
	}

	protected void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	protected boolean getPartialMatch() {
		return isPartialMatch;
	}

	protected void setPartialMatch(boolean partialMatch) {
		isPartialMatch = partialMatch;
	}

	protected boolean getRegex() {
		return isRegex;
	}

	protected void setRegex(boolean regex) {
		isRegex = regex;
	}

	protected boolean getIgnoreCase() {
		return isIgnoreCase;
	}

	protected void setIgnoreCase(boolean ignoreCase) {
		isIgnoreCase = ignoreCase;
	}

	protected String getFilter() {
		return filter;
	}

	protected void setFilter(String filter) {
		this.filter = filter;
	}

	protected boolean getHideMessage() {
		return hideMessage;
	}

	protected void setHideMessage(boolean hideMessage) {
		this.hideMessage = hideMessage;
	}

	protected boolean getShowActionBar() {
		return showActionBar;
	}

	protected void setShowActionBar(boolean showActionBar) {
		this.showActionBar = showActionBar;
	}

	protected boolean getShowAnnouncement() {
		return showAnnouncement;
	}

	protected void setShowAnnouncement(boolean showAnnouncement) {
		this.showAnnouncement = showAnnouncement;
	}

	protected String getReplaceMessage() {
		return replaceMessage;
	}

	private Optional<String> getReplaceMessageOpt() {
		return Optional.ofNullable(replaceMessage);
	}

	protected void setReplaceMessage(String replaceMessage) {
		this.replaceMessage = replaceMessage;
	}

	protected SoundEvent getCustomSound() {
		return customSound;
	}

	private Optional<SoundEvent> getCustomSoundOpt() {
		return Optional.ofNullable(customSound);
	}

	protected void setCustomSound(SoundEvent customSound) {
		this.customSound = customSound;
	}

	protected EnumSet<Location> getValidLocations() {
		return validLocations;
	}

	protected void setValidLocations(EnumSet<Location> validLocations) {
		this.validLocations = validLocations;
	}

	/**
	 * checks every input option and if the games state and the inputted str matches them returns true.
	 *
	 * @param inputString the chat message to check if fits
	 * @return if the inputs are all true and the outputs should be performed
	 */
	protected boolean isMatch(String inputString) {
		//enabled
		if (!enabled) return false;

		//ignore case
		String testString;
		String testFilter;

		if (isIgnoreCase) {
			testString = inputString.toLowerCase();
			testFilter = filter.toLowerCase();
		} else {
			testString = inputString;
			testFilter = filter;
		}

		//filter
		if (testFilter.isBlank()) return false;
		if (isRegex) {
			if (isPartialMatch) {
				if (!Pattern.compile(testFilter).matcher(testString).find()) return false;
			} else {
				if (!testString.matches(testFilter)) return false;
			}
		} else {
			if (isPartialMatch) {
				if (!testString.contains(testFilter)) return false;
			} else {
				if (!testFilter.equals(testString)) return false;
			}
		}

		// As a special case, if there are no valid locations all locations are valid.
		// This exists because it doesn't make sense to remove all valid locations, you should disable the chat rule if you want to do that.
		// This way, we can also default to an empty set for validLocations.
		if (validLocations.isEmpty()) return true;
		// UNKNOWN isn't a valid location, so we act the same as the list being empty.
		if (validLocations.size() == 1 && validLocations.contains(Location.UNKNOWN)) return true;
		return validLocations.contains(Utils.getLocation());
	}

	// This maps invalid entries to `Location.UNKNOWN`, which is better than failing outright.
	private static EnumSet<Location> encodeString(String string) {
		// Necessary for empty strings, which would've been decoded as UNKNOWN otherwise.
		if (string.isEmpty()) return EnumSet.noneOf(Location.class);

		// If a location's name contains a ! prefix, it's negated, meaning every location except that one is valid.
		if (string.contains("!")) return EnumSet.complementOf(
				Arrays.stream(string.split(", ?"))
					  .filter(s1 -> s1.startsWith("!")) // Filter out the non-negated locations because the negation of any element in the list already implies those non-negated locations being valid.
					  .map(s -> s.substring(1)) // Skip the `!`
					  .map(Location::fromFriendlyName)
					  .collect(CollectionUtils.enumSetCollector(Location.class))
		);
		return Arrays.stream(string.split(", ?"))
					 .map(Location::fromFriendlyName)
					 .collect(CollectionUtils.enumSetCollector(Location.class));
	}
}
