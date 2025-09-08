package de.hysky.skyblocker.skyblock.chat;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.utils.CollectionUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
	private Pattern pattern; // Only compile Regex patterns once

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
		this.pattern = null;
	}

	protected boolean getIgnoreCase() {
		return isIgnoreCase;
	}

	protected void setIgnoreCase(boolean ignoreCase) {
		isIgnoreCase = ignoreCase;
		this.pattern = null;
	}

	protected String getFilter() {
		return filter;
	}

	protected void setFilter(String filter) {
		this.filter = filter;
		this.pattern = null;
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

	private void compilePattern(String filterText) {
		if (pattern != null) return;

		try {
			this.pattern = Pattern.compile(filterText);
		} catch (PatternSyntaxException ex) {
			this.enabled = false;
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null) return;
			client.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.chat.chatRules.invalidRegex", this.name)), false);
		}
	}

	/**
	 * checks every input option and if the games state and the inputted str matches them returns true.
	 *
	 * @param inputString the chat message to check if fits
	 * @return if the inputs are all true and the outputs should be performed
	 */
	protected Match isMatch(String inputString) {
		//enabled
		if (!enabled) return Match.noMatch();

		//ignore case
		String testString = isIgnoreCase ? inputString.toLowerCase() : inputString;
		String testFilter = isIgnoreCase ? filter.toLowerCase() : filter;
		if (testFilter.isBlank()) return Match.noMatch();

		//filter
		Match match;
		if (isRegex) {
			compilePattern(testFilter);
			if (pattern == null) return Match.noMatch();

			Matcher matcher = pattern.matcher(testString);
			if (isPartialMatch) {
				if (matcher.find()) match = Match.ofRegex(matcher); else return Match.noMatch();
			} else {
				if (matcher.matches()) match = Match.ofRegex(matcher); else return Match.noMatch();
			}
		} else {
			if (isPartialMatch) {
				if (testString.contains(testFilter)) match = Match.ofString(); else return Match.noMatch();
			} else {
				if (testFilter.equals(testString)) match = Match.ofString(); else return Match.noMatch();
			}
		}

		// As a special case, if there are no valid locations, all locations are valid.
		// This exists because it doesn't make sense to remove all valid locations, you should disable the chat rule if you want to do that.
		// This way, we can also default to an empty set for validLocations.
		if (validLocations.isEmpty()) return match;
		// UNKNOWN isn't a valid location, so we act the same as the list being empty.
		if (validLocations.size() == 1 && validLocations.contains(Location.UNKNOWN)) return match;
		return validLocations.contains(Utils.getLocation()) ? match : Match.noMatch();
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

	// Allow value equality checks for ChatRule objects
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ChatRule chatRule)) return false;
		return Objects.equals(getName(), chatRule.getName()) && getEnabled() == chatRule.getEnabled() && getPartialMatch() == chatRule.getPartialMatch() && getRegex() == chatRule.getRegex() && getIgnoreCase() == chatRule.getIgnoreCase() && Objects.equals(getFilter(), chatRule.getFilter()) && Objects.equals(getValidLocations(), chatRule.getValidLocations()) && getHideMessage() == chatRule.getHideMessage() && getShowActionBar() == chatRule.getShowActionBar() && getShowAnnouncement() == chatRule.getShowAnnouncement() && Objects.equals(getReplaceMessage(), chatRule.getReplaceMessage()) && Objects.equals(getCustomSound(), chatRule.getCustomSound());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getEnabled(), getPartialMatch(), getRegex(), getIgnoreCase(), getFilter(), getValidLocations(), getHideMessage(), getShowActionBar(), getShowAnnouncement(), getReplaceMessage(), getCustomSound());
	}

	protected record Match(boolean matches, Optional<Matcher> matcher) {
		protected static Match noMatch() {
			return new Match(false, Optional.empty());
		}

		protected static Match ofString() {
			return new Match(true, Optional.empty());
		}

		protected static Match ofRegex(Matcher matcher) {
			return new Match(true, Optional.of(matcher));
		}

		protected String insertCaptureGroups(String replaceMessage) {
			if (!matches || matcher.isEmpty()) return replaceMessage;
			StringBuilder sb = new StringBuilder();
			matcher.get().appendReplacement(sb, replaceMessage);
			return sb.substring(matcher.get().start());
		}
	}
}
