package de.hysky.skyblocker.skyblock.chat;

import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Sound;
import net.minecraft.item.ItemStack;

import java.util.List;
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
    private String filter;
    private LocationOption validLocation;
    private List<ItemStack> validItems;

    //output
    private Boolean hideMessage;
    private Boolean showActionBar;
    private Boolean showAnnouncement;
    private String replaceMessage; //todo extract parts of original message
    private Sound customSound;

    /**
     * Creates a chat rule with default options.
     */
    public ChatRule(){
        this.name = "New Rule";

        this.enabled = true;
        this.isPartialMatch = false;
        this.isRegex = false;
        this.filter = "";
        this.validLocation = LocationOption.None;
        this.validItems = List.of();

        this.hideMessage = true;
        this.showActionBar = false;
        this.showAnnouncement = false;
        this.replaceMessage = null;
        this.customSound = null;
    }


    public ChatRule(String name, Boolean enabled, Boolean isPartialMatch, Boolean isRegex, String filter, LocationOption validLocation, List<ItemStack> validItems, Boolean hideMessage, Boolean showActionBar, Boolean showAnnouncement, String replaceMessage, Sound customSound) {
        this.name = name;
        this.enabled = enabled;
        this.isPartialMatch = isPartialMatch;
        this.isRegex = isRegex;
        this.filter = filter;
        this.validLocation = validLocation;
        this.validItems = validItems;
        this.hideMessage = hideMessage;
        this.showActionBar = showActionBar;
        this.showAnnouncement = showAnnouncement;
        this.replaceMessage = replaceMessage;
        this.customSound = customSound;
    }

    public Boolean getEnabled() { //todo remove unused getters and set
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getPartialMatch() {
        return isPartialMatch;
    }

    public void setPartialMatch(Boolean partialMatch) {
        isPartialMatch = partialMatch;
    }

    public Boolean getRegex() {
        return isRegex;
    }

    public void setRegex(Boolean regex) {
        isRegex = regex;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public LocationOption getValidLocation() {
        return validLocation;
    }

    public void setValidLocation(LocationOption validLocation) {
        this.validLocation = validLocation;
    }

    public List<ItemStack> getValidItems() {
        return validItems;
    }

    public void setValidItems(List<ItemStack> validItems) {
        this.validItems = validItems;
    }

    public Boolean getHideMessage() {
        return hideMessage;
    }

    public void setHideMessage(Boolean hideMessage) {
        this.hideMessage = hideMessage;
    }

    public Boolean getShowActionBar() {
        return showActionBar;
    }

    public void setShowActionBar(Boolean showActionBar) {
        this.showActionBar = showActionBar;
    }

    public Boolean getShowAnnouncement() {
        return showAnnouncement;
    }

    public void setShowAnnouncement(Boolean showAnnouncement) {
        this.showAnnouncement = showAnnouncement;
    }

    public String getReplaceMessage() {
        return replaceMessage;
    }

    public void setReplaceMessage(String replaceMessage) {
        this.replaceMessage = replaceMessage;
    }

    public Sound getCustomSound() {
        return customSound;
    }

    public void setCustomSound(Sound customSound) {
        this.customSound = customSound;
    }

    /**
     * checks every input option and if the games state and the inputted str matches them returns true.
     * @param str the chat message to check if fits
     * @return if the inputs are all true and the outputs should be performed
     */
    public Boolean isMatch(String str, MinecraftClient client){
        //enabled
        if (!enabled) return false;

        //filter
        if (filter.isEmpty()) return false;
        if(isRegex) {
            if (isPartialMatch) {
               if (! Pattern.compile(filter).matcher(str).find()) return false;
            }else {
                if (!str.matches(filter)) return false;
            }
        } else{
            if (isPartialMatch) {
                if (!str.contains(filter)) return false;
            }else {
                if (!filter.equals(str)) return false;
            }
        }

        //location
        String rawLocation = Utils.getLocationRaw();
        switch (validLocation){ //todo maybe add functionality straight into utils
            case Island -> {
                if (!rawLocation.equals("private_island")) return false;
            }
            case Hub -> {
                if (!rawLocation.equals("hub")) return false;
            }
            case Garden -> {
                if (!rawLocation.equals("garden")) return false;
            }
            default -> {}
        }

        //held items
        if (!validItems.isEmpty()){
            if (client.player == null) return false;
            ItemStack heldItem = client.player.getMainHandStack();
            if (!validItems.contains(heldItem)) return false;
        }

        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum LocationOption {
        None,
        Island,
        Hub,
        Garden; //todo add more

    }


}



