package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

public class ChatConfig {
    @SerialEntry
    public boolean skyblockXpMessages = true;

    @SerialEntry
    public boolean confirmationPromptHelper = false;

    @SerialEntry
    public ChatFilterResult hideAbility = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideHeal = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideAOTE = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideImplosion = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideMoltenWave = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideAds = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideTeleportPad = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideCombo = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideAutopet = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideShowOff = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideToggleSkyMall = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideMimicKill = ChatFilterResult.PASS;

    @SerialEntry
    public ChatFilterResult hideDeath = ChatFilterResult.PASS;

    @SerialEntry
    public boolean hideMana = false;

    @SerialEntry
    public ChatFilterResult hideDicer = ChatFilterResult.PASS;

    @SerialEntry
    public ChatRuleConfig chatRuleConfig = new ChatRuleConfig();

    public static class ChatRuleConfig {
        @SerialEntry
        public int announcementLength = 60;
    }
}
