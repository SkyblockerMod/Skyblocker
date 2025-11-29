package de.hysky.skyblocker.skyblock.chat;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.annotations.GenToString;
import de.hysky.skyblocker.utils.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
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

	private static final UnaryOperator<Optional<String>> REMOVE_BLANK = opt -> opt.flatMap(s -> s.isBlank() ? Optional.empty() : Optional.of(s));

	private static final Codec<ChatRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("name").forGetter(ChatRule::getName),
			Codec.BOOL.fieldOf("enabled").forGetter(ChatRule::getEnabled),
			Codec.BOOL.fieldOf("partialMatch").forGetter(ChatRule::getPartialMatch),
			Codec.BOOL.fieldOf("regex").forGetter(ChatRule::getRegex),
			Codec.BOOL.fieldOf("ignoreCase").forGetter(ChatRule::getIgnoreCase),
			Codec.STRING.fieldOf("filter").forGetter(ChatRule::getFilter),
			LOCATION_FIXING_CODEC.fieldOf("locations").forGetter(ChatRule::getValidLocations),
			Codec.BOOL.fieldOf("hideOriginalMessage").forGetter(ChatRule::getHideMessage),
			Codec.STRING.optionalFieldOf("chatMessage").xmap(REMOVE_BLANK, REMOVE_BLANK).forGetter(ChatRule::getChatMessageOptional),
			Codec.STRING.optionalFieldOf("actionbarMessage").xmap(REMOVE_BLANK, REMOVE_BLANK).forGetter(ChatRule::getActionBarMessageOptional),
			Codec.STRING.optionalFieldOf("announcementMessage").xmap(REMOVE_BLANK, REMOVE_BLANK).forGetter(ChatRule::getAnnouncementMessageOptional),
			ToastMessage.CODEC.optionalFieldOf("toastMessage").forGetter(ChatRule::getToastMessageOptional),
			SoundEvent.CODEC.optionalFieldOf("customSound").forGetter(ChatRule::getCustomSoundOptional)
	).apply(instance, (s, aBoolean, aBoolean2, aBoolean3, aBoolean4, s2, locations, aBoolean5, s3, s4, s5, toastMessage1, soundEvent) ->
			new ChatRule(s, aBoolean, aBoolean2, aBoolean3, aBoolean4, s2, locations, aBoolean5, s3.orElse(null), s4.orElse(null), s5.orElse(null), toastMessage1.orElse(null), soundEvent.orElse(null))
	));

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
	private @Nullable String chatMessage;
	private @Nullable String actionBarMessage;
	private @Nullable String announcementMessage;
	private @Nullable ToastMessage toastMessage;
	private @Nullable SoundEvent customSound;

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
		this.chatMessage = null;
		this.actionBarMessage = null;
		this.announcementMessage = null;
		this.toastMessage = null;
		this.customSound = null;
	}

	ChatRule(String name, boolean enabled, boolean isPartialMatch, boolean isRegex, boolean isIgnoreCase, String filter, EnumSet<Location> validLocations, boolean hideMessage, @Nullable String chatMessage, @Nullable String actionBarMessage, @Nullable String announcementMessage, @Nullable ToastMessage toastMessage, @Nullable SoundEvent customSound) {
		this.name = name;
		this.enabled = enabled;
		this.isPartialMatch = isPartialMatch;
		this.isRegex = isRegex;
		this.isIgnoreCase = isIgnoreCase;
		this.filter = filter;
		this.validLocations = validLocations;
		this.hideMessage = hideMessage;
		this.chatMessage = chatMessage;
		this.actionBarMessage = actionBarMessage;
		this.announcementMessage = announcementMessage;
		this.toastMessage = toastMessage;
		this.customSound = customSound;
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

	@Nullable String getActionBarMessage() {
		return actionBarMessage;
	}

	private Optional<String> getActionBarMessageOptional() {
		return Optional.ofNullable(getActionBarMessage());
	}

	void setActionBarMessage(@Nullable String actionBarMessage) {
		if (actionBarMessage != null && actionBarMessage.isBlank()) actionBarMessage = null;
		this.actionBarMessage = actionBarMessage;
	}

	@Nullable String getChatMessage() {
		return chatMessage;
	}

	private Optional<String> getChatMessageOptional() {
		return Optional.ofNullable(getChatMessage());
	}

	void setChatMessage(@Nullable String chatMessage) {
		if (chatMessage != null && chatMessage.isBlank()) chatMessage = null;
		this.chatMessage = chatMessage;
	}

	@Nullable String getAnnouncementMessage() {
		return announcementMessage;
	}

	private Optional<String> getAnnouncementMessageOptional() {
		return Optional.ofNullable(getAnnouncementMessage());
	}

	void setAnnouncementMessage(@Nullable String announcementMessage) {
		if (announcementMessage != null && announcementMessage.isBlank()) announcementMessage = null;
		this.announcementMessage = announcementMessage;
	}

	@Nullable ToastMessage getToastMessage() {
		return toastMessage;
	}

	private Optional<ToastMessage> getToastMessageOptional() {
		return Optional.ofNullable(getToastMessage());
	}

	void setToastMessage(@Nullable ToastMessage toastMessage) {
		this.toastMessage = toastMessage;
	}

	protected @Nullable SoundEvent getCustomSound() {
		return customSound;
	}

	private Optional<SoundEvent> getCustomSoundOptional() {
		return Optional.ofNullable(getCustomSound());
	}

	protected void setCustomSound(@Nullable SoundEvent customSound) {
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
		String testString = isIgnoreCase ? inputString.toLowerCase(Locale.ENGLISH) : inputString;
		String testFilter = isIgnoreCase ? filter.toLowerCase(Locale.ENGLISH) : filter;
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

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof ChatRule chatRule)) return false;
		return getEnabled() == chatRule.getEnabled() && getPartialMatch() == chatRule.getPartialMatch() && getRegex() == chatRule.getRegex() && getIgnoreCase() == chatRule.getIgnoreCase() && getHideMessage() == chatRule.getHideMessage() && getName().equals(chatRule.getName()) && getFilter().equals(chatRule.getFilter()) && getValidLocations().equals(chatRule.getValidLocations()) && Objects.equals(getChatMessage(), chatRule.getChatMessage()) && Objects.equals(getActionBarMessage(), chatRule.getActionBarMessage()) && Objects.equals(getAnnouncementMessage(), chatRule.getAnnouncementMessage()) && Objects.equals(getToastMessage(), chatRule.getToastMessage()) && Objects.equals(getCustomSound(), chatRule.getCustomSound());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), getEnabled(), getPartialMatch(), getRegex(), getIgnoreCase(), getFilter(), getValidLocations(), getHideMessage(), getChatMessage(), getActionBarMessage(), getActionBarMessage(), getAnnouncementMessage(), getToastMessage(), getCustomSound());
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
			// This resets the matcher every time which kinda sucks :/
			return matcher.get().replaceFirst(replaceMessage).substring(matcher.get().start());
		}
	}

	static class ToastMessage {

		static final Codec<ToastMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ItemStack.OPTIONAL_CODEC.optionalFieldOf("icon", ItemStack.EMPTY).forGetter(o -> o.icon),
				Codec.STRING.fieldOf("message").forGetter(o -> o.message),
				Codec.LONG.fieldOf("display_duration").forGetter(o -> o.displayDuration)
		).apply(instance, ToastMessage::new));

		ItemStack icon;
		String message;
		long displayDuration;

		ToastMessage(ItemStack icon, String message, long displayDuration) {
			this.message = message;
			this.icon = icon;
			this.displayDuration = displayDuration;
		}

		ToastMessage() {
			this(new ItemStack(Items.PAINTING), "", 1000);
		}

		@Override
		@GenToString
		public native String toString();
	}

	@Override
	@GenToString
	public native String toString();
}
