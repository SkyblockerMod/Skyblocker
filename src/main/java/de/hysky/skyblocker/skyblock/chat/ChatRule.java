package de.hysky.skyblocker.skyblock.chat;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Data class to contain all the settings for a chat rule
 */
public class ChatRule {
    /**
     * Codec that can decode both string and enumset of locations, while encoding only enumset of locations.
     * <br>
     * This is necessary due to a change in how the locations are stored in the config.
     * <br>
     * This could probably be done in a more pretty manner using the FP-style functions in the Codec class, but this works. Feel free to refactor if you want.
     */
    static final Codec<EnumSet<Location>> LOCATION_FIXING_CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<EnumSet<Location>, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<EnumSet<Location>, T>> result = Location.SET_CODEC.decode(ops, input);
            if (result.isSuccess()) return result;
            // Necessary for empty strings, which would've been decoded as UNKNOWN otherwise.
            if (input instanceof String string && string.isEmpty()) return DataResult.success(Pair.of(EnumSet.noneOf(Location.class), ops.empty()));

            return Codec.STRING.decode(ops, input)
                        .ap(DataResult.success(pair -> pair.mapFirst( string -> Arrays.stream(string.split(", ?"))
                                        .map(Location::fromFriendlyName)
                                        .collect(() -> EnumSet.noneOf(Location.class), EnumSet::add, EnumSet::addAll))));
		}

		@Override
		public <T> DataResult<T> encode(EnumSet<Location> input, DynamicOps<T> ops, T prefix) {
			return Location.SET_CODEC.encode(input, ops, prefix);
		}
	};

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
            SoundEvent.CODEC.optionalFieldOf("customSound").forGetter(ChatRule::getCustomSoundOpt))
            .apply(instance, ChatRule::new));
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
        return replaceMessage == null ? Optional.empty() : Optional.of(replaceMessage);
    }

    protected void setReplaceMessage(String replaceMessage) {
        this.replaceMessage = replaceMessage;
    }

    protected SoundEvent getCustomSound() {
       return customSound;
    }

    private Optional<SoundEvent> getCustomSoundOpt() {
        return customSound == null ? Optional.empty() : Optional.of(customSound);
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
		return validLocations.isEmpty() || validLocations.contains(Utils.getLocation());
    }
}



