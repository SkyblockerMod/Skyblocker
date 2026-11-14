package de.hysky.skyblocker.config.configs;

import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.annotations.EnumDisabledValue;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;

public class ChatConfig {
	public boolean skyblockXpMessages = true;

	public boolean confirmationPromptHelper = false;

	public float toastDisplayDuration = 4;

	public SackToast sackToast = new SackToast();

	public ChatFilterResult hideAbility = ChatFilterResult.PASS;

	public ChatFilterResult hideHeal = ChatFilterResult.PASS;

	public ChatFilterResult hideAOTE = ChatFilterResult.PASS;

	public ChatFilterResult hideImplosion = ChatFilterResult.PASS;

	public ChatFilterResult hideSpiritSceptre = ChatFilterResult.PASS;

	public ChatFilterResult hideMoltenWave = ChatFilterResult.PASS;

	public ChatFilterResult hideRareCrops = ChatFilterResult.PASS;

	public ChatFilterResult hideAds = ChatFilterResult.PASS;

	public ChatFilterResult hideTeleportPad = ChatFilterResult.PASS;

	public ChatFilterResult hideCombo = ChatFilterResult.PASS;

	public ChatFilterResult hideAutopet = ChatFilterResult.PASS;

	public ChatFilterResult hideShowOff = ChatFilterResult.PASS;

	public ChatFilterResult hideToggleSkyMall = ChatFilterResult.PASS;

	public ChatFilterResult hideToggleLottery = ChatFilterResult.PASS;

	public ChatFilterResult hideToggleBeekeeper = ChatFilterResult.PASS;

	public ChatFilterResult hideMimicKill = ChatFilterResult.PASS;

	public ChatFilterResult hideDeath = ChatFilterResult.PASS;

	public boolean hideMana = false;

	@Deprecated
	public transient ChatFilterResult hideDicer = ChatFilterResult.PASS;

	public ChatFilterResult hideDungeonBreaker = ChatFilterResult.PASS;

	public ChatFilterResult hideCritterCapture = ChatFilterResult.PASS;

	public ChatRuleConfig chatRuleConfig = new ChatRuleConfig();

	public static class ChatRuleConfig {
		public int announcementLength = 60;

		@Deprecated
		public transient int announcementScale = 3;
	}

	public static class SackToast {
		public EnableState enabled = EnableState.OFF;

		public float itemDisplayDuration = 1.2f;

		public boolean mergeAddedRemoved = false;

		public enum EnableState {
			@EnumDisabledValue
			OFF,
			KEEP_MESSAGE,
			HIDE_MESSAGE;

			public Component toComponent() {
				return Component.translatable("skyblocker.config.chat.sackToast.enable." + name());
			}
		}
	}
}
