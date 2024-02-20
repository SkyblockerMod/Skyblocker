package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.sound.SoundEvent;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Data class to contain all the settings for a chat rule
 */
public class ChatRule {

    private String name;

    //inputs
    private Boolean enabled;
    private Boolean isPartialMatch;
    private Boolean isRegex;
    private Boolean isIgnoreCase;
    private String filter;
    private String validLocations;

    //output
    private Boolean hideMessage;
    private Boolean showActionBar;
    private Boolean showAnnouncement;
    private String replaceMessage;
    private SoundEvent customSound;
    /**
     * Creates a chat rule with default options.
     */
    protected ChatRule(){
        this.name = "New Rule";

        this.enabled = true;
        this.isPartialMatch = false;
        this.isRegex = false;
        this.isIgnoreCase = true;
        this.filter = "";
        this.validLocations = "";

        this.hideMessage = true;
        this.showActionBar = false;
        this.showAnnouncement = false;
        this.replaceMessage = null;
        this.customSound = null;
    }

    protected String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected Boolean getEnabled() {
        return enabled;
    }

    protected void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    protected Boolean getPartialMatch() {
        return isPartialMatch;
    }

    protected void setPartialMatch(Boolean partialMatch) {
        isPartialMatch = partialMatch;
    }

    protected Boolean getRegex() {
        return isRegex;
    }

    protected void setRegex(Boolean regex) {
        isRegex = regex;
    }

    protected Boolean getIgnoreCase() {
        return isIgnoreCase;
    }

    protected void setIgnoreCase(Boolean ignoreCase) {
        isIgnoreCase = ignoreCase;
    }

    protected String getFilter() {
        return filter;
    }

    protected void setFilter(String filter) {
        this.filter = filter;
    }

    protected Boolean getHideMessage() {
        return hideMessage;
    }

    protected void setHideMessage(Boolean hideMessage) {
        this.hideMessage = hideMessage;
    }

    protected Boolean getShowActionBar() {
        return showActionBar;
    }

    protected void setShowActionBar(Boolean showActionBar) {
        this.showActionBar = showActionBar;
    }

    protected Boolean getShowAnnouncement() {
        return showAnnouncement;
    }

    protected void setShowAnnouncement(Boolean showAnnouncement) {
        this.showAnnouncement = showAnnouncement;
    }

    protected String getReplaceMessage() {
        return replaceMessage;
    }

    protected void setReplaceMessage(String replaceMessage) {
        this.replaceMessage = replaceMessage;
    }

    protected SoundEvent getCustomSound() {
       return customSound;
    }

    protected void setCustomSound(SoundEvent customSound) {
        this.customSound = customSound;
    }

    protected String getValidLocations() {
        return validLocations;
    }

    protected void setValidLocations(String validLocations) {
        this.validLocations = validLocations;
    }

    /**
     * checks every input option and if the games state and the inputted str matches them returns true.
     * @param inputString the chat message to check if fits
     * @return if the inputs are all true and the outputs should be performed
     */
    protected Boolean isMatch(String inputString){
        //enabled
        if (!enabled) return false;

        //ignore case
        String testString;
        String testFilter;
        if (isIgnoreCase){
            testString = inputString.toLowerCase();
            testFilter = filter.toLowerCase();
        }else {
            testString = inputString;
            testFilter = filter;
        }

        //filter
        if (testFilter.isBlank()) return false;
        if(isRegex) {
            if (isPartialMatch) {
               if (! Pattern.compile(testFilter).matcher(testString).find()) return false;
            }else {
                if (!testString.matches(testFilter)) return false;
            }
        } else{
            if (isPartialMatch) {
                if (!testString.contains(testFilter)) return false;
            }else {
                if (!testFilter.equals(testString)) return false;
            }
        }

        //location
        if (validLocations.isBlank()){ //if no locations do not check
            return true;
        }
        String rawLocation = Utils.getLocationRaw();
        Boolean isLocationValid = null;
        for (String validLocation : validLocations.replace(" ", "").toLowerCase().split(",")) {//the locations are raw locations split by "," and start with ! if not locations
            String rawValidLocation = ChatRulesHandler.locations.get(validLocation.replace("!",""));
            if (rawValidLocation == null) continue;
            if (validLocation.startsWith("!")) {//not location
                if (Objects.equals(rawValidLocation, rawLocation.toLowerCase())) {
                    isLocationValid = false;
                    break;
                }
            }
            else {
                if (Objects.equals(rawValidLocation, rawLocation.toLowerCase())) { //normal location
                    isLocationValid = true;
                    break;
                }
            }
        }
        if (isLocationValid != null && isLocationValid){//if location is not in the list at all and is a not a "!" location or and is a normal location
            return true;
        }

        return false;
    }    
}



