package de.hysky.skyblocker.skyblock.dungeon;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;

import org.jspecify.annotations.Nullable;

import net.azureaaron.hmapi.data.party.PartyRole;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.s2c.ErrorS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HypixelS2CPacket;
import net.azureaaron.hmapi.network.packet.v2.s2c.PartyInfoS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

/**
 * Lets party members queue the party into a dungeon floor or kuudra tier by typing {@code !f7}, {@code !m7}, {@code !t5}, etc. in party chat.
 * Only does something if we are the party leader, which is checked with the party info packet.
 */
public class PartyQueueCommands extends ChatPatternListener {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final String[] FLOORS = {"ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN"};
	private static final String[] KUUDRA_TIERS = {"NORMAL", "HOT", "BURNING", "FIERY", "INFERNAL"};

	private @Nullable String pendingInstance;
	private String pendingName = "";

	public PartyQueueCommands() {
		super("^Party > (?:\\[[^]]+] )?(?<player>\\w{1,16})(?: [ቾ⚒])?: !(?<type>[fmtFMT])(?<tier>[1-7])$");

		HypixelPacketEvents.PARTY_INFO.register(this::onPacket);
	}

	@Override
	protected ChatFilterResult state() {
		return SkyblockerConfigManager.get().dungeons.partyQueueCommands ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
	}

	@Override
	protected boolean onMatch(Component message, Matcher matcher) {
		if (!Utils.isOnSkyblock() || pendingInstance != null) return false;

		int tier = Integer.parseInt(matcher.group("tier"));
		String type = matcher.group("type").toLowerCase(Locale.ENGLISH);
		String instance = switch (type) {
			case "f" -> "CATACOMBS_FLOOR_" + FLOORS[tier - 1];
			case "m" -> "MASTER_CATACOMBS_FLOOR_" + FLOORS[tier - 1];
			case "t" -> tier <= KUUDRA_TIERS.length ? "KUUDRA_" + KUUDRA_TIERS[tier - 1] : null;
			default -> null;
		};
		if (instance == null) return false;

		pendingInstance = instance;
		pendingName = type.toUpperCase(Locale.ENGLISH) + tier;
		HypixelNetworking.sendPartyInfoC2SPacket(2);
		// Don't get stuck if the server never answers
		Scheduler.INSTANCE.schedule(() -> {
			if (pendingInstance == instance) pendingInstance = null;
		}, 100);

		return false; // Don't filter the party chat message
	}

	private void onPacket(HypixelS2CPacket packet) {
		switch (packet) {
			case PartyInfoS2CPacket(var inParty, var members) when pendingInstance != null -> {
				UUID ourUuid = Objects.requireNonNull(CLIENT.getUser().getProfileId());

				if (inParty && members != null && members.get(ourUuid) == PartyRole.LEADER && CLIENT.player != null) {
					CLIENT.player.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.partyQueueCommands.queueing", pendingName).withStyle(ChatFormatting.GREEN)));
					MessageScheduler.INSTANCE.sendMessageAfterCooldown("/joininstance " + pendingInstance, true);
				}

				pendingInstance = null;
			}

			case ErrorS2CPacket(var id, var _) when id.equals(PartyInfoS2CPacket.ID) && pendingInstance != null -> pendingInstance = null;

			default -> {} //Do nothing
		}
	}
}
