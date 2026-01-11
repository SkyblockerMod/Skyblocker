package de.hysky.skyblocker.config.configs;

import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class ChatConfig {
	public boolean skyblockXpMessages = true;

	public boolean confirmationPromptHelper = false;

	public ChatFilterResult hideAbility = ChatFilterResult.PASS;

	public ChatFilterResult hideHeal = ChatFilterResult.PASS;

	public ChatFilterResult hideAOTE = ChatFilterResult.PASS;

	public ChatFilterResult hideImplosion = ChatFilterResult.PASS;

	public ChatFilterResult hideMoltenWave = ChatFilterResult.PASS;

	public ChatFilterResult hideAds = ChatFilterResult.PASS;

	public ChatFilterResult hideTeleportPad = ChatFilterResult.PASS;

	public ChatFilterResult hideCombo = ChatFilterResult.PASS;

	public ChatFilterResult hideAutopet = ChatFilterResult.PASS;

	public ChatFilterResult hideShowOff = ChatFilterResult.PASS;

	public ChatFilterResult hideToggleSkyMall = ChatFilterResult.PASS;

	public ChatFilterResult hideToggleLottery = ChatFilterResult.PASS;

	public ChatFilterResult hideMimicKill = ChatFilterResult.PASS;

	public ChatFilterResult hideDeath = ChatFilterResult.PASS;

	public boolean hideMana = false;

	@Deprecated
	public transient ChatFilterResult hideDicer = ChatFilterResult.PASS;

	public ChatFilterResult hideDungeonBreaker = ChatFilterResult.PASS;

	public ChatRuleConfig chatRuleConfig = new ChatRuleConfig();

	public static class ChatRuleConfig {
		public int announcementLength = 60;

		@Deprecated
		public transient int announcementScale = 3;
	}
}
